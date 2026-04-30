package com.buffalotraining.schedule.dto;

import com.buffalotraining.schedule.entity.ClassSession;
import com.buffalotraining.schedule.entity.ClassSessionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class ClassSessionResponse {

    private Long id;

    private String className;
    private String description;

    private LocalDate classDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private Integer capacity;
    private Integer reservedSpots;
    private Integer availableSpots;

    private Long coachId;
    private String coachName;
    private String coachEmail;

    private ClassSessionStatus status;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ClassSessionResponse fromEntity(ClassSession classSession) {
        Integer availableSpots = classSession.getCapacity() - classSession.getReservedSpots();

        return ClassSessionResponse.builder()
                .id(classSession.getId())
                .className(classSession.getClassName())
                .description(classSession.getDescription())
                .classDate(classSession.getClassDate())
                .startTime(classSession.getStartTime())
                .endTime(classSession.getEndTime())
                .capacity(classSession.getCapacity())
                .reservedSpots(classSession.getReservedSpots())
                .availableSpots(availableSpots)
                .coachId(classSession.getCoach().getId())
                .coachName(classSession.getCoach().getFirstName() + " " + classSession.getCoach().getLastName())
                .coachEmail(classSession.getCoach().getEmail())
                .status(classSession.getStatus())
                .notes(classSession.getNotes())
                .createdAt(classSession.getCreatedAt())
                .updatedAt(classSession.getUpdatedAt())
                .build();
    }
}