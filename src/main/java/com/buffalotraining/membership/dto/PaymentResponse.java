package com.buffalotraining.membership.dto;

import com.buffalotraining.membership.entity.Payment;
import com.buffalotraining.membership.entity.PaymentMethod;
import com.buffalotraining.membership.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {

    private Long id;

    private Long membershipId;

    private Long athleteId;
    private String athleteName;
    private String athleteEmail;

    private Long registeredByUserId;
    private String registeredByName;

    private BigDecimal amount;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;

    private String referenceCode;
    private String notes;

    private LocalDateTime createdAt;

    public static PaymentResponse fromEntity(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .membershipId(payment.getMembership().getId())

                .athleteId(payment.getAthlete().getId())
                .athleteName(payment.getAthlete().getFirstName() + " " + payment.getAthlete().getLastName())
                .athleteEmail(payment.getAthlete().getEmail())

                .registeredByUserId(payment.getRegisteredBy().getId())
                .registeredByName(payment.getRegisteredBy().getFirstName() + " " + payment.getRegisteredBy().getLastName())

                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())

                .referenceCode(payment.getReferenceCode())
                .notes(payment.getNotes())

                .createdAt(payment.getCreatedAt())
                .build();
    }
}