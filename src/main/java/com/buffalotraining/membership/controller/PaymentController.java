package com.buffalotraining.membership.controller;

import com.buffalotraining.membership.dto.PaymentResponse;
import com.buffalotraining.membership.dto.PaymentWithMembershipResponse;
import com.buffalotraining.membership.dto.RegisterMembershipPaymentRequest;
import com.buffalotraining.membership.entity.PaymentStatus;
import com.buffalotraining.membership.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/membership")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentWithMembershipResponse registerMembershipPayment(
            @Valid @RequestBody RegisterMembershipPaymentRequest request
    ) {
        return paymentService.registerMembershipPayment(request);
    }

    @GetMapping
    public List<PaymentResponse> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/{id}")
    public PaymentResponse getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @GetMapping("/membership/{membershipId}")
    public List<PaymentResponse> getPaymentsByMembership(@PathVariable Long membershipId) {
        return paymentService.getPaymentsByMembership(membershipId);
    }

    @GetMapping("/athlete/{athleteId}")
    public List<PaymentResponse> getPaymentsByAthlete(@PathVariable Long athleteId) {
        return paymentService.getPaymentsByAthlete(athleteId);
    }

    @GetMapping("/status/{status}")
    public List<PaymentResponse> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        return paymentService.getPaymentsByStatus(status);
    }
}