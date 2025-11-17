package com.swp.myleague.controller;
// Định nghĩa package cho Controller này.

import java.io.File;
// Import lớp File để thao tác với hệ thống tập tin (dùng cho việc lưu thumbnail).
import java.io.IOException;
// Import lớp IOException để xử lý lỗi vào/ra tập tin.
import java.io.InputStream;
// Import lớp InputStream để đọc dữ liệu từ file upload.
import java.nio.file.Files;
// Import lớp Files để thực hiện các thao tác file/thư mục nâng cao (dùng để copy file).
import java.nio.file.StandardCopyOption;
// Import tùy chọn để thay thế file nếu đã tồn tại khi copy.
import java.security.Principal;
// Import Principal để lấy thông tin người dùng đang đăng nhập (Authentication).
import java.time.LocalDateTime;
// Import LocalDateTime để làm việc với thời gian (so sánh trận đấu, thời gian comment).
import java.util.ArrayList;
// Import ArrayList để tạo danh sách động (dùng cho clubStats).
import java.util.Comparator;
// Import Comparator để so sánh đối tượng (dùng để tìm MOTM).
import java.util.List;
// Import List (interface) để làm việc với danh sách.
import java.util.UUID;
// Import UUID để làm việc với các định danh duy nhất (dùng cho MatchId).

import org.apache.poi.ss.usermodel.Row;
// Import Row từ POI để đọc từng hàng (row) trong file Excel.
import org.apache.poi.ss.usermodel.Sheet;
// Import Sheet từ POI để đọc từng trang tính (sheet) trong file Excel.
import org.apache.poi.ss.usermodel.Workbook;
// Import Workbook từ POI để đại diện cho toàn bộ file Excel.
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
// Import XSSFWorkbook để xử lý file Excel định dạng .xlsx.
import org.springframework.beans.factory.annotation.Autowired;
// Import Annotation Autowired để tiêm các dependency (Service).
import org.springframework.stereotype.Controller;
// Import Annotation Controller để đánh dấu lớp là Controller của Spring MVC.
import org.springframework.ui.Model;
// Import Model để đóng gói dữ liệu gửi sang giao diện (Template).
import org.springframework.web.bind.annotation.RequestMapping;
// Import Annotation RequestMapping để định nghĩa URL cơ sở cho Controller.
import org.springframework.web.bind.annotation.RequestParam;
// Import Annotation RequestParam để lấy tham số từ URL hoặc Form Data.
import org.springframework.web.multipart.MultipartFile;
// Import MultipartFile để xử lý file được upload qua form (dùng cho highlight thumbnail và import lineup).

import com.swp.myleague.model.entities.Comment;
// Import Entity Comment.
import com.swp.myleague.model.entities.Role;
// Import Enum Role (dùng để kiểm tra quyền Admin).
import com.swp.myleague.model.entities.User;
// Import Entity User.
import com.swp.myleague.model.entities.information.Player;
// Import Entity Player.
import com.swp.myleague.model.entities.match.Match;
// Import Entity Match.
import com.swp.myleague.model.entities.match.MatchClubStat;
// Import Entity MatchClubStat (Thống kê CLB trong trận).
import com.swp.myleague.model.entities.match.MatchEvent;
// Import Entity MatchEvent (Dùng cho Highlight).
import com.swp.myleague.model.entities.match.MatchEventType;
// Import Enum MatchEventType.
import com.swp.myleague.model.entities.match.MatchPlayerStat;
// Import Entity MatchPlayerStat (Thống kê Cầu thủ trong trận).
import com.swp.myleague.model.service.CommentService;
// Import Service CommentService.
import com.swp.myleague.model.service.UserService;
// Import Service UserService.
import com.swp.myleague.model.service.informationservice.PlayerService;
// Import Service PlayerService.
import com.swp.myleague.model.service.matchservice.MatchClubStatService;
// Import Service MatchClubStatService.
import com.swp.myleague.model.service.matchservice.MatchEventService;
// Import Service MatchEventService.
import com.swp.myleague.model.service.matchservice.MatchPlayerStatService;
// Import Service MatchPlayerStatService.
import com.swp.myleague.model.service.matchservice.MatchService;
// Import Service MatchService (chứa các hàm logic chính liên quan đến Match).
import org.springframework.web.bind.annotation.GetMapping;
// Import Annotation GetMapping.
import org.springframework.web.bind.annotation.PathVariable;
// Import Annotation PathVariable để lấy tham số từ đường dẫn URL.
import org.springframework.web.bind.annotation.PostMapping;
// Import Annotation PostMapping.

@Controller
// Đánh dấu lớp là Controller của Spring MVC.
@RequestMapping(value = { "/match", "/match/" })
// Định nghĩa URL cơ sở là "/match" cho tất cả các request trong Controller này.
public class MatchController {

    @Autowired
    MatchService matchService;
// Tiêm MatchService để thao tác với dữ liệu Match.

    @Autowired
    MatchClubStatService matchClubStatService;
// Tiêm MatchClubStatService để thao tác với thống kê CLB.

    @Autowired
    MatchPlayerStatService matchPlayerStatService;
// Tiêm MatchPlayerStatService để thao tác với thống kê Cầu thủ.

    @Autowired
    MatchEventService matchEventService;
// Tiêm MatchEventService để thao tác với sự kiện trận đấu (Highlight).

    @Autowired
    UserService userService;
// Tiêm UserService để thao tác với dữ liệu người dùng (dùng cho comment và phân quyền).

    @Autowired
    PlayerService playerService;
// Tiêm PlayerService để thao tác với dữ liệu Cầu thủ (dùng cho import lineup).

    @Autowired
    CommentService commentService;
// Tiêm CommentService để thao tác với bình luận.

// --- BẮT ĐẦU CÁC PHƯƠNG THỨC XỬ LÝ REQUEST ---

### 2. Phương thức Xem Trận Đấu Đã Diễn Ra (Past Matches)

```java
    @GetMapping("")
    // Ánh xạ yêu cầu GET đến /match.
    public String getAllMatch(@RequestParam(name = "search", required = false) String search,
// Tham số tìm kiếm chung (không bắt buộc).
                              @RequestParam(name = "searchPlayerName", required = false) String searchPlayerName,
// Tham số tìm kiếm theo tên cầu thủ (không bắt buộc).
                              @RequestParam(name = "searchClubName", required = false) String searchClubName,
// Tham số tìm kiếm theo tên CLB (không bắt buộc).
                              @RequestParam(name = "season", required = false) String season,
// Tham số lọc theo mùa giải (không bắt buộc).
                              Model model) {
// Đối tượng Model để truyền dữ liệu sang giao diện.
        List<Match> matcheList = matchService.getAll().stream()
// Lấy toàn bộ trận đấu từ DB và bắt đầu tạo Stream.
                .filter(m -> m.getMatchStartTime().compareTo(LocalDateTime.now()) < 0).toList();
// LỌC: Chỉ giữ lại các trận đấu CÓ THỜI GIAN BẮT ĐẦU NHỎ HƠN THỜI GIAN HIỆN TẠI (trận đã diễn ra).

        // Lọc theo mùa giải
        if (season != null && !season.isBlank()) {
// Nếu có tham số mùa giải.
            try {
                int year = Integer.parseInt(season);
// Chuyển chuỗi thành số (năm).
                matcheList = matcheList.stream()
                        .filter(m -> m.getMatchStartTime().getYear() == year)
// LỌC: Chỉ giữ lại các trận có năm diễn ra trùng với năm đã chọn.
                        .toList();
            } catch (NumberFormatException e) {
// Bắt lỗi nếu chuỗi 'season' không phải là số.
                // Nếu season không phải số, bỏ qua filter này
            }
        }

        if (search != null && !search.isBlank()) {
// Nếu có tham số tìm kiếm chung.
            matcheList = matcheList.stream().filter(m -> m.getMatchDescription().contains(search)).toList();
// LỌC: Chỉ giữ lại trận đấu có mô tả chứa chuỗi tìm kiếm.
        }
        if (searchPlayerName != null && !searchPlayerName.isBlank()) {
// Nếu có tham số tìm kiếm theo tên cầu thủ.
            matcheList = matcheList.stream().filter(m -> m.getMatchPlayerStats().stream()
// LỌC: Bắt đầu Stream trên thống kê cầu thủ của mỗi trận.
                    .anyMatch(mps -> mps.getPlayer().getPlayerFullName().contains(searchPlayerName))).toList();
// LỌC: Chỉ giữ lại trận đấu nếu CÓ BẤT KỲ CẦU THỦ nào tham gia có tên chứa chuỗi tìm kiếm.
        }
        if (searchClubName != null && !searchClubName.isBlank()) {
// Nếu có tham số tìm kiếm theo tên CLB.
            matcheList = matcheList.stream().filter(m -> m.getMatchClubStats().stream()
// LỌC: Bắt đầu Stream trên thống kê CLB của mỗi trận.
                    .anyMatch(mcs -> mcs.getClub().getClubName().contains(searchClubName))).toList();
// LỌC: Chỉ giữ lại trận đấu nếu CÓ BẤT KỲ CLB nào tham gia có tên chứa chuỗi tìm kiếm.
        }
        model.addAttribute("matches", matcheList);
// Đóng gói danh sách trận đấu đã lọc gửi sang giao diện dưới key "matches".
        model.addAttribute("searchPlayerName", searchPlayerName);
// Gửi lại giá trị tìm kiếm để hiển thị trên form (user experience).
        model.addAttribute("searchClubName", searchClubName);
// Gửi lại giá trị tìm kiếm CLB.
        model.addAttribute("search", search);
// Gửi lại giá trị tìm kiếm chung.
        model.addAttribute("season", season);
// Gửi lại giá trị mùa giải.

        return "Match";
// Trả về tên view (Match.html) để render.
    }

### 3. Phương thức Xem Chi Tiết Trận Đấu

```java
    @GetMapping("/{matchId}")
    // Ánh xạ yêu cầu GET đến /match/{matchId} (ví dụ: /match/123e4567-e89b-12d3-a456-426614174000).
    public String getMatchById(@PathVariable(name = "matchId") String matchId, Model model, Principal principal) {
// Tham số matchId lấy từ đường dẫn URL. Principal lấy thông tin người dùng.
        Boolean isAdmin = false;
// Biến cờ để kiểm tra quyền Admin.
        if (principal != null) {
// Nếu có người dùng đăng nhập.
            String username = principal.getName();
// Lấy username của người dùng đang đăng nhập.
            User user = userService.findByUsername(username);
// Tìm đối tượng User.
            isAdmin = user.getRole() == Role.ADMIN;
// Thiết lập isAdmin nếu User có vai trò là ADMIN.
        }

        List<MatchPlayerStat> playerStats = matchPlayerStatService.getAllByMatchId(matchId);
// Lấy danh sách thống kê cầu thủ của trận đấu.
        List<MatchClubStat> clubStats = new ArrayList<>();
// Khởi tạo danh sách thống kê CLB.
        try {
            clubStats = matchClubStatService.getAllByMatchId(matchId);
// Lấy danh sách thống kê CLB của trận đấu.
        } catch (Exception e) {
// Bắt lỗi nếu có vấn đề khi lấy thống kê CLB (để tạm: in stack trace).
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Match match = matchService.getById(matchId);
// Lấy thông tin chi tiết về trận đấu.

        if (match.getMatchStartTime().isBefore(LocalDateTime.now())) {
// Kiểm tra xem trận đấu đã diễn ra chưa (thời gian bắt đầu trước thời gian hiện tại).
            model.addAttribute("isFixture", true);
// Nếu đã diễn ra, gửi cờ "isFixture=true" (tên biến nên là isPastMatch mới đúng ngữ cảnh).
        }

        model.addAttribute("isAdmin", isAdmin);
// Gửi cờ Admin sang giao diện.
        model.addAttribute("match", match);
// Gửi đối tượng Match sang giao diện.
        model.addAttribute("matchPlayerStats", playerStats);
// Gửi thống kê cầu thủ.
        model.addAttribute("clubStats", clubStats);
// Gửi thống kê CLB.
        model.addAttribute("startingLineupHome", matchService.getStartingLineup(matchId,
// Lấy đội hình chính thức của đội chủ nhà (giả định CLB đầu tiên trong list là Home).
                match.getMatchClubStats().get(0).getClub().getClubId().toString()));
        model.addAttribute("startingLineupAway", matchService.getStartingLineup(matchId,
// Lấy đội hình chính thức của đội khách (giả định CLB thứ hai trong list là Away).
                match.getMatchClubStats().get(1).getClub().getClubId().toString()));
        model.addAttribute("substitueLineupHome", matchService.getSubstitueLineup(matchId,
// Lấy đội hình dự bị của đội chủ nhà.
                match.getMatchClubStats().get(0).getClub().getClubId().toString()));
        model.addAttribute("substitueLineupAway", matchService.getSubstitueLineup(matchId,
// Lấy đội hình dự bị của đội khách.
                match.getMatchClubStats().get(1).getClub().getClubId().toString()));

        MatchPlayerStat mpsMOM = matchPlayerStatService
// Bắt đầu tìm kiếm Cầu thủ xuất sắc nhất trận (Man of the Match - MOTM).
                .getAllByMatchId(match.getMatchId().toString())
                .stream()
                .max(Comparator.comparing(MatchPlayerStat::getRating))
// Tìm MatchPlayerStat có Rating cao nhất.
                .orElse(null);
// Nếu không có thống kê nào, trả về null.
        model.addAttribute("motm", mpsMOM);
// Gửi đối tượng MOTM sang giao diện.
        model.addAttribute("comments", commentService.getAllCommentsByMatchId(matchId));
// Gửi danh sách bình luận của trận đấu.
        return "DetailMatch";
// Trả về view chi tiết trận đấu (DetailMatch.html).
    }

### 4. Phương thức Xem Lịch Thi Đấu Sắp Diễn Ra (Fixtures)

```java
    @GetMapping("/fixture")
    // Ánh xạ yêu cầu GET đến /match/fixture.
    public String getFixture(@RequestParam(name = "search", required = false) String search,
// Tham số tìm kiếm chung.
                             @RequestParam(name = "searchPlayerName", required = false) String searchPlayerName,
// Tham số tìm kiếm theo tên cầu thủ.
                             @RequestParam(name = "searchClubName", required = false) String searchClubName,
// Tham số tìm kiếm theo tên CLB.
                             @RequestParam(name = "season", required = false) String season,
// Tham số lọc theo mùa giải.
                             Model model) {
        List<Match> matcheList = matchService.getAll().stream()
// Lấy toàn bộ trận đấu từ DB và bắt đầu tạo Stream.
                .filter(m -> m.getMatchStartTime().compareTo(LocalDateTime.now()) > 0).toList();
// LỌC: Chỉ giữ lại các trận đấu CÓ THỜI GIAN BẮT ĐẦU LỚN HƠN THỜI GIAN HIỆN TẠI (trận sắp diễn ra).

        // Lọc theo mùa giải
        if (season != null && !season.isBlank()) {
// Các logic lọc tương tự như getAllMatch, áp dụng cho danh sách trận sắp diễn ra.
            try {
                int year = Integer.parseInt(season);
                matcheList = matcheList.stream()
                        .filter(m -> m.getMatchStartTime().getYear() == year)
                        .toList();
            } catch (NumberFormatException e) {
                // Nếu season không phải số, bỏ qua filter này
            }
        }

        if (search != null && !search.isBlank()) {
            matcheList = matcheList.stream().filter(m -> m.getMatchDescription().contains(search)).toList();
        }
        if (searchPlayerName != null && !searchPlayerName.isBlank()) {
            matcheList = matcheList.stream().filter(m -> m.getMatchPlayerStats().stream()
                    .anyMatch(mps -> mps.getPlayer().getPlayerFullName().contains(searchPlayerName))).toList();
        }
        if (searchClubName != null && !searchClubName.isBlank()) {
            matcheList = matcheList.stream().filter(m -> m.getMatchClubStats().stream()
                    .anyMatch(mcs -> mcs.getClub().getClubName().contains(searchClubName))).toList();
        }
        model.addAttribute("matches", matcheList);
// Đóng gói danh sách trận đấu đã lọc.
        model.addAttribute("searchPlayerName", searchPlayerName);
// Gửi lại giá trị tìm kiếm.
        model.addAttribute("searchClubName", searchClubName);
// Gửi lại giá trị tìm kiếm.
        model.addAttribute("search", search);
// Gửi lại giá trị tìm kiếm.
        model.addAttribute("season", season);
// Gửi lại giá trị mùa giải.

        return "Fixture";
// Trả về view lịch thi đấu (Fixture.html).
    }

### 5. Phương thức Thêm Cầu Thủ Xuất Sắc Nhất Trận (Admin Post)

```java
    @PostMapping("/add-motm")
    // Ánh xạ yêu cầu POST đến /match/add-motm (thường là từ form Admin).
    public String addManOfTheMatch(@RequestParam String matchId, @RequestParam String manOfTheMatch) {
// Nhận matchId (trận đấu) và manOfTheMatch (ID cầu thủ) từ form.
        Match match = matchService.getById(matchId);
// Lấy đối tượng Match.
        match.setMatchMOM(UUID.fromString(manOfTheMatch));
// Thiết lập MOTM cho trận đấu bằng cách chuyển ID cầu thủ sang UUID.
        matchService.save(match);
// Lưu thay đổi vào Database.
        return "redirect:/match/" + matchId;
// Chuyển hướng người dùng trở lại trang chi tiết trận đấu vừa cập nhật.
    }

### 6. Phương thức Thêm Highlight (Admin Post)

```java
    @PostMapping("/add-highlight")
    // Ánh xạ yêu cầu POST đến /match/add-highlight (thường là từ form Admin).
    public String addHighlight(@RequestParam String matchId,
// ID trận đấu.
                               @RequestParam int matchEventMinute,
// Phút diễn ra sự kiện Highlight.
                               @RequestParam String matchEventTitle,
// Tiêu đề của Highlight.
                               @RequestParam String vidUrl,
// URL video Highlight.
                               @RequestParam("hightlightThumnail") MultipartFile matchEventThumnails) {
// File thumbnail upload (MultipartFile).
        Match match = matchService.getById(matchId);
// Lấy đối tượng Match.
        MatchEvent event = new MatchEvent();
// Tạo đối tượng sự kiện mới.
        event.setMatch(match);
// Gán sự kiện cho trận đấu.
        event.setMatchEventMinute(matchEventMinute);
// Thiết lập phút.
        event.setMatchEventTitle(matchEventTitle);
// Thiết lập tiêu đề.
        event.setVidUrl(vidUrl);
// Thiết lập URL video.
        event.setMatchEventType(MatchEventType.Highlight); // Enum
// Thiết lập loại sự kiện là Highlight.

        if (!matchEventThumnails.isEmpty()) {
// Kiểm tra nếu có file thumbnail được upload.
            File imageFile = new File(
                    "src/main/resources/static/images/Storage-Files" + File.separator
// Định nghĩa đường dẫn lưu file trong thư mục static.
                            + matchEventThumnails.getOriginalFilename());
            try {
                Files.copy(matchEventThumnails.getInputStream(), imageFile.toPath(),
// Copy file upload vào thư mục tĩnh của dự án.
                        StandardCopyOption.REPLACE_EXISTING);
// Nếu file đã tồn tại, thay thế.
                event.setMatchEventThumnails("/images/Storage-Files/" + matchEventThumnails.getOriginalFilename());
// Lưu đường dẫn file thumbnail vào đối tượng sự kiện.
            } catch (IOException e) {
// Bắt lỗi nếu quá trình lưu file thất bại.
                e.printStackTrace(); // có thể log ra logger
            }
        }

        matchEventService.save(event);
// Lưu đối tượng MatchEvent vào Database.
        return "redirect:/match/" + matchId;
// Chuyển hướng về trang chi tiết trận đấu.
    }

### 7. Phương thức Import Đội Hình (Admin Post)

```java
    @PostMapping("/import-lineup")
    // Ánh xạ yêu cầu POST đến /match/import-lineup (thường là form upload file Excel).
    public String importLineup(@RequestParam("file") MultipartFile file,
// Nhận file Excel chứa thông tin đội hình.
                               @RequestParam("matchId") UUID matchId) {
// Nhận ID trận đấu.
        try (InputStream input = file.getInputStream()) {
// Mở Stream để đọc dữ liệu từ file upload.
            Workbook workbook = new XSSFWorkbook(input);
// Khởi tạo đối tượng Workbook (file Excel).
            Sheet sheet = workbook.getSheetAt(0);
// Lấy Sheet đầu tiên.

            for (Row row : sheet) {
// Bắt đầu vòng lặp duyệt qua từng hàng (Row) trong Sheet.
                if (row.getRowNum() == 0)
                    continue; // skip header
// Bỏ qua hàng đầu tiên (Header/Tiêu đề cột).

                String playerId = row.getCell(0).getStringCellValue();
// Lấy ID cầu thủ từ cột đầu tiên.
                boolean isStarter = false;
// Khởi tạo cờ đội hình chính (Starter).
                if (row.getCell(5).getStringCellValue().equals("Starter")) {
// Kiểm tra nếu giá trị ở cột thứ 6 là "Starter".
                    isStarter = true;
// Thiết lập cờ là đội hình chính.
                }

                Player player = playerService.getById(playerId);
// Lấy đối tượng Player từ DB.
                Match match = matchService.getById(matchId.toString());
// Lấy đối tượng Match.
                if (!match.getMatchClubStats().stream()
// Kiểm tra xem CLB của Cầu thủ có tham gia trận đấu này không.
                        .anyMatch(mcs -> mcs.getClub().getClubId().equals(player.getClub().getClubId()))) {
                    continue;
// Nếu không, bỏ qua cầu thủ này và chuyển sang hàng tiếp theo.
                }

                MatchPlayerStat stat = new MatchPlayerStat();
// Tạo đối tượng thống kê cầu thủ trong trận.
                stat.setMatch(match);
// Gán Match.
                stat.setPlayer(player);
// Gán Player.
                stat.setIsStarter(isStarter);
// Thiết lập Starter/Substitute.
                stat.setMatchPlayerGoal(0);
// Khởi tạo các thống kê cơ bản là 0.
                stat.setMatchPlayerAssist(0);
                stat.setMatchPlayerMinutedPlayed(0);
                stat.setMatchPlayerPass(0);
                stat.setMatchPlayerShoots(0);
                stat.setRating(0.00);
                matchPlayerStatService.save(stat);
// Lưu thống kê cầu thủ vào Database.
            }
            workbook.close();
// Đóng Workbook (giải phóng tài nguyên).
            return "redirect:/match/" + matchId + "?success";
// Chuyển hướng về trang chi tiết kèm thông báo thành công.
        } catch (Exception e) {
// Bắt lỗi nếu quá trình import thất bại.
            e.printStackTrace();
            return "redirect:/match/" + matchId + "?error";
// Chuyển hướng về trang chi tiết kèm thông báo lỗi.
        }
    }

### 8. Phương thức Đăng Bình Luận (User Post)

```java
    @PostMapping("/comment/{matchId}")
    // Ánh xạ yêu cầu POST đến /match/comment/{matchId} (form bình luận).
    public String postComment(@RequestParam(name = "commentContent") String commentContent,
// Nhận nội dung bình luận từ form.
                              @PathVariable(name = "matchId") String matchId, Principal principal) {
// Nhận ID trận đấu từ URL và thông tin người dùng.
        String username = principal.getName();
// Lấy username của người dùng đăng nhập.
        User user = userService.findByUsername(username);
// Lấy đối tượng User.
        Comment comment = new Comment();
// Tạo đối tượng Comment mới.
        comment.setMatch(matchService.getById(matchId));
// Gán trận đấu cho bình luận.
        comment.setCommentContent(commentContent);
// Thiết lập nội dung bình luận.
        comment.setUser(user);
// Gán người dùng đã bình luận.
        comment.setCommentDateCreated(LocalDateTime.now());
// Gán thời gian tạo bình luận là thời điểm hiện tại.
        commentService.save(comment);
// Lưu bình luận vào Database.

        return "redirect:/match/" + matchId;
// Chuyển hướng người dùng trở lại trang chi tiết trận đấu.
    }

}