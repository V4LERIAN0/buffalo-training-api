package com.buffalotraining.schedule.entity;

import com.buffalotraining.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "class_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_name", nullable = false, length = 80)
    private String className;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "class_date", nullable = false)
    private LocalDate classDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "reserved_spots", nullable = false)
    private Integer reservedSpots;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "coach_id", nullable = false)
    private User coach;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ClassSessionStatus status;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.reservedSpots == null) {
            this.reservedSpots = 0;
        }

        if (this.status == null) {
            this.status = ClassSessionStatus.OPEN;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}