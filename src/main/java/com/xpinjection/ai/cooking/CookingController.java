package com.xpinjection.ai.cooking;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/cooking", produces = MediaType.APPLICATION_JSON_VALUE)
public class CookingController {

    private final CookingService cookingService;

    public CookingController(CookingService cookingService) {
        this.cookingService = cookingService;
    }

    @GetMapping(path = "/dish")
    public Dish dish(@RequestParam("dishImageName") String dishImageName) {
        return cookingService.recognizeDish(dishImageName);
    }
}
