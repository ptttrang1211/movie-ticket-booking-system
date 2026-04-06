package com.trang.gachon.movie.controller;

import com.trang.gachon.movie.dto.AccountUpdateRequest;
import com.trang.gachon.movie.entity.Account;
import com.trang.gachon.movie.entity.Member;
import com.trang.gachon.movie.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // ============================================================
    // GET /account — entry point chung cho mọi role đã đăng nhập
    // ============================================================
    @GetMapping
    public String accountHome() {
        return "redirect:/account/info";
    }

    // ============================================================
    // GET /account/info — Thông tin tài khoản
    // ============================================================
    @GetMapping("/info")
    public String info(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Account account = accountService.getByUsername(userDetails.getUsername());
        Optional<Member> member = accountService.findMemberByAccountId(account.getAccountId());

        model.addAttribute("account", account);
        model.addAttribute("member", member.orElse(null));

        // Bind vào form update
        AccountUpdateRequest req = new AccountUpdateRequest();
        req.setFullName(account.getFullName());
        req.setEmail(account.getEmail());
        req.setPhoneNumber(account.getPhoneNumber());
        req.setGender(account.getGender());
        req.setAddress(account.getAddress());
        if (account.getDateOfBirth() != null)
            req.setDateOfBirth(account.getDateOfBirth());
        model.addAttribute("updateRequest", req);

        return "account/info";
    }

    // ============================================================
    // POST /account/update — Cập nhật thông tin
    // ============================================================
    @PostMapping("/update")
    public String update(
            @Valid @ModelAttribute("updateRequest") AccountUpdateRequest req,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            Account account = accountService.getByUsername(userDetails.getUsername());
            model.addAttribute("account", account);
            model.addAttribute("member",
                    accountService.findMemberByAccountId(account.getAccountId()).orElse(null));
            return "account/info";
        }

        try {
            accountService.updateProfile(userDetails.getUsername(), req);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/account/info";
    }

    // ============================================================
    // GET /account/booked-ticket — Lịch sử đặt vé
    // ============================================================
    @GetMapping("/booked-ticket")
    public String bookedTicket(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Account account = accountService.getByUsername(userDetails.getUsername());
        model.addAttribute("invoices",
                accountService.getBookingHistory(account.getAccountId()));
        return "account/booked-ticket";
    }

    // ============================================================
    // GET /account/score-history — Lịch sử điểm
    // ============================================================
    @GetMapping("/score-history")
    public String scoreHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Account account = accountService.getByUsername(userDetails.getUsername());
        Member  member  = accountService.getMemberByAccountId(account.getAccountId());

        model.addAttribute("member",  member);
        model.addAttribute("invoices",
                accountService.getScoreHistory(account.getAccountId()));
        return "account/score-history";
    }

    // ============================================================
    // POST /account/change-password — Đổi mật khẩu
    // ============================================================
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Mật khẩu xác nhận không khớp!");
            return "redirect:/account/info";
        }

        try {
            accountService.changePassword(
                    userDetails.getUsername(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Đổi mật khẩu thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/account/info";
    }
}
 
