
package com.trang.gachon.movie.config;

import com.trang.gachon.movie.entity.*;
import com.trang.gachon.movie.enums.*;
import com.trang.gachon.movie.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository        roleRepo;
    private final AccountRepository     accountRepo;
    private final EmployeeRepository    employeeRepo;
    private final BranchRepository      branchRepo;
    private final CinemaRoomRepository  cinemaRoomRepo;
    private final SeatRepository        seatRepo;
    private final TypeRepository        typeRepo;
    private final MovieRepository       movieRepo;
    private final ShowTimeRepository    showtimeRepo;
    private final ScheduleSeatRepository scheduleSeatRepo;
    private final MemberRepository      memberRepo;
    private final PromotionRepository   promotionRepo;
    private final InvoiceRepository     invoiceRepo;
    private final TicketRepository      ticketRepo;

    private static final long NORMAL_PRICE = 45_000L;
    private static final long VIP_PRICE    = 75_000L;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepo.count() > 0) {
            log.info("⏭️  Database đã có dữ liệu, bỏ qua seed.");
            return;
        }
        log.info("🌱 Bắt đầu seed dữ liệu...");

        List<Role> roles        = seedRoles();
        List<Account>     accounts     = seedAccounts(roles);
        seedEmployees(accounts);
        List<Branch>      branches     = seedBranches();
        List<CinemaRoom>  rooms        = seedCinemaRooms(branches);
        seedSeats(rooms);
        List<Type>        types        = seedTypes();
        List<Movie>       movies       = seedMovies();
        seedMovieTypes(movies, types);
        List<Showtime>    showtimes    = seedShowtimes(movies, rooms);
        List<ScheduleSeat> ssSeats     = seedScheduleSeats(showtimes);
        seedMembers(accounts);
        List<Promotion>   promotions   = seedPromotions();
        seedInvoicesAndTickets(accounts, promotions, showtimes, ssSeats);

        log.info("✅ Seed dữ liệu hoàn tất!");
    }

    // ═══════════════════════════════════════════════
    // ROLE
    // ═══════════════════════════════════════════════
    private List<Role> seedRoles() {
        var list = List.of(
                Role.builder().roleName("ADMIN").build(),
                Role.builder().roleName("EMPLOYEE").build(),
                Role.builder().roleName("MEMBER").build()
        );
        var saved = roleRepo.saveAll(list);
        log.info("  ✔ roles: {}", saved.size());
        return saved;
    }

    // ═══════════════════════════════════════════════
    // ACCOUNT
    // ═══════════════════════════════════════════════
    private List<Account> seedAccounts(List<Role> roles) {
        Role admin    = roles.get(0);
        Role staff    = roles.get(1);
        Role customer = roles.get(2);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


        var list = List.of(
                account("admin",  "tranggd121@gmail.com",  passwordEncoder.encode("admin123"),
                        "Nguyễn Văn Admin",    "0901234567", "001099012345",
                        "Nam",  LocalDate.of(1985, 1, 15),  "Hà Nội",    admin),

                account("staff1", "staff1@cinema.vn", passwordEncoder.encode("staff123"),
                        "Trần Thị Nhân Viên", "0912345678", "001099023456",
                        "Nữ",   LocalDate.of(1992, 3, 20),  "Hà Nội",    staff),

                account("staff2", "staff2@cinema.vn", passwordEncoder.encode("staff123"),
                        "Lê Văn Nhân Viên",   "0923456789", "001099034567",
                        "Nam",  LocalDate.of(1993, 7, 10),  "TP.HCM",    staff),

                account("user1",  "user1@gmail.com",  passwordEncoder.encode("user123"),
                        "Phạm Thị Lan",       "0934567890", "001099045678",
                        "Nữ",   LocalDate.of(1995, 11, 5),  "Hà Nội",    customer),

                account("user2",  "user2@gmail.com",
                        passwordEncoder.encode("user123"),
                        "Hoàng Văn Minh",     "0945678901", "001099056789",
                        "Nam",  LocalDate.of(1998, 6, 22),  "TP.HCM",    customer),

                account("user3",  "user3@gmail.com",  passwordEncoder.encode("user123"),
                        "Vũ Thị Hoa",         "0956789012", "001099067890",
                        "Nữ",   LocalDate.of(2000, 2, 14),  "Đà Nẵng",   customer)
        );
        var saved = accountRepo.saveAll(list);
        log.info("  ✔ accounts: {}", saved.size());
        return saved;
    }

    private Account account(String username, String email, String password,
                            String fullName, String phone, String idCard,
                            String gender, LocalDate dob, String address, Role role) {
        return Account.builder()
                .userName(username)
                .email(email)
                .password(password)
                .fullName(fullName)
                .phoneNumber(phone)
                .identityCard(idCard)
                .gender(gender)
                .dateOfBirth(dob)
                .registerDate(LocalDate.now())
                .accountStatus(AccountStatus.ACTIVE)
                .address(address)
                .role(role)
                .build();
    }

    // ═══════════════════════════════════════════════
    // EMPLOYEE
    // ═══════════════════════════════════════════════
    private void seedEmployees(List<Account> accounts) {
        var list = List.of(
                Employee.builder().account(accounts.get(1)).build(),
                Employee.builder().account(accounts.get(2)).build()
        );
        employeeRepo.saveAll(list);
        log.info("  ✔ employees: {}", list.size());
    }

    // ═══════════════════════════════════════════════
    // BRANCH
    // ═══════════════════════════════════════════════
    private List<Branch> seedBranches() {

        List<Branch> list = new ArrayList<>(List.of(
                branch("Chi nhánh Hà Nội - Hoàn Kiếm",
                        "123 Đinh Tiên Hoàng, Hoàn Kiếm, Hà Nội",
                        "024-38225566", "hanoi.jpg",
                        "https://maps.google.com/?q=HoanKiem"),

                branch("Chi nhánh TP.HCM - Quận 1",
                        "456 Nguyễn Huệ, Quận 1, TP.HCM",
                        "028-38225577", "hcm.jpg",
                        "https://maps.google.com/?q=Q1HCM"),

                branch("Chi nhánh Đà Nẵng",
                        "789 Bạch Đằng, Hải Châu, Đà Nẵng",
                        "0236-3822588", "dn.jpg",
                        "https://maps.google.com/?q=DaNang")
        ));

        var saved = branchRepo.saveAll(list);
        log.info("✔ branches: {}", saved.size());

        return saved;
    }

    private Branch branch(String name, String address, String phone,
                          String image, String mapUrl) {
        return Branch.builder()
                .branchName(name)
                .address(address)
                .phone(phone)
                .branchStatus(BranchStatus.ACTIVE)
                .image(image)
                .mapEmbedUrl(mapUrl)
                .build();
    }

    // ═══════════════════════════════════════════════
    // CINEMA ROOM
    // ═══════════════════════════════════════════════
    private List<CinemaRoom> seedCinemaRooms(List<Branch> branches) {
        var list = List.of(
                cinemaRoom("Phòng 2D - Hà Nội",   branches.get(0), 80),
                cinemaRoom("Phòng 3D IMAX - HN",  branches.get(0), 120),
                cinemaRoom("Phòng 2D - HCM",      branches.get(1), 80),
                cinemaRoom("Phòng 4DX - HCM",     branches.get(1), 100),
                cinemaRoom("Phòng 2D - Đà Nẵng",  branches.get(2), 60)
        );
        var saved = cinemaRoomRepo.saveAll(list);
        log.info("  ✔ cinemaRooms: {}", saved.size());
        return saved;
    }

    private CinemaRoom cinemaRoom(String name, Branch branch, int seatQty) {
        return CinemaRoom.builder()
                .cinemaRoomName(name)
                .branch(branch)
                .seatQuantity(seatQty)
                .build();
    }

    // ═══════════════════════════════════════════════
    // SEAT  — sinh đúng theo seatQuantity của từng phòng
    // ═══════════════════════════════════════════════
    private void seedSeats(List<CinemaRoom> rooms) {
        var seats = new java.util.ArrayList<Seat>();
        for (CinemaRoom room : rooms) {
            int seatsPerRow = 10;
            int totalSeats = room.getSeatQuantity();
            int rowCount = (int) Math.ceil(totalSeats / (double) seatsPerRow);
            int vipStartRow = Math.max(0, rowCount - 2);
            int created = 0;

            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                SeatType seatType = rowIndex >= vipStartRow ? SeatType.VIP : SeatType.NORMAL;
                for (int col = 1; col <= seatsPerRow && created < totalSeats; col++) {
                    seats.add(Seat.builder()
                            .seatRow(rowLabel(rowIndex))
                            .seatColumn(col)
                            .cinemaRoom(room)
                            .seatType(seatType)
                            .build());
                    created++;
                }
            }
        }
        seatRepo.saveAll(seats);
        log.info("  ✔ seats: {}", seats.size());
    }

    // ═══════════════════════════════════════════════
    // TYPE
    // ═══════════════════════════════════════════════
    private List<Type> seedTypes() {
        var list = List.of(
                Type.builder().typeName(MovieGenre.ACTION.getDisplayName())    .genre(MovieGenre.ACTION)    .build(),
                Type.builder().typeName(MovieGenre.ROMANCE.getDisplayName())   .genre(MovieGenre.ROMANCE)   .build(),
                Type.builder().typeName(MovieGenre.HORROR.getDisplayName())    .genre(MovieGenre.HORROR)    .build(),
                Type.builder().typeName(MovieGenre.ANIMATION.getDisplayName()) .genre(MovieGenre.ANIMATION) .build(),
                Type.builder().typeName(MovieGenre.SCI_FI.getDisplayName())    .genre(MovieGenre.SCI_FI)    .build()
        );
        var saved = typeRepo.saveAll(list);
        log.info("  ✔ types: {}", saved.size());
        return saved;
    }


    // ═══════════════════════════════════════════════
    // MOVIE
    // ═══════════════════════════════════════════════
    private List<Movie> seedMovies() {
        LocalDate today = LocalDate.now();

        var list = List.of(
                Movie.builder()
                        .movieNameVn("Avengers: Hồi Kết")
                        .movieNameEnglish("Avengers: Endgame")
                        .duration(181)
                        .director("Anthony Russo, Joe Russo")
                        .actor("Robert Downey Jr., Chris Evans, Scarlett Johansson")
                        .content("Sau cú búng tay của Thanos, những Avengers còn lại tập hợp cho nhiệm vụ cuối cùng nhằm đảo ngược thảm họa và cứu lấy vũ trụ.")
                        .movieProductionCompany("Marvel Studios")
                        .largeImage("https://upload.wikimedia.org/wikipedia/en/0/0d/Avengers_Endgame_poster.jpg")
                        .smallImage("https://upload.wikimedia.org/wikipedia/en/0/0d/Avengers_Endgame_poster.jpg")
                        .bannerImage("https://upload.wikimedia.org/wikipedia/en/0/0d/Avengers_Endgame_poster.jpg")
                        .featuredOnHome(true)
                        .version("2D/3D IMAX")
                        .fromDate(today.minusDays(5))
                        .toDate(today.plusDays(20))
                        .trailer("https://www.marvel.com/watch/trailers-and-extras/marvel-studios-avengers-endgame-offical-trailer-1")
                        .reviewLink("https://www.rottentomatoes.com/m/avengers_endgame")
                        .build(),

                Movie.builder()
                        .movieNameVn("Dune: Hành Tinh Cát - Phần Hai")
                        .movieNameEnglish("Dune: Part Two")
                        .duration(166)
                        .director("Denis Villeneuve")
                        .actor("Timothée Chalamet, Zendaya, Rebecca Ferguson")
                        .content("Paul Atreides liên minh với Chani và người Fremen để trả thù những kẻ đã hủy diệt gia tộc mình, đồng thời đối mặt với lời tiên tri về tương lai của Arrakis.")
                        .movieProductionCompany("Legendary Pictures")
                        .largeImage("https://upload.wikimedia.org/wikipedia/en/5/52/Dune_Part_Two_poster.jpeg")
                        .smallImage("https://upload.wikimedia.org/wikipedia/en/5/52/Dune_Part_Two_poster.jpeg")
                        .bannerImage("https://upload.wikimedia.org/wikipedia/en/5/52/Dune_Part_Two_poster.jpeg")
                        .version("2D IMAX")
                        .fromDate(today.minusDays(2))
                        .toDate(today.plusDays(18))
                        .trailer("https://www.movieinsider.com/m20138/dune-part-two/videos/17743")
                        .reviewLink("https://www.rottentomatoes.com/m/dune_part_two")
                        .build(),

                Movie.builder()
                        .movieNameVn("Những Mảnh Ghép Cảm Xúc 2")
                        .movieNameEnglish("Inside Out 2")
                        .duration(96)
                        .director("Kelsey Mann")
                        .actor("Amy Poehler, Maya Hawke, Phyllis Smith")
                        .content("Riley bước vào tuổi teen và bộ cảm xúc quen thuộc phải đối diện với những cảm xúc mới, khiến mọi thứ trong đầu cô bé trở nên hỗn loạn hơn bao giờ hết.")
                        .movieProductionCompany("Pixar Animation Studios")
                        .largeImage("https://upload.wikimedia.org/wikipedia/en/f/f7/Inside_Out_2_poster.jpg")
                        .smallImage("https://upload.wikimedia.org/wikipedia/en/f/f7/Inside_Out_2_poster.jpg")
                        .bannerImage("https://upload.wikimedia.org/wikipedia/en/f/f7/Inside_Out_2_poster.jpg")
                        .version("2D/3D")
                        .fromDate(today.minusDays(1))
                        .toDate(today.plusDays(28))
                        .trailer("https://movies.disney.com/inside-out-2")
                        .reviewLink("https://www.rottentomatoes.com/m/inside_out_2")
                        .build()
        );
        var saved = movieRepo.saveAll(list);
        log.info("  ✔ movies: {}", saved.size());
        return saved;
    }

    // ═══════════════════════════════════════════════
    // MOVIE_TYPE  (many-to-many)
    // ═══════════════════════════════════════════════
    private void seedMovieTypes(List<Movie> movies, List<Type> types) {
        // Avengers: Hành động + SciFi
        movies.get(0).setTypes(new ArrayList<>(List.of(types.get(0), types.get(4))));

        // Dune: Hành động + SciFi
        movies.get(1).setTypes(new ArrayList<>(List.of(types.get(0), types.get(4))));

        // Inside Out 2: Hoạt hình
        movies.get(2).setTypes(new ArrayList<>(List.of(types.get(3))));

        movieRepo.saveAll(movies);
        log.info("  ✔ movieTypes seeded");
    }

    // ═══════════════════════════════════════════════
    // SHOWTIME
    // ═══════════════════════════════════════════════
    private List<Showtime> seedShowtimes(List<Movie> movies, List<CinemaRoom> rooms) {
        LocalDate today = LocalDate.now();

        List<Showtime> list = new ArrayList<>(List.of(
                showtime(today,             movies.get(0), rooms.get(0), LocalTime.of(9, 0)),
                showtime(today,             movies.get(0), rooms.get(1), LocalTime.of(14, 0)),
                showtime(today.plusDays(1), movies.get(1), rooms.get(2), LocalTime.of(10, 30)),
                showtime(today.plusDays(1), movies.get(1), rooms.get(3), LocalTime.of(16, 0)),
                showtime(today.plusDays(2), movies.get(0), rooms.get(0), LocalTime.of(18, 0)),
                showtime(today.plusDays(7), movies.get(2), rooms.get(4), LocalTime.of(14, 0)),
                showtime(today.plusDays(8), movies.get(2), rooms.get(4), LocalTime.of(20, 0))
        ));

        var saved = showtimeRepo.saveAll(list);
        log.info("  ✔ showtimes: {}", saved.size());
        return saved;
    }

    private Showtime showtime(LocalDate date, Movie movie,
                              CinemaRoom room, LocalTime time) {
        return Showtime.builder()
                .showDate(date)
                .movie(movie)
                .cinemaRoom(room)
                .showTime(time)  
                .build();
    }
    
    // ═══════════════════════════════════════════════
    // SCHEDULE SEAT  — tạo trạng thái cho toàn bộ ghế trong phòng theo từng suất chiếu
    // ═══════════════════════════════════════════════
    private List<ScheduleSeat> seedScheduleSeats(List<Showtime> showtimes) {
        var all = new java.util.ArrayList<ScheduleSeat>();
        for (Showtime st : showtimes) {
            List<Seat> seats = seatRepo
                    .findByCinemaRoom_CinemaRoomIdOrderBySeatRowAscSeatColumnAsc(st.getCinemaRoom().getCinemaRoomId());
            for (Seat seat : seats) {
                all.add(ScheduleSeat.builder()
                        .showtime(st)
                        .seat(seat)
                        .seatStatus(SeatStatus.AVAILABLE)
                        .build());
            }
        }
        var saved = scheduleSeatRepo.saveAll(all);
        log.info("  ✔ scheduleSeats: {}", saved.size());
        return saved;
    }

    // ═══════════════════════════════════════════════
    // MEMBER
    // ═══════════════════════════════════════════════
    private void seedMembers(List<Account> accounts) {
        var list = List.of(
                Member.builder().account(accounts.get(3)).memberCode("MEM0001").score(18_000L).build(),
                Member.builder().account(accounts.get(4)).memberCode("MEM0002").score(12_000L).build(),
                Member.builder().account(accounts.get(5)).memberCode("MEM0003").score(7_500L).build()
        );
        memberRepo.saveAll(list);
        log.info("  ✔ members: {}", list.size());
    }

    // ═══════════════════════════════════════════════
    // PROMOTION
    // ═══════════════════════════════════════════════
    private List<Promotion> seedPromotions() {
        LocalDateTime now = LocalDateTime.now();

        var list = List.of(
                promotion("Giảm 20.000đ cuối tuần",
                        "Ưu đãi áp dụng cho các suất chiếu cuối tuần đang mở bán.",
                        20_000L,
                        now.minusDays(2),
                        now.plusDays(10),
                        "promo_le.jpg"),

                promotion("Thành viên giảm 15.000đ",
                        "Áp dụng cho thành viên khi mua vé trong tháng này.",
                        15_000L,
                        now.minusDays(15),
                        now.plusDays(20),
                        "promo_t2.jpg"),

                promotion("Sắp ra mắt Lật Mặt 8",
                        "Ưu đãi đặt sớm cho phim sắp chiếu.",
                        30_000L,
                        now.plusDays(5),
                        now.plusDays(25),
                        "promo_bday.jpg")
        );
        var saved = promotionRepo.saveAll(list);
        log.info("  ✔ promotions: {}", saved.size());
        return saved;
    }

    private Promotion promotion(String title, String detail, Long discount,
                                LocalDateTime start, LocalDateTime end, String image) {
        return Promotion.builder()
                .title(title)
                .detail(detail)
                .discountLevel(discount)
                .startTime(start)
                .endTime(end)
                .image(image)
                .build();
    }

    // ═══════════════════════════════════════════════
    // INVOICE + TICKET
    // ═══════════════════════════════════════════════
    private void seedInvoicesAndTickets(List<Account> accounts,
                                        List<Promotion> promotions,
                                        List<Showtime> showtimes,
                                        List<ScheduleSeat> scheduleSeats) {
        Map<Long, List<ScheduleSeat>> seatsByShowtime = scheduleSeats.stream()
                .collect(Collectors.groupingBy(
                        ss -> ss.getShowtime().getShowTimeId(),
                        LinkedHashMap::new,
                        Collectors.toCollection(ArrayList::new)
                ));

        Account admin  = accounts.get(0);
        Account staff1 = accounts.get(1);
        Account staff2 = accounts.get(2);
        Account user1  = accounts.get(3);
        Account user2  = accounts.get(4);
        Account user3  = accounts.get(5);

        List<Invoice> invoices = new ArrayList<>();
        List<Ticket> tickets = new ArrayList<>();

        invoices.add(buildInvoiceWithTickets(
                "INV-001",
                showtimes.get(0),
                takeSeats(seatsByShowtime, showtimes.get(0), 2),
                InvoiceStatus.WAITING,
                user1,
                null,
                promotions.get(0),
                10_000L,
                0L,
                LocalDateTime.now().minusDays(1),
                tickets
        ));

        invoices.add(buildInvoiceWithTickets(
                "INV-002",
                showtimes.get(1),
                takeSeats(seatsByShowtime, showtimes.get(1), 2),
                InvoiceStatus.CONFIRMED,
                null,
                staff1,
                promotions.get(1),
                0L,
                0L,
                LocalDateTime.now().minusHours(6),
                tickets
        ));

        invoices.add(buildInvoiceWithTickets(
                "INV-003",
                showtimes.get(2),
                takeSeats(seatsByShowtime, showtimes.get(2), 1),
                InvoiceStatus.CONFIRMED,
                user2,
                staff2,
                null,
                4_500L,
                0L,
                LocalDateTime.now().minusHours(3),
                tickets
        ));

        invoices.add(buildInvoiceWithTickets(
                "INV-004",
                showtimes.get(4),
                takeSeats(seatsByShowtime, showtimes.get(4), 1),
                InvoiceStatus.CANCELLED,
                user3,
                admin,
                promotions.get(0),
                0L,
                0L,
                LocalDateTime.now().minusHours(1),
                tickets
        ));

        invoiceRepo.saveAll(invoices);
        scheduleSeatRepo.saveAll(scheduleSeats);
        ticketRepo.saveAll(tickets);

        log.info("  ✔ invoices: {}", invoices.size());
        log.info("  ✔ tickets: {}", tickets.size());
    }

    private Invoice buildInvoiceWithTickets(String code,
                                            Showtime showtime,
                                            List<ScheduleSeat> selectedSeats,
                                            InvoiceStatus status,
                                            Account account,
                                            Account handledBy,
                                            Promotion promotion,
                                            Long addScore,
                                            Long useScore,
                                            LocalDateTime bookingDate,
                                            List<Ticket> tickets) {
        String seatSnapshot = selectedSeats.stream()
                .map(ss -> ss.getSeat().getSeatRow() + ss.getSeat().getSeatColumn())
                .collect(Collectors.joining(" "));

        long subTotal = selectedSeats.stream()
                .mapToLong(ss -> ss.getSeat().getSeatType() == SeatType.VIP ? VIP_PRICE : NORMAL_PRICE)
                .sum();

        long discount = promotion != null && promotion.isValid()
                ? promotion.getDiscountLevel()
                : 0L;

        long totalMoney = Math.max(0L, subTotal - discount - useScore);

        Invoice invoice = Invoice.builder()
                .bookingCode(code)
                .movieName(showtime.getMovie().getMovieNameVn() != null
                        ? showtime.getMovie().getMovieNameVn()
                        : showtime.getMovie().getMovieNameEnglish())
                .cinemaRoom(showtime.getCinemaRoom().getCinemaRoomName())
                .scheduleShow(showtime.getShowDate())
                .scheduleShowTime(String.valueOf(showtime.getShowTime()))
                .seat(seatSnapshot)
                .bookingDate(bookingDate)
                .invoiceStatus(status)
                .totalMoney(totalMoney)
                .addScore(addScore)
                .useScore(useScore)
                .account(account)
                .handledBy(handledBy)
                .promotion(promotion)
                .build();

        for (ScheduleSeat scheduleSeat : selectedSeats) {
            tickets.add(Ticket.builder()
                    .invoice(invoice)
                    .scheduleSeat(scheduleSeat)
                    .ticketType(scheduleSeat.getSeat().getSeatType() == SeatType.VIP
                            ? TicketType.VIP
                            : TicketType.NORMAL)
                    .price(scheduleSeat.getSeat().getSeatType() == SeatType.VIP
                            ? VIP_PRICE
                            : NORMAL_PRICE)
                    .build());

            scheduleSeat.setSeatStatus(status == InvoiceStatus.CANCELLED
                    ? SeatStatus.AVAILABLE
                    : SeatStatus.SOLD);
        }

        return invoice;
    }

    private List<ScheduleSeat> takeSeats(Map<Long, List<ScheduleSeat>> seatsByShowtime,
                                         Showtime showtime,
                                         int count) {
        List<ScheduleSeat> seats = seatsByShowtime.getOrDefault(showtime.getShowTimeId(), List.of());
        if (seats.size() < count) {
            throw new IllegalStateException("Không đủ ghế seed cho showtime " + showtime.getShowTimeId());
        }
        return new ArrayList<>(seats.subList(0, count));
    }

    private String rowLabel(int index) {
        StringBuilder label = new StringBuilder();
        int value = index;
        do {
            label.insert(0, (char) ('A' + (value % 26)));
            value = (value / 26) - 1;
        } while (value >= 0);
        return label.toString();
    }
}
