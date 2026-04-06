package com.trang.gachon.movie.controller;

import com.trang.gachon.movie.dto.TicketSellRequest;
import com.trang.gachon.movie.entity.*;
import com.trang.gachon.movie.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final MovieService movieService;
    private final BranchService branchService;
    private final ShowTimeService showTimeService;
    private final TicketService ticketService;
    private final PromotionService promotionService;

    @GetMapping({"", "/"})
    public String ticketHome() {
        return "redirect:/ticket/select-movie";
    }

    @GetMapping("/select-movie")
    public String selectMovie(Model model) {
        model.addAttribute("movies", movieService.getNowShowingMovies());
        model.addAttribute("branches", branchService.getActiveBranches());
        return "ticket/select-movie";
    }

    @GetMapping("/select-showtime")
    public String selectShowtime(
            @RequestParam Long movieId,
            @RequestParam Long branchId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        if (date == null) {
            date = LocalDate.now();
        }

        model.addAttribute("movie", movieService.getById(movieId));
        model.addAttribute("branch", branchService.getById(branchId));
        model.addAttribute("showtimes",
                showTimeService.getShowTimesByMovieAndBranchAndDate(movieId, branchId, date));
        model.addAttribute("selectedDate", date);
        model.addAttribute("movieId", movieId);
        model.addAttribute("branchId", branchId);
        model.addAttribute("dateRange",
                java.util.stream.IntStream.range(0, 7)
                        .mapToObj(LocalDate.now()::plusDays)
                        .toList());

        return "ticket/select-showtime";
    }

    @GetMapping("/select-seat")
    public String selectSeat(@RequestParam Long showtimeId, Model model) {
        Showtime showtime = showTimeService.getShowTimeById(showtimeId);
        List<Seat> allSeats = ticketService.getSeatsByRoom(
                showtime.getCinemaRoom().getCinemaRoomId());
        List<ScheduleSeat> scheduleSeats = ticketService.getSeatMap(showtimeId);

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

        model.addAttribute("showtime", showtime);
        model.addAttribute("allSeats", allSeats);
        model.addAttribute("scheduleSeats", scheduleSeats);
        model.addAttribute("seatStatusMap", seatStatusMap);
        model.addAttribute("seatsByRow", seatsByRow);
        model.addAttribute("promotions", promotionService.getActivePromotions());
        model.addAttribute("normalPrice", 45_000L);
        model.addAttribute("vipPrice", 75_000L);
        return "ticket/select-seat";
    }

    @GetMapping("/find-member")
    @ResponseBody
    public Map<String, Object> findMember(@RequestParam String keyword) {
        return ticketService.findMember(keyword)
                .map(member -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("memberCode", member.getMemberCode());
                    result.put("fullName", member.getAccount().getFullName());
                    result.put("score", member.getScore());
                    return result;
                })
                .orElseGet(LinkedHashMap::new);
    }

    @PostMapping("/confirm")
    public String confirmTicket(
            @ModelAttribute TicketSellRequest request,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            Invoice invoice = ticketService.sellTicket(request, authentication.getName());
            return "redirect:/ticket/receipt/" + invoice.getBookingCode();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/ticket/select-seat?showtimeId=" + request.getShowtimeId();
        }
    }

    @GetMapping("/receipt/{code}")
    public String receipt(@PathVariable String code, Model model) {
        Invoice invoice = ticketService.getByBookingCode(code);
        model.addAttribute("invoice", invoice);
        model.addAttribute("tickets", ticketService.getTicketsByInvoice(invoice.getInvoiceId()));
        return "ticket/receipt";
    }
}
