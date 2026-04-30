package com.buffalotraining.membership.entity;

import com.buffalotraining.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "memberships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * This user must be an ATHLETE.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "athlete_id", nullable = false)
    private User athlete;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /*
     * Date when the membership access expires.
     */
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /*
     * This is extremely important for Buffalo Training.
     * This date preserves the original payment base date.
     *
     * Example:
     * If athlete starts on January 1 and pays late on February 10,
     * the base payment date remains day 1, not day 10.
     */
    @Column(name = "base_payment_date", nullable = false)
    private LocalDate basePaymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MembershipStatus status;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.active == null) {
            this.active = true;
        }

        if (this.status == null) {
            this.status = MembershipStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}