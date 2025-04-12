package com.ecommerce.app.controller;

import com.ecommerce.app.dto.UserDTO;
import com.ecommerce.app.model.User;
import com.ecommerce.app.model.Wishlist;
import com.ecommerce.app.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserService userService;
    private final CartService cartService;

    @PostMapping("/add")
    public String addToWishlist(@RequestParam("productId") String productId, Model model) {

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
                    wishlistService.addToWishlist(user.getId(), productId);
                }
            }
        } else {
            return "redirect:/login";
        }

        model.addAttribute("cartItemCount", cartItemCount);

        return "redirect:/";
    }

    @GetMapping
    public String viewWishlist(Model model) {

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
                    Wishlist wishlist = wishlistService.getUserWishlist(user.getId());
                    model.addAttribute("wishlist", wishlist.getProducts());
                }
            }
        } else {
            return "redirect:/login?redirectTo=/wishlist";
        }

        model.addAttribute("cartItemCount", cartItemCount);

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
        if(isAdmin)
            return "admin/wishlist";

        return "user/wishlist";
    }

    @PostMapping("/toggle")
    public String toggleWishlist(@RequestParam("productId") String productId,
                                 Principal principal,
                                 @RequestHeader(value = "referer", required = false) String referer) {
        if (principal != null) {
            Optional<User> userOpt = userService.getUserByEmail(principal.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                wishlistService.toggleProductInWishlist(user.getId(), productId);
            }
        }

        return "redirect:" + (referer != null ? referer : "/");
    }


    @GetMapping("/remove/{productId}")
    public String removeFromWishlist(@PathVariable String productId, Model model) {

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
                    wishlistService.removeFromWishlist(user.getId(), productId);
                }
            }
        } else {
            return "redirect:/login";
        }

        model.addAttribute("cartItemCount", cartItemCount);

        return "redirect:/wishlist";
    }
}

