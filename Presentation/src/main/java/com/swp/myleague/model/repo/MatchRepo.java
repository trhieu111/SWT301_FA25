package com.swp.myleague.model.repo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;     // Spring Data JPA để thao tác DB
import org.springframework.data.jpa.repository.Query;           // Dùng để viết JPQL query custom
import org.springframework.data.repository.query.Param;         // Gắn parameter vào query
import org.springframework.stereotype.Repository;               // Đánh dấu là tầng Repository (DAO)

import com.swp.myleague.model.entities.match.Match;             // Entity Match

@Repository                                                     // Nói với Spring: đây là nơi truy cập DB
public interface MatchRepo extends JpaRepository<Match, UUID> { // Kế thừa CRUD mặc định, khóa chính là UUID

    // ===========================
    // LẤY CÁC TRẬN ĐẤU SẮP DIỄN RA CỦA 1 CÂU LẠC BỘ
    // ===========================
    @Query("""
      SELECT m FROM Match m                                    
      JOIN m.matchClubStats mcs                                 -- Join bảng MatchClubStat để biết CLB nào tham gia trận
      WHERE mcs.club.clubId = :clubId                           -- Chỉ lấy trận có CLB này tham gia
        AND m.matchStartTime > CURRENT_TIMESTAMP                -- Lọc: trận phải diễn ra TRONG TƯƠNG LAI (future)
      ORDER BY m.matchStartTime ASC                             -- Sắp xếp: trận gần nhất → xa nhất
      """)
    List<Match> findUpcomingMatchesByClub(@Param("clubId") UUID clubId);
    // Trả về danh sách Match, dùng cho UC: xem lịch thi đấu của CLB


    // ===========================
    // TÌM TRẬN THEO MÔ TẢ (VD: "Vòng 1", "Vòng 2")
    // ===========================
    List<Match> findByMatchDescription(String roundDescription);
    // Spring tự generate SQL:
    // SELECT * FROM Match WHERE match_description = ?


    // ===========================
    // TÌM TRẬN TRONG KHOẢNG THỜI GIAN (±15 phút)
    // ===========================
    @Query("SELECT DISTINCT m FROM Match m WHERE m.matchStartTime BETWEEN :start AND :end")
    List<Match> findMatchesBetween(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);
    /*
     * Dùng DISTINCT để tránh trùng kết quả vì Match JOIN với nhiều MatchClubStat
     * Lấy trận diễn ra giữa khoảng thời gian
     * Ứng dụng: lấy trận đang diễn ra hoặc chuẩn bị diễn ra
     */


    // ===========================
    // LẤY TRẬN SẮP DIỄN RA TRONG KHOẢNG THỜI GIAN GIỚI HẠN
    // ===========================
    @Query("""
          SELECT m FROM Match m
          WHERE m.matchStartTime > :now       -- trận phải nằm trong tương lai
            AND m.matchStartTime < :within    -- và không vượt quá một mốc thời gian (now + 1 day / now + 7 days)
      """)
    List<Match> findUpcomingMatches(@Param("now") LocalDateTime now,
                                    @Param("within") LocalDateTime within);
    /*
     * Use case: hiển thị các trận sắp diễn ra trong vòng 24h / trong tuần.
     */

}
