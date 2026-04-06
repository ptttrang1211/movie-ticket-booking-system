package com.trang.gachon.movie.controller;

import com.trang.gachon.movie.entity.Invoice;
import com.trang.gachon.movie.service.BookingManageService;
import com.trang.gachon.movie.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/booking-manage")
@RequiredArgsConstructor
public class BookingManageController {

    private final BookingManageService bookingManageService;
    private final TicketService        ticketService;

    // ============================================================
    // GET /booking-manage — entry point từ home/menu
    // ============================================================
    @GetMapping({"", "/"})
    public String home() {
        return "redirect:/booking-manage/list";
    }

    // ============================================================
    // GET /booking-manage/list — Danh sách booking
    // ============================================================
    @GetMapping("/list")
    public String list(
            @RequestParam(required = false) String keyword,
            Model model) {

        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("invoices", bookingManageService.search(keyword));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("invoices", bookingManageService.getAll());
        }
        return "booking-manage/list";
    }

    // ============================================================
    // GET /booking-manage/detail/{id} — Chi tiết booking
    // ============================================================
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Invoice invoice = bookingManageService.getById(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("tickets",
                ticketService.getTicketsByInvoice(id));
        return "booking-manage/detail";
    }

    // ============================================================
    // POST /booking-manage/confirm/{id} — Xác nhận vé WAITING → CONFIRMED
    // ============================================================
    @PostMapping("/confirm/{id}")
    public String confirm(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Invoice invoice = bookingManageService.confirmBooking(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMsg",
                    "Xác nhận vé thành công. Có thể in vé cho khách ngay.");
            return "redirect:/booking-manage/receipt/" + invoice.getInvoiceId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/booking-manage/detail/" + id;
    }

    // ============================================================
    // GET /booking-manage/receipt/{id} — In vé cho booking online đã xác nhận
    // ============================================================
    @GetMapping("/receipt/{id}")
    public String receipt(@PathVariable Long id, Model model) {
        Invoice invoice = bookingManageService.getById(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("tickets", ticketService.getTicketsByInvoice(id));
        return "booking-manage/receipt";
    }

    // ============================================================
    // POST /booking-manage/cancel/{id} — Hủy vé
    // ============================================================
    @PostMapping("/cancel/{id}")
    public String cancel(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            bookingManageService.cancelBooking(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMsg", "Đã hủy vé.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/booking-manage/detail/" + id;
    }
}
