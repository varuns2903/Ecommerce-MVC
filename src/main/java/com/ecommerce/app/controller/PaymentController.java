package com.ecommerce.app.controller;

import com.ecommerce.app.dto.UserDTO;
import com.ecommerce.app.model.Cart;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.*;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private EmailService emailService;

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

        model.addAttribute("cart", cart);
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

        Order order = orderService.createOrder(user, cart.getItems(), address);

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

        orderService.save(order);

        // Redirect to Stripe checkout page
        return "redirect:" + session.getUrl();
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam("orderId") String orderId, Model model, Authentication authentication) {
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());

        if (isAuthenticated) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetail userDetails) {
                System.out.println("Authentication name: " + userDetails.getUsername());
                Optional<User> userOptional = userService.getUserByEmail(userDetails.getUsername());
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    Order savedOrder = orderService.findById(orderId);

                    // ✅ Send confirmation email
                    String subject = "Order Confirmation - Order ID: " + savedOrder.getId();
                    String body = "<!DOCTYPE html>" +
                            "<html lang='en'>" +
                            "<head>" +
                            "<meta charset='UTF-8'>" +
                            "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                            "<style>" +
                            "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                            ".container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }" +
                            ".header { background-color: #007bff; color: #ffffff; padding: 20px; text-align: center; }" +
                            ".header h1 { margin: 0; font-size: 24px; }" +
                            ".content { padding: 20px; }" +
                            ".content p { line-height: 1.6; color: #333333; }" +
                            ".order-details { width: 100%; border-collapse: collapse; margin: 20px 0; }" +
                            ".order-details th, .order-details td { padding: 10px; border: 1px solid #dddddd; text-align: left; }" +
                            ".order-details th { background-color: #f8f8f8; }" +
                            ".footer { text-align: center; padding: 20px; background-color: #f4f4f4; color: #777777; font-size: 14px; }" +
                            ".button { display: inline-block; padding: 10px 20px; margin: 20px 0; background-color: #007bff; color: #ffffff; text-decoration: none; border-radius: 5px; }" +
                            "</style>" +
                            "</head>" +
                            "<body>" +
                            "<div class='container'>" +
                            "<div class='header'>" +
                            "<h1>Order Confirmation</h1>" +
                            "</div>" +
                            "<div class='content'>" +
                            "<p>Dear " + user.getName() + ",</p>" +
                            "<p>Thank you for your purchase! Your order has been placed successfully.</p>" +
                            "<h2>Order Details</h2>" +
                            "<table class='order-details'>" +
                            "<tr><th>Order ID</th><td>" + savedOrder.getId() + "</td></tr>" +
                            "<tr><th>Shipping Address</th><td>" + savedOrder.getAddress() + "</td></tr>" +
                            "<tr><th>Total Items</th><td>" + savedOrder.getItems().size() + "</td></tr>" +
                            "</table>" +
                            "<p>We’re preparing your order and will notify you once it’s shipped.</p>" +
                            "<a href='https://yourstore.com/orders/" + savedOrder.getId() + "' class='button'>View Your Order</a>" +
                            "</div>" +
                            "<div class='footer'>" +
                            "<p>Thank you for shopping with us!</p>" +
                            "<p>&copy; 2025 Your Store. All rights reserved.</p>" +
                            "</div>" +
                            "</div>" +
                            "</body>" +
                            "</html>";

                    emailService.sendEmail(user.getEmail(), subject, body);
                }
            }
        }



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



        Order order = orderService.findById(orderId);
        if (order != null && order.getStatus() == Order.OrderStatus.NOT_PROCESS) {
            order.setStatus(Order.OrderStatus.PROCESSING);
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

        Order order = orderService.findById(orderId);

        if (order != null && order.getStatus() == Order.OrderStatus.NOT_PROCESS) {
            orderService.deleteById(orderId);
        }

        model.addAttribute("message", "Payment was cancelled and the order was discarded.");
        return "user/payment_cancel";
    }

}
