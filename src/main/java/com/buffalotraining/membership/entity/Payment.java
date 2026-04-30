package com.buffalotraining.membership.entity;

import com.buffalotraining.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Membership being paid.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "membership_id", nullable = false)
    private Membership membership;

    /*
     * Athlete who made the payment.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "athlete_id", nullable = false)
    private User athlete;

    /*
     * Admin or coach who registered the payment manually.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "registered_by_user_id", nullable = false)
    private User registeredBy;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "reference_code", length = 80)
    private String referenceCode;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = PaymentStatus.APPROVED;
        }
    }
}