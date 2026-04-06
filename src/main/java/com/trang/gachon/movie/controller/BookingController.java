package com.trang.gachon.movie.controller;

import com.trang.gachon.movie.dto.BookingRequest;
import com.trang.gachon.movie.entity.*;
import com.trang.gachon.movie.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final MovieService      movieService;
    private final BranchService     branchService;
    private final ShowTimeService   showtimeService;
    private final BookingService    bookingService;
    private final PromotionService  promotionService;

    // ============================================================
    // BƯỚC 1 — Chọn chi nhánh
    // GET /booking/select-branch?movieId=1
    // ============================================================
    @GetMapping("/select-branch")
    public String selectBranch(
            @RequestParam Long movieId,
            Model model) {

        model.addAttribute("movie",    movieService.getById(movieId));
        // Chỉ show chi nhánh nào thực sự có lịch chiếu cho phim này,
        // để member không bị dẫn vào một bước chọn suất chiếu rỗng.
        model.addAttribute("branches", branchService.getActiveBranchesByMovie(movieId));
        return "booking/select-branch";
    }

    // ============================================================
    // BƯỚC 2 — Chọn ngày + suất chiếu
    // GET /booking/select-showtime?movieId=1&branchId=1&date=2025-01-12
    // ============================================================
    @GetMapping("/select-showtime")
    public String selectShowtime(
            @RequestParam Long movieId,
            @RequestParam Long branchId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        if (date == null) date = LocalDate.now();

        // Lấy suất chiếu theo phim + chi nhánh + ngày
        List<Showtime> showtimes = showtimeService
                .getShowTimesByMovieAndBranchAndDate(movieId, branchId, date);

        model.addAttribute("movie",      movieService.getById(movieId));
        model.addAttribute("branch",     branchService.getById(branchId));
        model.addAttribute("showtimes",  showtimes);
        model.addAttribute("selectedDate", date);
        model.addAttribute("movieId",    movieId);
        model.addAttribute("branchId",   branchId);

        // 7 ngày tới để chọn
        model.addAttribute("dateRange",
                java.util.stream.IntStream.range(0, 7)
                        .mapToObj(LocalDate.now()::plusDays)
                        .toList());

        return "booking/select-showtime";
    }

    // ============================================================
    // BƯỚC 3 — Sơ đồ ghế
    // GET /booking/select-seat?showtimeId=1
    // ============================================================
    @GetMapping("/select-seat")
    public String selectSeat(
            @RequestParam Long showtimeId,
            Model model) {

        Showtime showtime = showtimeService.getShowTimeById(showtimeId);

        // Lấy tất cả ghế của phòng
        List<Seat> allSeats = bookingService.getSeatsByRoom(
                showtime.getCinemaRoom().getCinemaRoomId());

        // Lấy trạng thái ghế theo suất chiếu
        List<ScheduleSeat> scheduleSeats =
                bookingService.getSeatMap(showtimeId);

        Map<Long, String> seatStatusMap = scheduleSeats.stream()
                .collect(Collectors.toMap(
                        ss -> ss.getSeat().getSeatId(),
                        ss -> ss.getSeatStatus().name(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        Map<String, List<Seat>> seatsByRow = allSeats.stream()
                .collect(Collectors.groupingBy(
                        Seat::getSeatRow,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // KM đang có hiệu lực
        List<Promotion> promotions = promotionService.getActivePromotions();

        model.addAttribute("showtime",     showtime);
        model.addAttribute("allSeats",     allSeats);
        model.addAttribute("scheduleSeats",scheduleSeats);
        model.addAttribute("seatStatusMap", seatStatusMap);
        model.addAttribute("seatsByRow",   seatsByRow);
        model.addAttribute("promotions",   promotions);
        model.addAttribute("normalPrice",  45000L);
        model.addAttribute("vipPrice",     75000L);
        return "booking/select-seat";
    }

    // ============================================================
    // BƯỚC 4 — Xác nhận đặt vé
    // POST /booking/confirm
    // ============================================================
    @PostMapping("/confirm")
    public String confirmBooking(
            @ModelAttribute BookingRequest req,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Invoice invoice = bookingService.confirmBooking(
                    req, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("invoice", invoice);
            return "redirect:/booking/ticket/" + invoice.getBookingCode();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/booking/select-seat?showtimeId=" + req.getShowtimeId();
        }
    }

    // ============================================================
    // BƯỚC 5 — Thông tin vé sau đặt
    // GET /booking/ticket/{code}
    // ============================================================
    @GetMapping("/ticket/{code}")
    public String ticketInfo(
            @PathVariable String code,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Invoice invoice = bookingService.getByBookingCode(code);

        // Kiểm tra vé thuộc về user đang login
        if (!invoice.getAccount().getUserName().equals(userDetails.getUsername())) {
            return "redirect:/auth/403";
        }

        model.addAttribute("invoice", invoice);
        model.addAttribute("tickets", bookingService.getTicketsByInvoice(
                invoice.getInvoiceId()));
        return "booking/ticket-info";
    }
}
