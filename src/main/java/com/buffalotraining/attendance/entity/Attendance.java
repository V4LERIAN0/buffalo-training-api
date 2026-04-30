package com.buffalotraining.attendance.entity;

import com.buffalotraining.schedule.entity.ClassSession;
import com.buffalotraining.schedule.entity.Reservation;
import com.buffalotraining.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Class where the athlete checked in.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_session_id", nullable = false)
    private ClassSession classSession;

    /*
     * Athlete who attended.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "athlete_id", nullable = false)
    private User athlete;

    /*
     * Reservation associated with this attendance.
     * Nullable because admin/manual attendance may be allowed without reservation.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    /*
     * Coach/admin who validated or manually registered attendance.
     * Nullable for initial athlete self check-in.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "validated_by_user_id")
    private User validatedBy;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_in_method", nullable = false, length = 30)
    private CheckInMethod checkInMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AttendanceStatus status;

    @Column(name = "notes", length = 255)
    private String notes;

    @PrePersist
    public void prePersist() {
        if (this.checkInTime == null) {
            this.checkInTime = LocalDateTime.now();
        }

        if (this.status == null) {
            this.status = AttendanceStatus.CHECKED_IN;
        }

        if (this.checkInMethod == null) {
            this.checkInMethod = CheckInMethod.SELF_CHECK_IN;
        }
    }
}