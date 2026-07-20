package com.sharedbasket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Root controller for handling page navigation
 */
@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard.xhtml";
    }
}
