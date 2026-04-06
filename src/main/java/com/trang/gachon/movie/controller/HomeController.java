package com.trang.gachon.movie.controller;

import com.trang.gachon.movie.service.MovieService;
import com.trang.gachon.movie.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MovieService     movieService;
    private final PromotionService promotionService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("featuredMovie", movieService.getFeaturedMovieForHome());
        model.addAttribute("movies",     movieService.getHomeNowShowingMovies());
        model.addAttribute("promotions", promotionService.getActivePromotions());
        return "home";
    }
}
