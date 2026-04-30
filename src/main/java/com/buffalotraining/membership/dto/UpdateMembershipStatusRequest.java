package com.buffalotraining.membership.dto;

import com.buffalotraining.membership.entity.MembershipStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMembershipStatusRequest {

    @NotNull(message = "Membership status is required")
    private MembershipStatus status;
}