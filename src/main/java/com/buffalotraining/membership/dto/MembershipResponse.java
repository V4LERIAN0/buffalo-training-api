package com.buffalotraining.membership.dto;

import com.buffalotraining.membership.entity.Membership;
import com.buffalotraining.membership.entity.MembershipStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class MembershipResponse {

    private Long id;

    private Long athleteId;
    private String athleteName;
    private String athleteEmail;

    private Long planId;
    private String planName;
    private BigDecimal planPrice;
    private Integer durationDays;
    private Integer gracePeriodDays;

    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate basePaymentDate;

    private MembershipStatus status;
    private Boolean active;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MembershipResponse fromEntity(Membership membership) {
        return MembershipResponse.builder()
                .id(membership.getId())

                .athleteId(membership.getAthlete().getId())
                .athleteName(membership.getAthlete().getFirstName() + " " + membership.getAthlete().getLastName())
                .athleteEmail(membership.getAthlete().getEmail())

                .planId(membership.getPlan().getId())
                .planName(membership.getPlan().getName())
                .planPrice(membership.getPlan().getPrice())
                .durationDays(membership.getPlan().getDurationDays())
                .gracePeriodDays(membership.getPlan().getGracePeriodDays())

                .startDate(membership.getStartDate())
                .dueDate(membership.getDueDate())
                .basePaymentDate(membership.getBasePaymentDate())

                .status(membership.getStatus())
                .active(membership.getActive())
                .notes(membership.getNotes())

                .createdAt(membership.getCreatedAt())
                .updatedAt(membership.getUpdatedAt())
                .build();
    }
}