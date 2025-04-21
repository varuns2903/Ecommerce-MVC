package com.ecommerce.app.service;

import com.ecommerce.app.model.Cart;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.ProductItem;
import com.ecommerce.app.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;


@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductService productService;

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

    public Cart getCartByUser(String userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setItems(new ArrayList<>());
            return cartRepository.save(newCart);
        });
    }

    public void addProductToCart(String userId, String productId) {
        Cart cart = getCartByUser(userId);

        Product product = productService.getProductById(productId);
        boolean productExists = false;

        for (ProductItem item : cart.getItems()) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(item.getQuantity() + 1);
                item.setTotalPrice(item.getQuantity() * item.getPrice());
                productExists = true;
                break;
            }
        }

        if (!productExists) {
            ProductItem newItem = new ProductItem(product, 1);
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
    }

    public Cart decreaseQuantity(String userId, String productId) {
        Cart cart = getCartByUser(userId);

        if (cart == null || cart.getItems() == null) {
            return null;
        }

        Iterator<ProductItem> iterator = cart.getItems().iterator();
        while (iterator.hasNext()) {
            ProductItem item = iterator.next();
            if (item.getProduct().getId().equals(productId)) {
                if (item.getQuantity() <= 1) {
                    iterator.remove();
                } else {
                    item.setQuantity(item.getQuantity() - 1);
                    item.setTotalPrice(item.getQuantity() * item.getPrice());
                }
                break;
            }
        }

        return cartRepository.save(cart);
    }

    public Cart updateCartQuantities(String userId, Map<String, Integer> quantities) {
        Cart cart = getCartByUser(userId);

        cart.getItems().forEach(item -> {
            String productId = item.getProduct().getId();
            if (quantities.containsKey(productId)) {
                item.setQuantity(quantities.get(productId));
            }
        });

        cart.getItems().removeIf(item -> item.getQuantity() <= 0);

        return cartRepository.save(cart);
    }

    public Cart removeItemFromCart(String userId, String productId) {
        Cart cart = getCartByUser(userId);

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));

        return cartRepository.save(cart);
    }

    public void clearCart(String userId) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        cartOptional.ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }
}
