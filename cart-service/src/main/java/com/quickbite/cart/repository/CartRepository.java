package com.quickbite.cart.repository;

import com.quickbite.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Find cart by customer
    // unique = true on customerId means each customer has max one cart
    Optional<Cart> findByCustomerId(Long customerId);

    // Check if customer already has a cart
    boolean existsByCustomerId(Long customerId);

    // Delete cart when customer checks out or clears
    void deleteByCustomerId(Long customerId);
}