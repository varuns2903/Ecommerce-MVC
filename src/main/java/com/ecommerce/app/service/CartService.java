package com.ecommerce.app.service;

import com.ecommerce.app.model.Cart;
import com.ecommerce.app.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    public int getCartItemCount(String userId) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            if (cart.getItems() != null) {
                return cart.getItems().size();
            }
        }
        return 0;
    }
}
