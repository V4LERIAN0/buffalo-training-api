package com.buffalotraining.attendance.dto;

import com.buffalotraining.attendance.entity.Attendance;
import com.buffalotraining.attendance.entity.AttendanceStatus;
import com.buffalotraining.attendance.entity.CheckInMethod;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class AttendanceResponse {

    private Long id;

    private Long classSessionId;
    private String className;
    private LocalDate classDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private Long athleteId;
    private String athleteName;
    private String athleteEmail;

    private Long reservationId;

    private Long validatedByUserId;
    private String validatedByName;

    private LocalDateTime checkInTime;
    private LocalDateTime validatedAt;

    private CheckInMethod checkInMethod;
    private AttendanceStatus status;

    private String notes;

    public static AttendanceResponse fromEntity(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())

                .classSessionId(attendance.getClassSession().getId())
                .className(attendance.getClassSession().getClassName())
                .classDate(attendance.getClassSession().getClassDate())
                .startTime(attendance.getClassSession().getStartTime())
                .endTime(attendance.getClassSession().getEndTime())

                .athleteId(attendance.getAthlete().getId())
                .athleteName(attendance.getAthlete().getFirstName() + " " + attendance.getAthlete().getLastName())
                .athleteEmail(attendance.getAthlete().getEmail())

                .reservationId(attendance.getReservation() != null ? attendance.getReservation().getId() : null)

                .validatedByUserId(attendance.getValidatedBy() != null ? attendance.getValidatedBy().getId() : null)
                .validatedByName(attendance.getValidatedBy() != null
                        ? attendance.getValidatedBy().getFirstName() + " " + attendance.getValidatedBy().getLastName()
                        : null)

                .checkInTime(attendance.getCheckInTime())
                .validatedAt(attendance.getValidatedAt())
                .checkInMethod(attendance.getCheckInMethod())
                .status(attendance.getStatus())
                .notes(attendance.getNotes())
                .build();
    }
}