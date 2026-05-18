package com.quickbite.cart.repository;

import com.quickbite.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Find all items in a specific cart
    List<CartItem> findByCart_CartId(Long cartId);

    // Find a specific item in a specific cart
    // Used to check if item already exists (so we can increase qty)
    Optional<CartItem> findByCart_CartIdAndMenuItemId(Long cartId,
                                                      Long menuItemId);

    // Delete all items in a cart (when clearing or switching restaurant)
    void deleteByCart_CartId(Long cartId);
}