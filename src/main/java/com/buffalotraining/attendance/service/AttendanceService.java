package com.buffalotraining.attendance.service;

import com.buffalotraining.attendance.dto.AttendanceResponse;
import com.buffalotraining.attendance.dto.CheckInRequest;
import com.buffalotraining.attendance.dto.ManualAttendanceRequest;
import com.buffalotraining.attendance.dto.ValidateAttendanceRequest;
import com.buffalotraining.attendance.entity.Attendance;
import com.buffalotraining.attendance.entity.AttendanceStatus;
import com.buffalotraining.attendance.entity.CheckInMethod;
import com.buffalotraining.attendance.repository.AttendanceRepository;
import com.buffalotraining.membership.entity.Membership;
import com.buffalotraining.membership.entity.MembershipStatus;
import com.buffalotraining.membership.repository.MembershipRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ReservationRepository reservationRepository;
    private final ClassSessionRepository classSessionRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public AttendanceResponse checkIn(CheckInRequest request) {
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + request.getReservationId()));

        User athlete = userRepository.findById(request.getAthleteId())
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found with id: " + request.getAthleteId()));

        validateAthlete(athlete);
        validateReservationForCheckIn(reservation, athlete);

        if (attendanceRepository.existsByReservationId(reservation.getId())) {
            throw new IllegalArgumentException("Attendance already exists for this reservation");
        }

        validateAthleteMembership(athlete);

        Attendance attendance = Attendance.builder()
                .classSession(reservation.getClassSession())
                .athlete(athlete)
                .reservation(reservation)
                .checkInTime(LocalDateTime.now())
                .checkInMethod(CheckInMethod.SELF_CHECK_IN)
                .status(AttendanceStatus.CHECKED_IN)
                .notes(request.getNotes())
                .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);

        reservation.setStatus(ReservationStatus.ATTENDED);
        reservationRepository.save(reservation);

        return AttendanceResponse.fromEntity(savedAttendance);
    }

    @Transactional
    public AttendanceResponse manualAttendance(ManualAttendanceRequest request) {
        ClassSession classSession = classSessionRepository.findById(request.getClassSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Class session not found with id: " + request.getClassSessionId()));

        User athlete = userRepository.findById(request.getAthleteId())
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found with id: " + request.getAthleteId()));

        User registeredBy = userRepository.findById(request.getRegisteredByUserId())
                .orElseThrow(() -> new IllegalArgumentException("Registering user not found with id: " + request.getRegisteredByUserId()));

        validateAthlete(athlete);
        validateCoachOrAdmin(registeredBy);
        validateClassForManualAttendance(classSession);
        validateAthleteMembership(athlete);

        boolean attendanceExists = attendanceRepository.existsByClassSessionIdAndAthleteIdAndStatusNot(
                classSession.getId(),
                athlete.getId(),
                AttendanceStatus.CANCELLED
        );

        if (attendanceExists) {
            throw new IllegalArgumentException("Attendance already exists for this athlete and class session");
        }

        Attendance attendance = Attendance.builder()
                .classSession(classSession)
                .athlete(athlete)
                .reservation(null)
                .validatedBy(registeredBy)
                .checkInTime(LocalDateTime.now())
                .validatedAt(LocalDateTime.now())
                .checkInMethod(CheckInMethod.ADMIN_MANUAL)
                .status(AttendanceStatus.MANUAL)
                .notes(request.getNotes())
                .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);

        return AttendanceResponse.fromEntity(savedAttendance);
    }

    public List<AttendanceResponse> getAllAttendance() {
        return attendanceRepository.findAll()
                .stream()
                .map(AttendanceResponse::fromEntity)
                .toList();
    }

    public AttendanceResponse getAttendanceById(Long id) {
        Attendance attendance = findAttendanceEntityById(id);
        return AttendanceResponse.fromEntity(attendance);
    }

    public List<AttendanceResponse> getAttendanceByClassSession(Long classSessionId) {
        return attendanceRepository.findByClassSessionIdOrderByCheckInTimeAsc(classSessionId)
                .stream()
                .map(AttendanceResponse::fromEntity)
                .toList();
    }

    public List<AttendanceResponse> getAttendanceByAthlete(Long athleteId) {
        return attendanceRepository.findByAthleteIdOrderByCheckInTimeDesc(athleteId)
                .stream()
                .map(AttendanceResponse::fromEntity)
                .toList();
    }

    public List<AttendanceResponse> getAttendanceByStatus(AttendanceStatus status) {
        return attendanceRepository.findByStatusOrderByCheckInTimeDesc(status)
                .stream()
                .map(AttendanceResponse::fromEntity)
                .toList();
    }

    @Transactional
    public AttendanceResponse validateAttendance(Long id, ValidateAttendanceRequest request) {
        Attendance attendance = findAttendanceEntityById(id);

        if (attendance.getStatus() == AttendanceStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled attendance cannot be validated");
        }

        User validator = userRepository.findById(request.getValidatedByUserId())
                .orElseThrow(() -> new IllegalArgumentException("Validator user not found with id: " + request.getValidatedByUserId()));

        validateCoachOrAdmin(validator);

        attendance.setValidatedBy(validator);
        attendance.setValidatedAt(LocalDateTime.now());
        attendance.setStatus(AttendanceStatus.VALIDATED);
        attendance.setCheckInMethod(CheckInMethod.COACH_VALIDATED);

        if (request.getNotes() != null) {
            attendance.setNotes(request.getNotes());
        }

        Attendance validatedAttendance = attendanceRepository.save(attendance);

        return AttendanceResponse.fromEntity(validatedAttendance);
    }

    @Transactional
    public AttendanceResponse cancelAttendance(Long id) {
        Attendance attendance = findAttendanceEntityById(id);

        if (attendance.getStatus() == AttendanceStatus.CANCELLED) {
            throw new IllegalArgumentException("Attendance is already cancelled");
        }

        attendance.setStatus(AttendanceStatus.CANCELLED);

        Attendance cancelledAttendance = attendanceRepository.save(attendance);

        return AttendanceResponse.fromEntity(cancelledAttendance);
    }

    private Attendance findAttendanceEntityById(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attendance not found with id: " + id));
    }

    private void validateAthlete(User user) {
        if (!"ATHLETE".equals(user.getRole().getName())) {
            throw new IllegalArgumentException("Selected user is not an athlete");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Athlete user is not active");
        }
    }

    private void validateCoachOrAdmin(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("User is not active");
        }

        String roleName = user.getRole().getName();

        if (!"COACH".equals(roleName) && !"ADMIN".equals(roleName)) {
            throw new IllegalArgumentException("Only a coach or admin can perform this action");
        }
    }

    private void validateReservationForCheckIn(Reservation reservation, User athlete) {
        if (!reservation.getAthlete().getId().equals(athlete.getId())) {
            throw new IllegalArgumentException("Reservation does not belong to this athlete");
        }

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new IllegalArgumentException("Only active reservations can be checked in");
        }

        ClassSession classSession = reservation.getClassSession();

        if (classSession.getStatus() == ClassSessionStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot check in to a cancelled class session");
        }

        if (classSession.getStatus() == ClassSessionStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot check in to a completed class session");
        }
    }

    private void validateClassForManualAttendance(ClassSession classSession) {
        if (classSession.getStatus() == ClassSessionStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot register attendance for a cancelled class session");
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
            throw new IllegalArgumentException("Athlete membership is not valid for attendance");
        }
    }

    private MembershipStatus calculateMembershipStatus(LocalDate dueDate, Integer gracePeriodDays) {
        LocalDate today = LocalDate.now();
        LocalDate expiringSoonStart = dueDate.minusDays(3);
        LocalDate graceLimitDate = dueDate.plusDays(gracePeriodDays);

        if (today.isBefore(expiringSoonStart)) {
            return MembershipStatus.ACTIVE;
        }

        if (!today.isAfter(graceLimitDate)) {
            return MembershipStatus.EXPIRING_SOON;
        }

        return MembershipStatus.EXPIRED;
    }
}