package com.trang.gachon.movie.controller;

import com.trang.gachon.movie.dto.MovieRequest;
import com.trang.gachon.movie.entity.Movie;
import com.trang.gachon.movie.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MovieController {
    private final TypeService typeService;
    private final MovieService movieService;

    //public - trang chủ danh sách phim đang chiếu
    @GetMapping("/movies")
    public String showMovies(@RequestParam(required = false) String keyword, Model model) {
        // Trang public ưu tiên hiển thị phim đang mở bán.
        // Nếu người dùng có keyword thì search theo tên, còn không thì lấy phim đang chiếu.
        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("movies", movieService.searchByName(keyword));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("movies", movieService.getNowShowingMovies());
        }
        return "movie/list";
    }

    //public - chi tiết phim
    @GetMapping("/movies/{id}")
    public String showMovieDetail(@PathVariable Long id, Model model) {
        model.addAttribute("movie", movieService.getById(id));
        return "movie/detail";
    }

    //admin - quản lý danh sách phim

    @GetMapping("/admin/movie/list")
    public String adminMovieList(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "admin/movie/list";
    }

    //admin - form  thêm phim
    @GetMapping("/admin/movie/add")
    public String adminMovieAdd(Model model) {
        model.addAttribute("movieRequest", new MovieRequest());
        model.addAttribute("types", typeService.getAllTypes());
        return "admin/movie/add";
    }

    //admin - xử lí thêm phim
    @PostMapping("/admin/movie/add")
    public String handleAdminMovieAdd(@Valid @ModelAttribute("movieRequest") MovieRequest movieRequest, BindingResult bindingResult, @RequestParam(required = false) MultipartFile largeImageFile, @RequestParam(required = false) MultipartFile smallImageFile, @RequestParam(required = false) MultipartFile bannerImageFile, RedirectAttributes redirectAttributes, Model model) throws Exception {
        //Nếu có lỗi validate thì trả về form với lỗi, nếu không có lỗi thì gọi service thêm phim, sau đó chuyển hướng về trang danh sách phim với thông báo thành công
        if (bindingResult.hasErrors()) {
            model.addAttribute("types", typeService.getAllTypes());
            return "admin/movie/add";
        }

        try {
            movieService.addMovie(movieRequest, largeImageFile, smallImageFile, bannerImageFile);
            redirectAttributes.addFlashAttribute("successMsg", "Thêm phim thành công!");
            return "redirect:/admin/movie/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("types", typeService.getAllTypes());
            model.addAttribute("errorMsg", e.getMessage());
            return "admin/movie/add";
        }
    }

    //admin- form sửa phim
    @GetMapping("/admin/movie/edit/{id}")
    public String adminMovieEdit(@PathVariable Long id, Model model) {
        //lấy phim từ DB, nếu không tìm thấy sẽ ném ra IllegalArgumentException
        Movie movie = movieService.getById(id);

        //map entity sang request để điền vào form
        // Map entity → request để bind vào form
        MovieRequest req = movieService.toRequest(movie);

        model.addAttribute("movieRequest", req);
        model.addAttribute("movie", movie);
        model.addAttribute("types", typeService.getAllTypes());
        return "admin/movie/edit";
    }

    //admin - xử lí sửa phim
    @PostMapping("/admin/movie/edit/{id}")
    public String handleAdminMovieEdit(@PathVariable Long id, @Valid @ModelAttribute("movieRequest") MovieRequest movieRequest, BindingResult bindingResult, @RequestParam(required = false) MultipartFile largeImageFile, @RequestParam(required = false) MultipartFile smallImageFile, @RequestParam(required = false) MultipartFile bannerImageFile, RedirectAttributes redirectAttributes, Model model) throws Exception {
        if (bindingResult.hasErrors()) {
            model.addAttribute("types", typeService.getAllTypes());
            return "admin/movie/edit";
        }
        try {
            movieService.updateMovie(id, movieRequest, largeImageFile, smallImageFile, bannerImageFile);
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật phim thành công!");
            return "redirect:/admin/movie/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("types", typeService.getAllTypes());
            model.addAttribute("errorMsg", e.getMessage());
            return "admin/movie/edit";

        }
    }

    //admin- xoá phim - xoá thẳng tay cần sẽ bị dính khoá phụ bên show nên sẽ ném ra DataIntegrityViolationException, nếu muốn xoá thì phải xoá hết show liên quan trước, sau đó mới xoá phim
    @PostMapping("/admin/movie/delete/{id}")
    public String handleAdminMovieDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        //giải thích: gọi service để xóa phim dựa trên id đã cho. Nếu xóa thành công, thêm thông báo thành công vào redirectAttributes và chuyển hướng về trang danh sách phim. Nếu có lỗi xảy ra (ví dụ: không tìm thấy phim với id đó), bắt ngoại lệ và thêm thông báo lỗi vào redirectAttributes trước khi chuyển hướng về trang danh sách phim.
        try {
            movieService.deleteMovie(id);
            redirectAttributes.addFlashAttribute("successMsg", "Xóa phim thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Xóa phim thất bại: " + e.getMessage());
        }
        return "redirect:/admin/movie/list";
    }

}
