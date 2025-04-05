package com.ecommerce.app.controller;

import com.ecommerce.app.dto.UserDTO;
import com.ecommerce.app.model.Cart;
import com.ecommerce.app.model.Category;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());

        int cartItemCount = 0;

        if (isAuthenticated) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetail userDetails) {
                Optional<User> userOptional = userService.getUserByEmail(userDetails.getUsername());
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    UserDTO userDTO = new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getAddress());
                    model.addAttribute("loggedInUser", userDTO);
                    cartItemCount = cartService.getCartItemCount(user.getId());
                    model.addAttribute("cartItemCount", cartItemCount);
                }
            }
        }

        model.addAttribute("categories", categories);

        return "user/dashboard";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute UserDTO userDTO,
            Model model,
            RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOptional = userService.getUserByEmail(username);
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User not found");
            return "redirect:/dashboard";
        }

        User user = userOptional.get();

        user.setName(userDTO.getName());
        user.setPhone(userDTO.getPhone());
        user.setAddress(userDTO.getAddress());

        try {
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("loggedInUser", userDTO);
            return "redirect:/dashboard";
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOptional = userService.getUserByEmail(username);
        if (!userOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/login";
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect.");
            return "redirect:/dashboard";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/dashboard";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.saveUser(user);

        redirectAttributes.addFlashAttribute("success", "Password updated successfully.");
        return "redirect:/dashboard";
    }

    /*@GetMapping("/wishlist")
    public String userWishlist(Model model) {
    List<Category> categories = categoryService.getAllCategories();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());

        int cartItemCount = 0;

        if (isAuthenticated) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetail userDetails) {
                Optional<User> userOptional = userService.getUserByEmail(userDetails.getUsername());
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    UserDTO userDTO = new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getAddress());
                    model.addAttribute("loggedInUser", userDTO);
                    cartItemCount = cartService.getCartItemCount(user.getId());
                    model.addAttribute("cartItemCount", cartItemCount);
                }
            }
        }

        model.addAttribute("categories", categories);

        return "user/wishlist";
    }*/
}
