package com.buffalotraining.membership.repository;

import com.buffalotraining.membership.entity.Payment;
import com.buffalotraining.membership.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByMembershipIdOrderByPaymentDateDesc(Long membershipId);

    List<Payment> findByAthleteIdOrderByPaymentDateDesc(Long athleteId);

    List<Payment> findByStatusOrderByPaymentDateDesc(PaymentStatus status);
}