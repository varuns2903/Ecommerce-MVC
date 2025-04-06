package com.ecommerce.app.controller;

import com.ecommerce.app.dto.UserDTO;
import com.ecommerce.app.model.Category;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class OrderController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/orders")
    public String userOrders(Model model) {
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
                    List<Order> orders = orderService.getOrdersByUser(user.getId());
                    model.addAttribute("orders", orders);
                }
            }
        }

        model.addAttribute("categories", categories);

        return "user/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable String orderId) {

        return "user/order-details";
    }
}
