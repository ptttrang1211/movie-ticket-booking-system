package com.trang.gachon.movie.controller;

import com.trang.gachon.movie.dto.OtpRequest;
import com.trang.gachon.movie.dto.RegisterRequest;
import com.trang.gachon.movie.dto.ResetPasswordRequest;
import com.trang.gachon.movie.service.authandotpservice.AuthService;


import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ============================================================
    // LOGIN
    // ============================================================
    @GetMapping("/login")
    public String showLogin(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout, HttpSession session,
            Model model) {
        //nếu tài khoản chưa có thì sẽ bắn ra chưa có, nếu đang pending OTP thì chuyển hướng sang trang verify-otp
        if (error != null) {
            String msg = (String) session.getAttribute("errorMsg");
            model.addAttribute("errorMsg",
                    msg != null ? msg : "Tên tài khoản, email hoặc mật khẩu không đúng!");

            Boolean showVerifyOtpLink = (Boolean) session.getAttribute("showVerifyOtpLink");
            String pendingEmail = (String) session.getAttribute("pendingEmail");
            if (Boolean.TRUE.equals(showVerifyOtpLink)) {
                model.addAttribute("showVerifyOtpLink", true);
                model.addAttribute("pendingEmail", pendingEmail);
            }

            session.removeAttribute("errorMsg");
            session.removeAttribute("showVerifyOtpLink");
            session.removeAttribute("pendingEmail");
        }
        if (logout != null) model.addAttribute("successMsg", "Đăng xuất thành công.");
        return "auth/login";
    }

    //register

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(
            @Valid @ModelAttribute("registerRequest") RegisterRequest req,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) return "auth/register";

        try {
            String successMsg = authService.register(req);
            // Lưu email vào session để trang verify-otp biết gửi về đâu
            redirectAttributes.addFlashAttribute("email", req.getEmail());
            redirectAttributes.addFlashAttribute("successMsg", successMsg);
            return "redirect:/auth/verify-otp";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMsg", ex.getMessage());
            return "auth/register";
        }
    }

    // ============================================================
    // VERIFY OTP — Sau đăng ký
    // ============================================================
    @GetMapping("/verify-otp")
    public String showVerifyOtp(
            @RequestParam(required = false) String email,
            Model model) {
        OtpRequest otpRequest = new OtpRequest();
        Object flashEmail = model.asMap().get("email");
        if (flashEmail instanceof String flashEmailValue) {
            otpRequest.setEmail(flashEmailValue);
        } else if (email != null) {
            otpRequest.setEmail(email);
        }
        model.addAttribute("otpRequest", otpRequest);
        model.addAttribute("email", otpRequest.getEmail());
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String handleVerifyOtp(
            @Valid @ModelAttribute("otpRequest") OtpRequest req,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) return "auth/verify-otp";

        try {
            boolean valid = authService.verifyOtp(req.getEmail(), req.getOtp());
            if (!valid) {
                model.addAttribute("email", req.getEmail());
                model.addAttribute("errorMsg", "Mã OTP không đúng hoặc đã hết hạn!");
                model.addAttribute("otpRequest", req);
                return "auth/verify-otp";
            }

            redirectAttributes.addFlashAttribute("successMsg",
                    "Xác thực email thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("email", req.getEmail());
            model.addAttribute("errorMsg", ex.getMessage());
            model.addAttribute("otpRequest", req);
            return "auth/verify-otp";
        }
    }

    // ============================================================
    // RESEND OTP
    // ============================================================

    @PostMapping("/resend-otp")
    public String resendOtp(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {
        try {
            authService.resendOtp(email);
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("successMsg", "Đã gửi lại mã OTP về email " + email);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/auth/verify-otp";
    }

    // ============================================================
    // FORGOT PASSWORD — Bước 1: nhập email
    // ============================================================
    @GetMapping("/forgot-password")
    public String showForgotPassword(Model model) {
        model.addAttribute("email", "");
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(
            @RequestParam String email,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            authService.sendResetOtp(email);
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Mã OTP đã được gửi về email " + email);
            return "redirect:/auth/reset-password";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMsg", ex.getMessage());
            return "auth/forgot-password";
        }
    }

    // ============================================================
    // RESET PASSWORD — Bước 2: nhập OTP + mật khẩu mới
    // ============================================================
    @GetMapping("/reset-password")
    public String showResetPassword(Model model) {
        model.addAttribute("resetRequest", new ResetPasswordRequest());
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(
            @Valid @ModelAttribute("resetRequest") ResetPasswordRequest req,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) return "auth/reset-password";

        try {
            authService.resetPassword(req);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMsg", ex.getMessage());
            return "auth/reset-password";
        }
    }

    // ============================================================
    // 403
    // ============================================================
    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }

}
