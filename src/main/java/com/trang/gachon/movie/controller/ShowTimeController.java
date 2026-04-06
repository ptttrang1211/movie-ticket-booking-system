package com.trang.gachon.movie.controller;

import com.trang.gachon.movie.dto.ShowTimeRequest;
import com.trang.gachon.movie.entity.Showtime;
import com.trang.gachon.movie.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class ShowTimeController {
    private final ShowTimeService showTimeService;
    private final MovieService movieService;
    private final CinemaRoomService cinemaRoomService;
    private final BranchService branchService;

    //public - lịch phim sắp chiếu
    @GetMapping("/showtimes")
    public String showShowTime(@RequestParam ( required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date, @RequestParam(required = false) Long branchId,@RequestParam(required = false) Long movieId, Model model) {

        // Mặc định là hôm nay
        if (date == null) date = LocalDate.now();

        // Lấy showtimes theo filter
        if (movieId != null) {
            model.addAttribute("showtimes", showTimeService.getShowTimesByMovieAndDate(movieId, date));
            model.addAttribute("selectedMovie",
                    movieService.getById(movieId));
        } else if (branchId != null) {
            model.addAttribute("showtimes",
                    showTimeService.getShowTimesByBranchAndDate(branchId, date));
        } else {
            model.addAttribute("showtimes",
                    showTimeService.getShowTimesByDate(date));
        }

        model.addAttribute("branches",      branchService.getAll());
        model.addAttribute("movies",        movieService.getNowShowingMovies());
        model.addAttribute("selectedDate",  date);
        model.addAttribute("selectedBranchId", branchId);
        model.addAttribute("selectedMovieId",  movieId);

        // Tạo list 7 ngày tới để user chọn
        model.addAttribute("dateRange",
                java.util.stream.IntStream.range(0, 7)
                        .mapToObj(LocalDate.now()::plusDays)
                        .toList());

        return "showtime/list";
    }

    //admin - quản lí lịch chiếu
    @GetMapping("/admin/showtime/list")
    public String adminShowtimeList(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        if (date == null) date = LocalDate.now();

        model.addAttribute("showtime",    showTimeService.getShowTimesByDate(date));
        model.addAttribute("selectedDate", date);
        return "admin/showtime/list";
    }

    //admin - form thêm suất chiếu
    @GetMapping("/admin/showtime/add")
    public String adminShowtimeAdd(Model model) {
        model.addAttribute("showtimeRequest", new ShowTimeRequest());
        model.addAttribute("movies",      movieService.getAllMovies());
        model.addAttribute("cinemaRooms", cinemaRoomService.getAll());
        return "admin/showtime/add";
    }

    //admin - xử lí thêm suất chiếu

    @PostMapping("/admin/showtime/add")
    public String handleAdminShowtimeAdd(
            @Valid @ModelAttribute("showtimeRequest") ShowTimeRequest req,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("movies",      movieService.getAllMovies());
            model.addAttribute("cinemaRooms", cinemaRoomService.getAll());
            return "admin/showtime/add";
        }

        try {
            showTimeService.addShowTime(req);
            redirectAttributes.addFlashAttribute("successMsg", "Thêm suất chiếu thành công!");
            return "redirect:/admin/showtime/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg",    e.getMessage());
            model.addAttribute("movies",      movieService.getAllMovies());
            model.addAttribute("cinemaRooms", cinemaRoomService.getAll());
            return "admin/showtime/add";
        }
    }
    //admin - form sửa suất chiếu : get
    @GetMapping("/admin/showtime/edit/{id}")
    public String adminShowtimeEdit(@PathVariable Long id, Model model) {
        Showtime showtime = showTimeService.getShowTimeById(id);

        model.addAttribute("showtimeRequest", showTimeService.toRequest(showtime));
        model.addAttribute("showtime",    showtime);
        model.addAttribute("movies",      movieService.getAllMovies());
        model.addAttribute("cinemaRooms", cinemaRoomService.getAll());
        return "admin/showtime/edit";
    }

    //admin - xử lí sửa suất chiếu : post
    @PostMapping("/admin/showtime/edit/{id}")
    public String handleAdminShowtimeEdit(
            @PathVariable Long id,
            @Valid @ModelAttribute("showtimeRequest") ShowTimeRequest req,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("movies",      movieService.getAllMovies());
            model.addAttribute("cinemaRooms", cinemaRoomService.getAll());
            return "admin/showtime/edit";
        }

        try {
            showTimeService.updateShowTime(id, req);
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật suất chiếu thành công!");
            return "redirect:/admin/showtime/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg",    e.getMessage());
            model.addAttribute("movies",      movieService.getAllMovies());
            model.addAttribute("cinemaRooms", cinemaRoomService.getAll());
            return "admin/showtime/edit";
        }
    }

    //admin - xoá suấ chiếu

    @PostMapping("/admin/showtime/delete/{id}")
    public String handleAdminShowtimeDelete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            showTimeService.deleteShowTime(id);
            redirectAttributes.addFlashAttribute("successMsg", "Xóa suất chiếu thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/showtime/list";
    }
}



    

