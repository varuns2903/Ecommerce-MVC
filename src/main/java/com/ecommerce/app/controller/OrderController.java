package com.ecommerce.app.controller;

import com.ecommerce.app.dto.UserDTO;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());

        if(!isAuthenticated)
            return "redirect:/login";

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

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
        if(isAdmin)
            return "admin/orders";

        return "user/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable("id") String orderId, Model model, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());

        if (!isAuthenticated)
            return "redirect:/login";

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
                    Order order = orderService.findById(orderId);
                    model.addAttribute("order", order);
                }
            }
        }

        if (redirectAttributes.containsAttribute("message")) {
            model.addAttribute("message", redirectAttributes.getFlashAttributes().get("message"));
        }

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
        if (isAdmin)
            return "admin/order-details";

        return "user/order-details";
    }


    @PostMapping("/orders/cancel/{id}")
    public String cancelOrder(@PathVariable("id") String orderId, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());

        if (!isAuthenticated)
            return "redirect:/login";

        Order order = orderService.findById(orderId);

        if (order != null && canCancel(order)) {
            order.setStatus(Order.OrderStatus.CANCELLED);
            orderService.save(order);
            redirectAttributes.addFlashAttribute("message", "Order cancelled successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Refund not available for this order.");
        }

        return "redirect:/orders/" + orderId;
    }


    private boolean canCancel(Order order) {
        return order.getStatus() == Order.OrderStatus.NOT_PROCESS ||
                order.getStatus() == Order.OrderStatus.PROCESSING ||
                order.getStatus() == Order.OrderStatus.SHIPPED;
    }
}
