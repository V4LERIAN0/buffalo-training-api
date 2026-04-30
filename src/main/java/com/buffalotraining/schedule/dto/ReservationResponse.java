package com.buffalotraining.schedule.dto;

import com.buffalotraining.schedule.entity.Reservation;
import com.buffalotraining.schedule.entity.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class ReservationResponse {

    private Long id;

    private Long classSessionId;
    private String className;
    private LocalDate classDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private Long athleteId;
    private String athleteName;
    private String athleteEmail;

    private Long coachId;
    private String coachName;

    private ReservationStatus status;
    private LocalDateTime reservedAt;
    private LocalDateTime cancelledAt;
    private String notes;

    public static ReservationResponse fromEntity(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())

                .classSessionId(reservation.getClassSession().getId())
                .className(reservation.getClassSession().getClassName())
                .classDate(reservation.getClassSession().getClassDate())
                .startTime(reservation.getClassSession().getStartTime())
                .endTime(reservation.getClassSession().getEndTime())

                .athleteId(reservation.getAthlete().getId())
                .athleteName(reservation.getAthlete().getFirstName() + " " + reservation.getAthlete().getLastName())
                .athleteEmail(reservation.getAthlete().getEmail())

                .coachId(reservation.getClassSession().getCoach().getId())
                .coachName(reservation.getClassSession().getCoach().getFirstName() + " " + reservation.getClassSession().getCoach().getLastName())

                .status(reservation.getStatus())
                .reservedAt(reservation.getReservedAt())
                .cancelledAt(reservation.getCancelledAt())
                .notes(reservation.getNotes())
                .build();
    }
}