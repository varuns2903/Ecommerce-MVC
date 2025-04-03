package com.ecommerce.app.controller;

import com.ecommerce.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard() {

        return "user/dashboard";
    }

    @GetMapping("/profile")
    public String profile() {

        return "user/profile";
    }
}
