package com.buffalotraining.schedule.repository;

import com.buffalotraining.schedule.entity.Reservation;
import com.buffalotraining.schedule.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByClassSessionIdOrderByReservedAtAsc(Long classSessionId);

    List<Reservation> findByAthleteIdOrderByReservedAtDesc(Long athleteId);

    List<Reservation> findByStatusOrderByReservedAtDesc(ReservationStatus status);

    Optional<Reservation> findFirstByClassSessionIdAndAthleteIdAndStatus(
            Long classSessionId,
            Long athleteId,
            ReservationStatus status
    );

    boolean existsByClassSessionIdAndAthleteIdAndStatus(
            Long classSessionId,
            Long athleteId,
            ReservationStatus status
    );
}