package com.ecommerce.app.service;

import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.ProductItem;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public Order findById(String id) {
        return orderRepository.findById(id).get();
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByUser(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders;
    }

    public Order createOrder(User user, List<ProductItem> items, String address) {
        Order order = new Order();
        order.setUserId(user.getId());
        order.setUserEmail(user.getEmail());
        order.setItems(items);
        order.setAddress(address);
        order.setStatus(Order.OrderStatus.NOT_PROCESS);
        order.setCreatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    public void deleteById(String id) {
        orderRepository.deleteById(id);
    }

    public Order updateOrder(String id, Order updatedOrder) {
        Order order = findById(id);
        order.setStatus(updatedOrder.getStatus());
        return orderRepository.save(order);
    }
    public List<Order> getOrdersByEmailAndProductId(String email, String productId) {
        return orderRepository.findByUserEmailAndItemsProductId(email, productId);
    }


}
