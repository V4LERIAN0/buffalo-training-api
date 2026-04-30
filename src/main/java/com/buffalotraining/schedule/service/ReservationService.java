package com.buffalotraining.schedule.service;

import com.buffalotraining.schedule.dto.ClassSessionResponse;
import com.buffalotraining.schedule.dto.CreateReservationRequest;
import com.buffalotraining.schedule.dto.ReservationResponse;
import com.buffalotraining.schedule.dto.ReservationWithClassSessionResponse;
import com.buffalotraining.schedule.entity.ClassSession;
import com.buffalotraining.schedule.entity.ClassSessionStatus;
import com.buffalotraining.schedule.entity.Reservation;
import com.buffalotraining.schedule.entity.ReservationStatus;
import com.buffalotraining.schedule.repository.ClassSessionRepository;
import com.buffalotraining.schedule.repository.ReservationRepository;
import com.buffalotraining.user.entity.User;
import com.buffalotraining.user.entity.UserStatus;
import com.buffalotraining.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.buffalotraining.membership.entity.Membership;
import com.buffalotraining.membership.entity.MembershipStatus;
import com.buffalotraining.membership.repository.MembershipRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ClassSessionRepository classSessionRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public ReservationWithClassSessionResponse createReservation(CreateReservationRequest request) {
        ClassSession classSession = classSessionRepository.findById(request.getClassSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Class session not found with id: " + request.getClassSessionId()));

        User athlete = userRepository.findById(request.getAthleteId())
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found with id: " + request.getAthleteId()));

        validateAthlete(athlete);
        validateAthleteMembership(athlete);
        validateClassCanBeReserved(classSession);

        if (reservationRepository.existsByClassSessionIdAndAthleteIdAndStatus(
                classSession.getId(),
                athlete.getId(),
                ReservationStatus.ACTIVE
        )) {
            throw new IllegalArgumentException("Athlete already has an active reservation for this class");
        }

        Reservation reservation = Reservation.builder()
                .classSession(classSession)
                .athlete(athlete)
                .status(ReservationStatus.ACTIVE)
                .reservedAt(LocalDateTime.now())
                .notes(request.getNotes())
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        increaseReservedSpots(classSession);
        ClassSession updatedClassSession = classSessionRepository.save(classSession);

        return ReservationWithClassSessionResponse.builder()
                .message("Class reservation created successfully")
                .reservation(ReservationResponse.fromEntity(savedReservation))
                .updatedClassSession(ClassSessionResponse.fromEntity(updatedClassSession))
                .build();
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::fromEntity)
                .toList();
    }

    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = findReservationEntityById(id);
        return ReservationResponse.fromEntity(reservation);
    }

    public List<ReservationResponse> getReservationsByClassSession(Long classSessionId) {
        return reservationRepository.findByClassSessionIdOrderByReservedAtAsc(classSessionId)
                .stream()
                .map(ReservationResponse::fromEntity)
                .toList();
    }

    public List<ReservationResponse> getReservationsByAthlete(Long athleteId) {
        return reservationRepository.findByAthleteIdOrderByReservedAtDesc(athleteId)
                .stream()
                .map(ReservationResponse::fromEntity)
                .toList();
    }

    public List<ReservationResponse> getReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatusOrderByReservedAtDesc(status)
                .stream()
                .map(ReservationResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ReservationWithClassSessionResponse cancelReservation(Long id) {
        Reservation reservation = findReservationEntityById(id);

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new IllegalArgumentException("Only active reservations can be cancelled");
        }

        ClassSession classSession = reservation.getClassSession();

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());

        Reservation cancelledReservation = reservationRepository.save(reservation);

        decreaseReservedSpots(classSession);
        ClassSession updatedClassSession = classSessionRepository.save(classSession);

        return ReservationWithClassSessionResponse.builder()
                .message("Class reservation cancelled successfully")
                .reservation(ReservationResponse.fromEntity(cancelledReservation))
                .updatedClassSession(ClassSessionResponse.fromEntity(updatedClassSession))
                .build();
    }

    @Transactional
    public ReservationWithClassSessionResponse markNoShow(Long id) {
        Reservation reservation = findReservationEntityById(id);

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new IllegalArgumentException("Only active reservations can be marked as no-show");
        }

        reservation.setStatus(ReservationStatus.NO_SHOW);

        Reservation updatedReservation = reservationRepository.save(reservation);

        return ReservationWithClassSessionResponse.builder()
                .message("Reservation marked as no-show")
                .reservation(ReservationResponse.fromEntity(updatedReservation))
                .updatedClassSession(ClassSessionResponse.fromEntity(reservation.getClassSession()))
                .build();
    }

    private Reservation findReservationEntityById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + id));
    }

    private MembershipStatus calculateMembershipStatus(java.time.LocalDate dueDate, Integer gracePeriodDays) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate expiringSoonStart = dueDate.minusDays(3);
        java.time.LocalDate graceLimitDate = dueDate.plusDays(gracePeriodDays);

        if (today.isBefore(expiringSoonStart)) {
            return MembershipStatus.ACTIVE;
        }

        if (!today.isAfter(graceLimitDate)) {
            return MembershipStatus.EXPIRING_SOON;
        }

        return MembershipStatus.EXPIRED;
    }

    private void validateAthlete(User user) {
        if (!"ATHLETE".equals(user.getRole().getName())) {
            throw new IllegalArgumentException("Selected user is not an athlete");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot reserve class for an inactive or suspended athlete");
        }
    }

    private void validateClassCanBeReserved(ClassSession classSession) {
        if (classSession.getStatus() == ClassSessionStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot reserve a cancelled class session");
        }

        if (classSession.getStatus() == ClassSessionStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot reserve a completed class session");
        }

        if (classSession.getStatus() == ClassSessionStatus.FULL) {
            throw new IllegalArgumentException("Class session is already full");
        }

        if (classSession.getReservedSpots() >= classSession.getCapacity()) {
            classSession.setStatus(ClassSessionStatus.FULL);
            classSessionRepository.save(classSession);
            throw new IllegalArgumentException("Class session is already full");
        }
    }

    private void increaseReservedSpots(ClassSession classSession) {
        classSession.setReservedSpots(classSession.getReservedSpots() + 1);

        if (classSession.getReservedSpots() >= classSession.getCapacity()) {
            classSession.setStatus(ClassSessionStatus.FULL);
        }
    }

    private void decreaseReservedSpots(ClassSession classSession) {
        if (classSession.getReservedSpots() > 0) {
            classSession.setReservedSpots(classSession.getReservedSpots() - 1);
        }

        if (classSession.getStatus() == ClassSessionStatus.FULL
                && classSession.getReservedSpots() < classSession.getCapacity()) {
            classSession.setStatus(ClassSessionStatus.OPEN);
        }
    }

    private void validateAthleteMembership(User athlete) {
        Membership membership = membershipRepository.findFirstByAthleteIdAndActiveTrueOrderByCreatedAtDesc(athlete.getId())
                .orElseThrow(() -> new IllegalArgumentException("Athlete does not have an active membership"));

        MembershipStatus status = calculateMembershipStatus(
                membership.getDueDate(),
                membership.getPlan().getGracePeriodDays()
        );

        if (membership.getStatus() != status) {
            membership.setStatus(status);
            membershipRepository.save(membership);
        }

        if (status == MembershipStatus.EXPIRED || status == MembershipStatus.CANCELLED) {
            throw new IllegalArgumentException("Athlete membership is not valid for reservations");
        }
    }
}