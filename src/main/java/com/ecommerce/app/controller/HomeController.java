package com.ecommerce.app.controller;

import com.ecommerce.app.dto.UserDTO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@SessionAttributes("categories")
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
                Optional<User> userOptional = userRepository.findByEmail(userDetails.getUsername());
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    UserDTO userDTO = new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getAddress());
                    model.addAttribute("loggedInUser", userDTO);
                    cartItemCount = cartService.getCartItemCount(user.getId());
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

    @GetMapping("/search")
    public String searchProducts(@RequestParam("query") String query, Model model) {
        List<Product> searchResults = productService.searchProducts(query);
        model.addAttribute("products", searchResults);
        model.addAttribute("query", query);
        return "search-results";
    }
}
