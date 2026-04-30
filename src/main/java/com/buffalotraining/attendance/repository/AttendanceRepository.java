package com.buffalotraining.attendance.repository;

import com.buffalotraining.attendance.entity.Attendance;
import com.buffalotraining.attendance.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByClassSessionIdOrderByCheckInTimeAsc(Long classSessionId);

    List<Attendance> findByAthleteIdOrderByCheckInTimeDesc(Long athleteId);

    List<Attendance> findByStatusOrderByCheckInTimeDesc(AttendanceStatus status);

    Optional<Attendance> findFirstByReservationId(Long reservationId);

    boolean existsByReservationId(Long reservationId);

    boolean existsByClassSessionIdAndAthleteIdAndStatusNot(
            Long classSessionId,
            Long athleteId,
            AttendanceStatus status
    );
}