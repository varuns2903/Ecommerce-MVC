package com.ecommerce.app.controller;

import com.ecommerce.app.dto.UserDTO;
import com.ecommerce.app.model.Cart;
import com.ecommerce.app.model.Category;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class CartController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @GetMapping("/cart")
    public String userCart(Model model) {
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
                    Cart cart = cartService.getCartByUser(user.getId());
                    model.addAttribute("cart", cart);
                    cartItemCount = cartService.getCartItemCount(user.getId());
                    model.addAttribute("cartItemCount", cartItemCount);
                }
            }
        }

        model.addAttribute("categories", categories);

        return "user/cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") String productId,
                            HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetail userDetails) {
            Optional<User> userOptional = userService.getUserByEmail(userDetails.getUsername());
            userOptional.ifPresent(user -> cartService.addProductToCart(user.getId(), productId));
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + referer;
    }


    @PostMapping("/cart/update")
    public String updateCart(@RequestParam Map<String, String> allParams, Model model, RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOptional = userService.getUserByEmail(username);
        if (!userOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/login";
        }

        User user = userOptional.get();

        Map<String, Integer> quantities = allParams.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("quantity_"))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().substring(9),
                        entry -> Integer.parseInt(entry.getValue())
                ));

        try {
            Cart updatedCart = cartService.updateCartQuantities(user.getId(), quantities);
            model.addAttribute("cart", updatedCart);
            model.addAttribute("cartItemCount", updatedCart.getItems().size());
            return "redirect:/cart";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update cart: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/cart/remove/{productId}")
    public String removeFromCart(@PathVariable String productId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<User> userOptional = userService.getUserByEmail(username);
            if (!userOptional.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "User not found.");
                return "redirect:/login";
            }

            User user = userOptional.get();

            Cart updatedCart = cartService.removeItemFromCart(user.getId(), productId);
            model.addAttribute("cart", updatedCart);
            model.addAttribute("cartItemCount", updatedCart.getItems().size());
            return "redirect:/cart";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to remove item: " + e.getMessage());
            return "redirect:/cart";
        }
    }

}
