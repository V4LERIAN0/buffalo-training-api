package com.buffalotraining.membership.controller;

import com.buffalotraining.membership.dto.AssignMembershipRequest;
import com.buffalotraining.membership.dto.MembershipResponse;
import com.buffalotraining.membership.dto.UpdateMembershipStatusRequest;
import com.buffalotraining.membership.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MembershipResponse assignMembership(@Valid @RequestBody AssignMembershipRequest request) {
        return membershipService.assignMembership(request);
    }

    @GetMapping
    public List<MembershipResponse> getAllMemberships() {
        return membershipService.getAllMemberships();
    }

    @GetMapping("/{id}")
    public MembershipResponse getMembershipById(@PathVariable Long id) {
        return membershipService.getMembershipById(id);
    }

    @GetMapping("/athlete/{athleteId}")
    public List<MembershipResponse> getMembershipsByAthlete(@PathVariable Long athleteId) {
        return membershipService.getMembershipsByAthlete(athleteId);
    }

    @GetMapping("/athlete/{athleteId}/active")
    public MembershipResponse getActiveMembershipByAthlete(@PathVariable Long athleteId) {
        return membershipService.getActiveMembershipByAthlete(athleteId);
    }

    @PatchMapping("/{id}/status")
    public MembershipResponse updateMembershipStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMembershipStatusRequest request
    ) {
        return membershipService.updateMembershipStatus(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public MembershipResponse cancelMembership(@PathVariable Long id) {
        return membershipService.cancelMembership(id);
    }
}