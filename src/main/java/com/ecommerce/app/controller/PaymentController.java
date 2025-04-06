package com.ecommerce.app.controller;

import com.ecommerce.app.dto.UserDTO;
import com.ecommerce.app.model.Cart;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.CartService;
import com.ecommerce.app.service.CustomUserDetail;
import com.ecommerce.app.service.OrderService;
import com.ecommerce.app.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class PaymentController {

    @Value("${stripe.public.key}")
    private String stripePublicKey;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping("/checkout")
    public String checkoutPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User user = userService.getUserByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Cart cart = cartService.getCartByUser(user.getId());
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        int cartItemCount = 0;

        UserDTO userDTO = new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getAddress());
        model.addAttribute("loggedInUser", userDTO);

        cartItemCount = cartService.getCartItemCount(user.getId());

        model.addAttribute("cart", cart);
        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("address", user.getAddress()); // Optional: preload saved address
        return "user/checkout";
    }

    @PostMapping("/checkout")
    public String handleCheckout(
            @RequestParam("address") String address,
            Authentication authentication
    ) throws StripeException {
        // Check authentication
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // Get user details
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        Optional<User> optionalUser = userService.getUserByEmail(userDetails.getUsername());

        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = optionalUser.get();

        // Get user's cart
        Cart cart = cartService.getCartByUser(user.getId());
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        // 1. Create new Order with NOT_PROCESS status
        Order order = new Order();
        order.setUserId(user.getId());
        order.setAddress(address);
        order.setItems(cart.getItems());
        order.setStatus(Order.OrderStatus.NOT_PROCESS);
        order.setCreatedAt(LocalDateTime.now());

        // Save order to get order ID
        order = orderService.save(order);

        // 2. Prepare Stripe Line Items
        List<SessionCreateParams.LineItem> lineItems = cart.getItems().stream()
                .map(item -> SessionCreateParams.LineItem.builder()
                        .setQuantity((long) item.getQuantity())
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount((long) (item.getTotalPrice() * 100)) // Convert to cents
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(item.getProduct().getName())
                                        .build())
                                .build())
                        .build())
                .collect(Collectors.toList());

        // 3. Create Stripe Session
        SessionCreateParams params = SessionCreateParams.builder()
                .addAllLineItem(lineItems)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/payment/success?orderId=" + order.getId())
                .setCancelUrl("http://localhost:8080/payment/cancel?orderId=" + order.getId())
                .build();

        Session session = Session.create(params);

        // Redirect to Stripe checkout page
        return "redirect:" + session.getUrl();
    }


    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam("orderId") String orderId, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User user = userService.getUserByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Cart cart = cartService.getCartByUser(user.getId());
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        int cartItemCount = 0;

        UserDTO userDTO = new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getAddress());
        model.addAttribute("loggedInUser", userDTO);

        cartItemCount = cartService.getCartItemCount(user.getId());
        model.addAttribute("cartItemCount", cartItemCount);

        Order order = orderService.findById(orderId);
        if (order != null && order.getStatus() == Order.OrderStatus.NOT_PROCESS) {
            orderService.save(order);

            cartService.clearCart(order.getUserId());
        }

        model.addAttribute("order", order);
        return "user/payment_success"; // Success view
    }

    @GetMapping("/payment/cancel")
    public String paymentCancel(@RequestParam("orderId") String orderId, Model model, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User user = userService.getUserByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Cart cart = cartService.getCartByUser(user.getId());
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        int cartItemCount = 0;

        UserDTO userDTO = new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getAddress());
        model.addAttribute("loggedInUser", userDTO);

        cartItemCount = cartService.getCartItemCount(user.getId());
        model.addAttribute("cartItemCount", cartItemCount);

        Order order = orderService.findById(orderId);

        if (order != null && order.getStatus() == Order.OrderStatus.NOT_PROCESS) {
            orderService.deleteById(orderId);
        }

        model.addAttribute("message", "Payment was cancelled and the order was discarded.");
        return "user/payment_cancel";
    }

}
