package com.trang.gachon.movie.controller;

import com.trang.gachon.movie.entity.Promotion;
import com.trang.gachon.movie.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    // Public — xem khuyến mãi đang có hiệu lực
    @GetMapping("/promotions")
    public String list(Model model) {
        model.addAttribute("promotions",
                promotionService.getActivePromotions());
        return "promotion/list";
    }

    // Admin — quản lý
    @GetMapping("/admin/promotion")
    public String adminHome() {
        return "redirect:/admin/promotion/list";
    }

    @GetMapping("/admin/promotion/list")
    public String adminList(Model model) {
        model.addAttribute("promotions", promotionService.getAll());
        return "admin/promotion/list";
    }

    @GetMapping("/admin/promotion/add")
    public String showAdd(Model model) {
        model.addAttribute("promotion", new Promotion());
        return "admin/promotion/add-edit";
    }

    @PostMapping("/admin/promotion/add")
    public String handleAdd(
            @ModelAttribute("promotion") Promotion promotion,
            @RequestParam(required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {

        try {
            promotionService.save(promotion, imageFile);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Thêm khuyến mãi thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể lưu ảnh banner.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/promotion/list";
    }

    @GetMapping("/admin/promotion/edit/{id}")
    public String showEdit(@PathVariable Long id, Model model) {
        model.addAttribute("promotion", promotionService.getById(id));
        return "admin/promotion/add-edit";
    }

    @PostMapping("/admin/promotion/edit/{id}")
    public String handleEdit(
            @PathVariable Long id,
            @ModelAttribute("promotion") Promotion promotion,
            @RequestParam(required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {

        promotion.setPromotionId(id);
        try {
            promotionService.save(promotion, imageFile);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Cập nhật khuyến mãi thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể lưu ảnh banner.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/promotion/list";
    }

    @PostMapping("/admin/promotion/delete/{id}")
    public String handleDelete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            promotionService.delete(id);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Xóa khuyến mãi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/promotion/list";
    }
}
