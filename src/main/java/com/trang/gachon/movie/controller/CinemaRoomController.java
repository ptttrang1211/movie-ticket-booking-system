package com.trang.gachon.movie.controller;

import com.trang.gachon.movie.dto.CinemaRoomRequest;
import com.trang.gachon.movie.service.BranchService;
import com.trang.gachon.movie.service.CinemaRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/admin/cinema-room", "/cinema-room"})
@RequiredArgsConstructor
public class CinemaRoomController {

    private final CinemaRoomService cinemaRoomService;
    private final BranchService branchService;

    @GetMapping({"", "/", "/list"})
    public String list(Model model) {
        model.addAttribute("rooms", cinemaRoomService.getAll());
        return "admin/cinema-room/list";
    }

    @GetMapping("/add")
    public String showAdd(Model model) {
        model.addAttribute("cinemaRoomRequest", new CinemaRoomRequest());
        model.addAttribute("branches", branchService.getAll());
        return "admin/cinema-room/add";
    }

    @PostMapping("/add")
    public String handleAdd(
            @Valid @ModelAttribute("cinemaRoomRequest") CinemaRoomRequest req,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("branches", branchService.getAll());
            return "admin/cinema-room/add";
        }

        try {
            cinemaRoomService.addRoom(req);
            redirectAttributes.addFlashAttribute("successMsg", "Thêm phòng chiếu thành công!");
            return "redirect:/admin/cinema-room";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("branches", branchService.getAll());
            return "admin/cinema-room/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEdit(@PathVariable Long id, Model model) {
        var room = cinemaRoomService.getById(id);
        model.addAttribute("room", room);
        model.addAttribute("cinemaRoomRequest", cinemaRoomService.toRequest(room));
        model.addAttribute("branches", branchService.getAll());
        model.addAttribute("seats", cinemaRoomService.getSeatsByRoom(id));
        return "admin/cinema-room/edit";
    }

    @PostMapping("/edit/{id}")
    public String handleEdit(
            @PathVariable Long id,
            @Valid @ModelAttribute("cinemaRoomRequest") CinemaRoomRequest req,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("room", cinemaRoomService.getById(id));
            model.addAttribute("branches", branchService.getAll());
            model.addAttribute("seats", cinemaRoomService.getSeatsByRoom(id));
            return "admin/cinema-room/edit";
        }

        try {
            cinemaRoomService.updateRoom(id, req);
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật phòng chiếu thành công!");
            return "redirect:/admin/cinema-room";
        } catch (IllegalArgumentException e) {
            model.addAttribute("room", cinemaRoomService.getById(id));
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("branches", branchService.getAll());
            model.addAttribute("seats", cinemaRoomService.getSeatsByRoom(id));
            return "admin/cinema-room/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            cinemaRoomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("successMsg", "Xóa phòng chiếu thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/cinema-room";
    }
}
