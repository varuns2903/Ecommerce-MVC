package com.ecommerce.app.controller;

import com.ecommerce.app.dto.UserDTO;
import com.ecommerce.app.model.Category;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final UserService userService;
    private final CartService cartService;
    private final WishlistService wishlistService;

    public HomeController(CategoryService categoryService, ProductService productService, UserService userService, CartService cartService, WishlistService wishlistService) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.userService = userService;
        this.cartService = cartService;
        this.wishlistService = wishlistService;
    }

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {
        List<Category> categories = categoryService.getAllCategories();
        List<Product> products = productService.getAllProducts();

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
                    List<String> wishlistProductIds = wishlistService.getUserWishlist(user.getId()).getProducts().stream().map(Product::getId).toList();
                    model.addAttribute("wishlistProductIds", wishlistProductIds);
                }
            }
        } else {
            model.addAttribute("wishlistProductIds", null);
        }

        model.addAttribute("categories", categories);
        model.addAttribute("products", products);
        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("servletPath", request.getServletPath());

        System.out.println("Model: " + model.asMap());

        return "home";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam("query") String query, Model model) {

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
                }
            }
        }

        List<Category> categories = categoryService.getAllCategories();
        List<Product> searchResults = productService.searchProducts(query);
        model.addAttribute("categories", categories);
        model.addAttribute("products", searchResults);
        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("query", query);
        return "search-results";
    }
}
