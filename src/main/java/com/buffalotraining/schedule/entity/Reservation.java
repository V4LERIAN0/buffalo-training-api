package com.buffalotraining.schedule.entity;

import com.buffalotraining.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_session_id", nullable = false)
    private ClassSession classSession;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "athlete_id", nullable = false)
    private User athlete;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReservationStatus status;

    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "notes", length = 255)
    private String notes;

    @PrePersist
    public void prePersist() {
        if (this.reservedAt == null) {
            this.reservedAt = LocalDateTime.now();
        }

        if (this.status == null) {
            this.status = ReservationStatus.ACTIVE;
        }
    }
}