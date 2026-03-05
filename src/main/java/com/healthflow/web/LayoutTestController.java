package com.healthflow.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LayoutTestController {

    @GetMapping("/test/layout")
    public String testLayout(Model model) {
        model.addAttribute("title", "Prueba de Layout");
        model.addAttribute("notificationCount", 3);
        return "test-layout";
    }
}