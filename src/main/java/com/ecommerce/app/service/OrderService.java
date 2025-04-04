package com.ecommerce.app.service;

import com.ecommerce.app.model.Order;
import com.ecommerce.app.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public List<Order> getOrdersByUser(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders;
    }
}
