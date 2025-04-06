package com.ecommerce.app.service;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.Wishlist;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public Wishlist getUserWishlist(String userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wishlist wishlist = new Wishlist();
                    wishlist.setUserId(userId);
                    return wishlistRepository.save(wishlist);
                });
    }

    public void addToWishlist(String userId, String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Wishlist wishlist = getUserWishlist(userId);

        if (!wishlist.getProducts().contains(product)) {
            wishlist.getProducts().add(product);
            wishlistRepository.save(wishlist);
        }
    }

    public void toggleProductInWishlist(String userId, String productId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUserId(userId);
                    return wishlistRepository.save(newWishlist);
                });
        Product product = productRepository.findById(productId)
                .orElseThrow();

        if (wishlist.getProducts().contains(product)) {
            wishlist.getProducts().remove(product);
        } else {
            wishlist.getProducts().add(product);
        }

        wishlistRepository.save(wishlist);
    }

    public void removeFromWishlist(String userId, String productId) {
        Wishlist wishlist = getUserWishlist(userId);
        wishlist.getProducts().removeIf(p -> p.getId().equals(productId));
        wishlistRepository.save(wishlist);
    }
}
