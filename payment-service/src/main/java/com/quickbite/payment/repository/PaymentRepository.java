package com.quickbite.payment.repository;

import com.quickbite.payment.entity.Payment;
import com.quickbite.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);
    List<Payment> findByCustomerId(Long customerId);
    List<Payment> findByStatus(PaymentStatus status);
    boolean existsByOrderId(Long orderId);
}