package com.ecommerce.app.controller;

import com.ecommerce.app.dto.UserDTO;
import com.ecommerce.app.model.Category;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    // Dashboard
    @GetMapping("/dashboard")
    public String getDashboard(Model model) {

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

        return "admin/dashboard";
    }

    // List all products
    @GetMapping("/products")
    public String getProducts(Model model) {
        return "admin/products/list";
    }

    // Show product form for new product
    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new Product());
        return "admin/products/form";
    }

    // Show product form for editing existing product
    @GetMapping("/products/edit/{id}")
    public String editProduct(@PathVariable String id, Model model) {
        model.addAttribute("product", productService.getProductById(id));
        return "admin/products/form";
    }

    // Save or update product
    @PostMapping("/products")
    public String saveProduct(@ModelAttribute("product") Product product) {
        if (product.getId() == null ) {
            productService.createProduct(product);
        } else {
            productService.updateProduct(product.getId(), product);
        }
        return "redirect:/admin/products";
    }

    // Delete product
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }







    // Categories
    @GetMapping("/categories")
    public String getCategories(Model model) {
        return "admin/categories/list";
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("category", new Category());
        return "admin/categories/form";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable String id, Model model) {
        model.addAttribute("category", categoryService.getCategoryById(id));
        return "admin/categories/form";
    }

    @PostMapping("/categories")
    public String saveCategory(@ModelAttribute("category") Category category) {
        if (category.getId() == null || category.getId().trim().isEmpty()) {
            categoryService.createCategory(category);
        } else {
            categoryService.updateCategory(category.getId(), category);
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return "redirect:/admin/categories";
    }









    // Orders
    @GetMapping("/orders")
    public String getOrders(Model model) {
        return "admin/orders/list";
    }

    @GetMapping("/orders/{id}")
    public String editOrder(@PathVariable String id, Model model) {
        Order order = orderService.findById(id);
        model.addAttribute("order", order);
        model.addAttribute("statuses", Order.OrderStatus.values());
        return "admin/orders/form";
    }

    @PostMapping("/orders")
    public String updateOrder(@ModelAttribute Order order) {
        orderService.updateOrder(order.getId(), order);
        return "redirect:/admin/orders";
    }

    @GetMapping("/orders/delete/{id}")
    public String deleteOrder(@PathVariable String id) {
        orderService.deleteById(id);
        return "redirect:/admin/orders";
    }
}