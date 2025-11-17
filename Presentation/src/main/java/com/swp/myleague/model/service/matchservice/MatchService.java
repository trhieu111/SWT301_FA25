package com.swp.myleague.model.service.matchservice;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.information.Club;
import com.swp.myleague.model.entities.information.Player;
import com.swp.myleague.model.entities.match.Match;
import com.swp.myleague.model.entities.match.MatchClubStat;
import com.swp.myleague.model.entities.match.MatchPlayerStat;
import com.swp.myleague.model.repo.ClubRepo;
import com.swp.myleague.model.repo.MatchClubStatRepo;
import com.swp.myleague.model.repo.MatchRepo;

@Service // Đánh dấu đây là tầng Service (xử lý logic nghiệp vụ)
public class MatchService implements IService<Match> {

    @Autowired
    MatchRepo matchRepo; // Làm việc với bảng Match

    @Autowired
    ClubRepo clubRepo; // Lấy danh sách CLB, dùng cho auto-generating fixtures

    @Autowired
    MatchClubStatRepo matchClubStatRepo; // Lưu thống kê trận đấu theo CLB

    // ===========================================================
    // CRUD CƠ BẢN
    // ===========================================================

    @Override
    public List<Match> getAll() {
        // Lấy toàn bộ trận đấu từ DB (findAll do JPA cung cấp)
        return matchRepo.findAll();
    }

    @Override
    public Match getById(String id) {
        // Lấy Match theo UUID, nếu không có → ném exception
        return matchRepo.findById(UUID.fromString(id)).orElseThrow();
    }

    public List<Match> getByClubId(String clubId) {
        // Lấy danh sách trận SẮP DIỄN RA của 1 CLB (query custom)
        return matchRepo.findUpcomingMatchesByClub(UUID.fromString(clubId));
    }

    @Override
    public Match save(Match e) {
        // Lưu hoặc update 1 trận đấu
        return matchRepo.save(e);
    }

    @Override
    public Match delete(String id) {
        // Chưa implement, sẽ cần khi làm chức năng admin xoá trận
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    // ===========================================================
    // AUTO-GENERATE FIXTURES (tạo lịch thi đấu)
    // ===========================================================
    public List<Match> autoGenFixturesMatches(LocalDate startDate, List<LocalTime> matchSlots) {

        // Lấy tất cả CLB hợp lệ
        List<Club> clubs = clubRepo.findAll().stream()
                .filter(clb -> clb.getClubLogoPath() != null
                        && clb.getClubLogoPath().contains("images")
                        && clb.getIsActive())
                .collect(Collectors.toList());

        List<Match> matches = new ArrayList<>();
        int numTeams = clubs.size();

        // Ít hơn 2 đội → không tạo lịch
        if (numTeams < 2)
            return matches;

        int numRounds = numTeams - 1; // Giải vòng tròn 1 lượt
        int halfSize = numTeams / 2;

        // Áp dụng thuật toán "Circle Method"
        List<Club> rotation = new ArrayList<>(clubs);
        Club fixed = rotation.remove(0); // Một đội cố định, xoay các đội còn lại

        // Tạo các vòng đấu
        for (int round = 0; round < numRounds; round++) {

            String roundDescription = "Vòng " + (round + 1);

            // Kiểm tra DB xem vòng này đã tồn tại chưa
            List<Match> existingMatches = matchRepo.findByMatchDescription(roundDescription).stream()
                    .filter(m -> m.getMatchStartTime().getYear() == LocalDate.now().getYear())
                    .toList();

            List<Match> roundMatches = new ArrayList<>();
            Set<Club> usedClubs = new HashSet<>();

            // Nếu DB đã có → không tạo lại
            if (!existingMatches.isEmpty()) {
                matches.addAll(existingMatches);
                existingMatches.forEach(m -> {
                    usedClubs.add(m.getMatchClubStats().get(0).getClub());
                    usedClubs.add(m.getMatchClubStats().get(1).getClub());
                });

                // Nếu đủ số trận cho vòng → bỏ qua vòng này luôn
                if (existingMatches.size() == halfSize)
                    continue;
            }

            // Xáo trộn danh sách xoay
            Collections.shuffle(rotation);

            // Ghép đội theo cặp
            for (int i = 0; i < halfSize; i++) {

                Club home = (i == 0) ? fixed : rotation.get(i - 1);
                Club away = rotation.get(rotation.size() - i - 1);

                // Tránh trùng đội nếu DB đã có
                if (usedClubs.contains(home) || usedClubs.contains(away))
                    continue;

                // Tạo match
                Match match = new Match();
                match.setMatchId(UUID.randomUUID());
                match.setMatchTitle(home.getClubName() + " vs " + away.getClubName());
                match.setMatchDescription(roundDescription);

                // Tạo MatchClubStat mặc định
                MatchClubStat homeStat = new MatchClubStat(null, 0, 0, 0, 0, 0, 0, 0, match, home);
                MatchClubStat awayStat = new MatchClubStat(null, 0, 0, 0, 0, 0, 0, 0, match, away);

                match.setMatchClubStats(List.of(homeStat, awayStat));

                roundMatches.add(match);
                usedClubs.add(home);
                usedClubs.add(away);
            }

            // Lên lịch đấu: mỗi vòng chia thành 2 ngày (T7 + CN)
            LocalDate weekStart = startDate.plusWeeks(round);

            LocalDate day1 = weekStart.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
            LocalDate day2 = day1.plusDays(1);

            int totalMatches = roundMatches.size();
            int split = (int) Math.ceil(totalMatches / 2.0);

            // Gán thời gian cho từng trận
            for (int i = 0; i < totalMatches; i++) {

                Match match = roundMatches.get(i);

                LocalDate matchDate = (i < split) ? day1 : day2;
                int slotIndex = i % matchSlots.size(); // Chọn giờ đá
                LocalTime matchTime = matchSlots.get(slotIndex);

                match.setMatchStartTime(matchDate.atTime(matchTime));
                match.setMatchLinkLivestream("https://example.com/live/" + match.getMatchId());

                matches.add(match);
            }
        }

        return matches;
    }

    // ===========================================================
    // LẤY DANH SÁCH TRẬN THEO VÒNG
    // ===========================================================
    public List<Match> getByRound(Integer roundNumber) {
        // matchDescription = "Vòng X" → tách lấy số vòng
        return getAll().stream()
                .filter(m -> m.getMatchDescription().split(" ")[1].equals(roundNumber + ""))
                .toList();
    }

    // ===========================================================
    // LƯU LIST MATCH AUTO-GENERATED + CLUB STATS
    // ===========================================================
    public List<Match> saveAuto(List<Match> list) {

        // Lưu tất cả Match một lần → JPA auto tạo ID
        List<Match> newMatches = matchRepo.saveAll(list);

        AtomicInteger index = new AtomicInteger(0);

        for (Match match : list) {
            Match savedMatch = newMatches.get(index.getAndIncrement());

            // Set lại quan hệ Match → ClubStat
            match.getMatchClubStats().forEach(mcs -> {
                mcs.setMatch(savedMatch);
            });

            // Lưu Club Stats
            matchClubStatRepo.saveAll(match.getMatchClubStats());
        }

        return newMatches;
    }

    // ===========================================================
    // LẤY ĐỘI HÌNH RA SÂN (starter)
    // ===========================================================
    @Transactional(readOnly = true)
    public List<Player> getStartingLineup(String matchId, String clubId) {

        Match match = getById(matchId);
        Club club = clubRepo.findById(UUID.fromString(clubId)).orElseThrow();

        return match.getMatchPlayerStats().stream()
                .filter(stat -> stat.getPlayer().getClub().equals(club)
                        && Boolean.TRUE.equals(stat.getIsStarter()))
                .map(MatchPlayerStat::getPlayer)
                .collect(Collectors.toList());
    }

    // ===========================================================
    // LẤY ĐỘI HÌNH DỰ BỊ
    // ===========================================================
    @Transactional(readOnly = true)
    public List<Player> getSubstitueLineup(String matchId, String clubId) {

        Match match = getById(matchId);
        Club club = clubRepo.findById(UUID.fromString(clubId)).orElseThrow();

        return match.getMatchPlayerStats().stream()
                .filter(stat -> stat.getPlayer().getClub().equals(club)
                        && Boolean.FALSE.equals(stat.getIsStarter()))
                .map(MatchPlayerStat::getPlayer)
                .collect(Collectors.toList());
    }

    // ===========================================================
    // LẤY TRẬN ĐẤU ĐANG DIỄN RA (±15 phút)
    // Use Case: TRẬN ĐẤU SẮP DIỄN RA / ĐANG DIỄN RA
    // ===========================================================
    @Transactional(readOnly = true)
    public List<Match> getMatchesBetween(LocalDateTime aroundTime) {

        LocalDateTime start = aroundTime.minusMinutes(15);
        LocalDateTime end = aroundTime.plusMinutes(15);

        // Repo query lấy các trận trong khoảng 30 phút (15 trước - 15 sau)
        return matchRepo.findMatchesBetween(start, end);
    }

}
