package com.buffalotraining.membership.controller;

import com.buffalotraining.membership.dto.CreateMembershipPlanRequest;
import com.buffalotraining.membership.dto.MembershipPlanResponse;
import com.buffalotraining.membership.dto.UpdateMembershipPlanRequest;
import com.buffalotraining.membership.dto.UpdateMembershipPlanStatusRequest;
import com.buffalotraining.membership.service.MembershipPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membership-plans")
@RequiredArgsConstructor
public class MembershipPlanController {

    private final MembershipPlanService membershipPlanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MembershipPlanResponse createPlan(@Valid @RequestBody CreateMembershipPlanRequest request) {
        return membershipPlanService.createPlan(request);
    }

    @GetMapping
    public List<MembershipPlanResponse> getAllPlans() {
        return membershipPlanService.getAllPlans();
    }

    @GetMapping("/active")
    public List<MembershipPlanResponse> getActivePlans() {
        return membershipPlanService.getActivePlans();
    }

    @GetMapping("/{id}")
    public MembershipPlanResponse getPlanById(@PathVariable Long id) {
        return membershipPlanService.getPlanById(id);
    }

    @PutMapping("/{id}")
    public MembershipPlanResponse updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMembershipPlanRequest request
    ) {
        return membershipPlanService.updatePlan(id, request);
    }

    @PatchMapping("/{id}/status")
    public MembershipPlanResponse updatePlanStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMembershipPlanStatusRequest request
    ) {
        return membershipPlanService.updatePlanStatus(id, request);
    }
}