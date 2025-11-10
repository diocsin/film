package by.ilyushenko.film.controller;

import by.ilyushenko.film.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final MovieService movieService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/saved")
    public String saved(Model model) {
        model.addAttribute("movies", movieService.getAllSavedMovies());
        return "saved";
    }
}

