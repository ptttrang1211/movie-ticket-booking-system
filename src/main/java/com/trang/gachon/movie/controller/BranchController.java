package com.trang.gachon.movie.controller;

import com.trang.gachon.movie.dto.BranchRequest;
import com.trang.gachon.movie.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/branch")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping({"", "/", "/list"})
    public String list(Model model) {
        model.addAttribute("branches", branchService.getAllForAdmin());
        return "admin/branch/list";
    }

    @GetMapping("/add")
    public String showAdd(Model model) {
        model.addAttribute("branchRequest", new BranchRequest());
        model.addAttribute("statuses", com.trang.gachon.movie.enums.BranchStatus.values());
        return "admin/branch/add";
    }

    @PostMapping("/add")
    public String add(
            @Valid @ModelAttribute("branchRequest") BranchRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", com.trang.gachon.movie.enums.BranchStatus.values());
            return "admin/branch/add";
        }

        try {
            branchService.addBranch(request);
            redirectAttributes.addFlashAttribute("successMsg", "Thêm chi nhánh thành công!");
            return "redirect:/admin/branch";
        } catch (IllegalArgumentException e) {
            model.addAttribute("statuses", com.trang.gachon.movie.enums.BranchStatus.values());
            model.addAttribute("errorMsg", e.getMessage());
            return "admin/branch/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEdit(@PathVariable Long id, Model model) {
        model.addAttribute("branchId", id);
        model.addAttribute("branchRequest", branchService.toRequest(id));
        model.addAttribute("statuses", com.trang.gachon.movie.enums.BranchStatus.values());
        return "admin/branch/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            @Valid @ModelAttribute("branchRequest") BranchRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("branchId", id);
            model.addAttribute("statuses", com.trang.gachon.movie.enums.BranchStatus.values());
            return "admin/branch/edit";
        }

        try {
            branchService.updateBranch(id, request);
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật chi nhánh thành công!");
            return "redirect:/admin/branch";
        } catch (IllegalArgumentException e) {
            model.addAttribute("branchId", id);
            model.addAttribute("statuses", com.trang.gachon.movie.enums.BranchStatus.values());
            model.addAttribute("errorMsg", e.getMessage());
            return "admin/branch/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            branchService.deleteBranch(id);
            redirectAttributes.addFlashAttribute("successMsg", "Xóa chi nhánh thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/branch";
    }
}
