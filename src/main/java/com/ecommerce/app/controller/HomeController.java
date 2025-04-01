package com.ecommerce.app.controller;

import com.ecommerce.app.model.Category;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.UserRepository;
import com.ecommerce.app.service.CartService;
import com.ecommerce.app.service.CategoryService;
import com.ecommerce.app.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final CartService cartService;

    public HomeController(CategoryService categoryService, ProductService productService, UserRepository userRepository, CartService cartService) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.userRepository = userRepository;
        this.cartService = cartService;
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
            if (principal instanceof UserDetails userDetails) {
                Optional<User> user = userRepository.findByEmail(userDetails.getUsername());
                if (user.isPresent()) {
                    model.addAttribute("loggedInUser", user.get());
                    cartItemCount = cartService.getCartItemCount(user.get().getId());
                }
            }
        }

        model.addAttribute("categories", categories);
        model.addAttribute("products", products);
        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("servletPath", request.getServletPath());

        System.out.println("Model: " + model.asMap());

        return "home";
    }
}
