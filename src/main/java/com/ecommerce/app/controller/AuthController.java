package com.ecommerce.app.controller;

import com.ecommerce.app.dto.UserDTO;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String authenticateUser(@RequestParam("username") String username,
                                   @RequestParam("password") String password,
                                   Model model) {
        try {
            UserDetails userDetails = userService.authenticateUser(username, password);

            String role = userDetails.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .findFirst()
                    .orElse("USER");

            User user = userService.getUserByEmail(username);
            model.addAttribute("loggedInUser", user);
            model.addAttribute("role", role);
        } catch (AuthenticationException e) {
            System.out.println(e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "auth/login";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "auth/login";
        }

        System.out.println("Model: " + model.asMap());

        return "redirect:/";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        try {
            userService.saveUser(user);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.logout();
        } catch (ServletException e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            User user = userService.getUserByEmail(userDetails.getUsername());

            UserDTO userDTO = new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getAddress());
            model.addAttribute("loggedInUser", userDTO);
        }

        System.out.println("Model: " + model.asMap());

        return "auth/profile";
    }
}
