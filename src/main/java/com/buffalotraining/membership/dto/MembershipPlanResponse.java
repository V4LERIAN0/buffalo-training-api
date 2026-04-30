package com.buffalotraining.membership.dto;

import com.buffalotraining.membership.entity.MembershipPlan;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.buffalotraining.membership.entity.BillingCycle;

@Getter
@Builder
public class MembershipPlanResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private Integer gracePeriodDays;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BillingCycle billingCycle;

    public static MembershipPlanResponse fromEntity(MembershipPlan plan) {
        return MembershipPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .billingCycle(plan.getBillingCycle())
                .gracePeriodDays(plan.getGracePeriodDays())
                .active(plan.getActive())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}