package com.buffalotraining.membership.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentWithMembershipResponse {

    private String message;
    private PaymentResponse payment;
    private MembershipResponse updatedMembership;
}