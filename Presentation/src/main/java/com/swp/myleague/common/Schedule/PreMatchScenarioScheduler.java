package com.swp.myleague.common.Schedule;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.swp.myleague.model.entities.information.Player;
import com.swp.myleague.model.entities.match.Match;
import com.swp.myleague.model.entities.match.MatchClubStat;
import com.swp.myleague.model.entities.match.MatchEvent;
import com.swp.myleague.model.entities.match.MatchEventType;
import com.swp.myleague.model.entities.match.MatchPlayerStat;
import com.swp.myleague.model.repo.MatchClubStatRepo;
import com.swp.myleague.model.repo.MatchEventRepo;
import com.swp.myleague.model.repo.MatchPlayerStatRepo;
import com.swp.myleague.model.repo.MatchRepo;
import com.swp.myleague.model.repo.PlayerRepo;
import com.swp.myleague.model.service.matchservice.MatchService;
import com.swp.myleague.utils.gemini_matchevent.GeminiClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PreMatchScenarioScheduler {

    private final MatchRepo matchRepo;

    private final MatchService matchService;

    // private final LlamaClientService llamaClientService;

    // private final OpenAiService openAiService;

    private final GeminiClient geminiClient;

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Ho_Chi_Minh")
    public void generateScenarioForUpcomingMatches() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime threshold = now.plusMinutes(90);

        List<Match> upcoming = matchRepo.findUpcomingMatches(now, threshold);

        for (Match match : upcoming) {
            try {
                String prompt = buildPrompt(match);
                String script = geminiClient.generate(prompt); // ✅ sử dụng LLaMA

                applyMatchScript(match, script);

                log.info("Generated scenario for match {}", match.getMatchTitle());
            } catch (Exception ex) {
                log.error("Failed to create scenario for match {}", match.getMatchId(), ex);
            }
        }
    }

    @Autowired
    private MatchRepo matchRepository;

    @Autowired
    private PlayerRepo playerRepository;

    @Autowired
    private MatchEventRepo matchEventRepository;

    @Autowired
    private MatchClubStatRepo matchClubStatRepository;

    @Autowired
    private MatchPlayerStatRepo matchPlayerStatRepository;

    public void applyMatchScript(Match match, String script) {
        List<MatchEvent> events = new ArrayList<>();

        List<MatchPlayerStat> playerStats = matchPlayerStatRepository.findByMatch(match);
        Map<UUID, MatchPlayerStat> playerStatsMap = playerStats.stream()
                .collect(Collectors.toMap(stat -> stat.getPlayer().getPlayerId(), stat -> stat));

        Map<UUID, Integer> playerAppearMinutes = new HashMap<>();

        // List<MatchClubStat> clubStats = matchClubStatRepo.findByMatch(match);
        // Map<UUID, MatchClubStat> clubStatsMap = clubStats.stream()
        // .collect(Collectors.toMap(stat -> stat.getClub().getClubId(), stat -> stat));

        String[] lines = script.split("\\R");

        for (String line : lines) {
            try {
                line = line.trim();
                if (line.isEmpty() || !line.contains(":"))
                    continue;

                String[] parts = line.split(":", 3);
                if (parts.length < 3)
                    continue;

                Integer minute = parseMinute(parts[0].trim());
                String content = parts[2].trim();

                MatchEvent event = new MatchEvent();
                event.setMatch(match);
                event.setMatchEventMinute(minute);
                event.setMatchEventTitle(content);
                event.setMatchEventType(determineEventType(content));

                // Tìm Player ID nếu có trong câu (theo định dạng Player 1-10)
                extractPlayerByName(content).ifPresent(player -> {
                    int prevMinute = playerAppearMinutes.getOrDefault(player.getPlayerId(), 0);
                    playerAppearMinutes.put(player.getPlayerId(), Math.max(prevMinute, minute));
                    event.setPlayer(player);

                    MatchPlayerStat stat = playerStatsMap.get(player.getPlayerId());
                    if (stat != null) {
                        if (event.getMatchEventType() == MatchEventType.Goal) {
                            stat.setMatchPlayerGoal(
                                    (stat.getMatchPlayerGoal() == null ? 0 : stat.getMatchPlayerGoal()) + 1);
                        } else if (event.getMatchEventType() == MatchEventType.Shoot) {
                            stat.setMatchPlayerShoots(
                                    (stat.getMatchPlayerShoots() == null ? 0 : stat.getMatchPlayerShoots()) + 1);
                        }
                        matchPlayerStatRepository.save(stat);
                    }
                });

                // Cập nhật cho club nếu thấy tên Club
                for (MatchClubStat clubStat : match.getMatchClubStats()) {
                    if (content.contains(clubStat.getClub().getClubName())) {
                        if (event.getMatchEventType() == MatchEventType.YellowCard) {
                            clubStat.setMatchClubStatYellowCard((clubStat.getMatchClubStatYellowCard() == null ? 0
                                    : clubStat.getMatchClubStatYellowCard()) + 1);
                        } else if (event.getMatchEventType() == MatchEventType.Shoot) {
                            clubStat.setMatchClubStatShoots(
                                    (clubStat.getMatchClubStatShoots() == null ? 0 : clubStat.getMatchClubStatShoots())
                                            + 1);
                        }
                        matchClubStatRepository.save(clubStat);
                    }
                }

                matchEventRepository.save(event);
                events.add(event);
                match.getMatchPlayerStats().forEach(mps -> mps.setMatchPlayerMinutedPlayed(minute));
                matchPlayerStatRepository.saveAll(match.getMatchPlayerStats());
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi xử lý dòng: " + line);
                e.printStackTrace();
            }
            
        }

        match.setMatchEvents(events);
        matchRepository.save(match);
    }

    private int parseMinute(String timestamp) {
        try {
            String[] parts = timestamp.trim().split(":");
            return Integer.parseInt(parts[0]) * 1; // hoặc * 60 + Integer.parseInt(parts[1]) nếu cần chính xác hơn
        } catch (Exception e) {
            return 0;
        }
    }

    private Optional<Player> extractPlayerByName(String content) {
        Pattern pattern = Pattern.compile("Player\\s+\\d+-\\d+");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String playerName = matcher.group(); // ví dụ: "Player 1-10"
            return playerRepository.findByPlayerFullName(playerName);
        }
        return Optional.empty();
    }

    private MatchEventType determineEventType(String content) {
        if (content.toLowerCase().contains("Thẻ vàng"))
            return MatchEventType.YellowCard;
        if (content.contains("thẻ đỏ") || content.contains("truất quyền thi đấu"))
            return MatchEventType.RedCard;
        if (content.contains("dứt điểm") || content.contains("cú sút") || content.contains("sút bóng")
                || content.contains("vô lê")
                || content.contains("đánh đầu") || content.contains("cú đá"))
            return MatchEventType.Shoot;
        if (content.contains("ghi bàn") || content.contains("lập công") || content.contains("mở tỷ số")
                || content.contains("nâng tỷ số") || content.contains("gỡ hòa") || content.contains("ấn định")
                || content.contains("rút ngắn")
                || content.contains("phản lưới"))
            return MatchEventType.Goal;
        return MatchEventType.Highlight;
    }

    private record TeamLineup(
            String clubName,
            List<Player> starters,
            List<Player> substitutes) {
    }

    private TeamLineup extractLineup(Match match, MatchClubStat stat) {
        String matchId = match.getMatchId().toString();
        String clubId = stat.getClub().getClubId().toString();
        String clubName = stat.getClub().getClubName();

        List<Player> starters = matchService.getStartingLineup(matchId, clubId);
        List<Player> substitutes = matchService.getSubstitueLineup(matchId, clubId);

        return new TeamLineup(clubName, starters, substitutes);
    }

    /** Tạo prompt hướng dẫn ChatGPT */
    private String buildPrompt(Match m) {
        TeamLineup team1 = extractLineup(m, m.getMatchClubStats().get(0));
        TeamLineup team2 = extractLineup(m, m.getMatchClubStats().get(1));

        String prompt = """
                Bạn là bình luận viên bóng đá. Hãy tạo kịch bản diễn biến 90 phút trận đấu ở mức KHỞI ĐẦU
                (những sự kiện quan trọng dự kiến, không quá chi tiết) cho trận:

                - Tiêu đề: %s
                - Thời gian bắt đầu: %s (GMT+7)

                Danh sách cầu thủ dự kiến:

                - %s (Sân nhà)
                Đội hình chính:
                %s

                Dự bị:
                %s

                - %s (Sân khách)
                Đội hình chính:
                %s

                Dự bị:
                %s

                Yêu cầu:
                1. Trả về **MỘT chuỗi duy nhất**, mỗi dòng dạng "HH:MM:Nội dung".
                   Ví dụ: `00:00:Trọng tài thổi còi khai cuộc`.

                2. Trong **mỗi dòng nội dung có liên quan đến một cầu thủ**, hãy ghi theo định dạng:
                   `HH:MM:<Nội dung> - <Tên cầu thủ> (<Vị trí> #<Số áo>) - <Loại sự kiện>`

                   Ví dụ: `00:45:Cú dứt điểm căng của Nguyễn Văn A - Nguyễn Văn A (FW #10) - Goal`

                3. Phân loại nội dung theo các loại sau:
                   - `Goal` (bàn thắng)
                   - `Shoot` (dứt điểm)
                   - `YellowCard` (thẻ vàng)
                   - `RedCard` (thẻ đỏ)

                   ⚠️ Bạn không cần ghi loại này vào dòng, chỉ cần giúp mô tả chi tiết rõ để hệ thống có thể hiểu được thuộc loại nào.

                4. Hạn chế spam nhiều sự kiện ở cùng 1 phút. Cố gắng tạo kịch bản hợp lý, căng thẳng dần.

                """
                .formatted(
                        m.getMatchTitle(),
                        m.getMatchStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        team1.clubName,
                        formatPlayerList(team1.starters),
                        formatPlayerList(team1.substitutes),
                        team2.clubName,
                        formatPlayerList(team2.starters),
                        formatPlayerList(team2.substitutes));

        return prompt;
    }

    private String formatPlayerList(List<Player> players) {
        return players.stream()
                .map(p -> "- ID: " + p.getPlayerId() + ", Name: " + p.getPlayerFullName()
                        + ", Position: " + p.getPlayerPosition().name()
                        + ", Number: #" + p.getPlayerNumber())
                .collect(Collectors.joining("\n"));
    }
}
