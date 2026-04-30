package com.buffalotraining.membership.service;

import com.buffalotraining.membership.dto.CreateMembershipPlanRequest;
import com.buffalotraining.membership.dto.MembershipPlanResponse;
import com.buffalotraining.membership.dto.UpdateMembershipPlanRequest;
import com.buffalotraining.membership.dto.UpdateMembershipPlanStatusRequest;
import com.buffalotraining.membership.entity.MembershipPlan;
import com.buffalotraining.membership.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipPlanService {

    private final MembershipPlanRepository membershipPlanRepository;

    public MembershipPlanResponse createPlan(CreateMembershipPlanRequest request) {
        String normalizedName = request.getName().trim();

        if (membershipPlanRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException("A membership plan with this name already exists");
        }

        MembershipPlan plan = MembershipPlan.builder()
                .name(normalizedName)
                .description(request.getDescription())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .billingCycle(request.getBillingCycle())
                .gracePeriodDays(request.getGracePeriodDays())
                .active(true)
                .build();

        MembershipPlan savedPlan = membershipPlanRepository.save(plan);

        return MembershipPlanResponse.fromEntity(savedPlan);
    }

    public List<MembershipPlanResponse> getAllPlans() {
        return membershipPlanRepository.findAll()
                .stream()
                .map(MembershipPlanResponse::fromEntity)
                .toList();
    }

    public List<MembershipPlanResponse> getActivePlans() {
        return membershipPlanRepository.findByActiveTrue()
                .stream()
                .map(MembershipPlanResponse::fromEntity)
                .toList();
    }

    public MembershipPlanResponse getPlanById(Long id) {
        MembershipPlan plan = findPlanEntityById(id);
        return MembershipPlanResponse.fromEntity(plan);
    }

    public MembershipPlanResponse updatePlan(Long id, UpdateMembershipPlanRequest request) {
        MembershipPlan plan = findPlanEntityById(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            String normalizedName = request.getName().trim();

            boolean nameBelongsToAnotherPlan = membershipPlanRepository.findByNameIgnoreCase(normalizedName)
                    .map(existingPlan -> !existingPlan.getId().equals(id))
                    .orElse(false);

            if (nameBelongsToAnotherPlan) {
                throw new IllegalArgumentException("A membership plan with this name already exists");
            }

            plan.setName(normalizedName);
        }

        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }

        if (request.getDurationDays() != null) {
            plan.setDurationDays(request.getDurationDays());
        }

        if (request.getBillingCycle() != null) {
            plan.setBillingCycle(request.getBillingCycle());
        }

        if (request.getGracePeriodDays() != null) {
            plan.setGracePeriodDays(request.getGracePeriodDays());
        }

        MembershipPlan updatedPlan = membershipPlanRepository.save(plan);

        return MembershipPlanResponse.fromEntity(updatedPlan);
    }

    public MembershipPlanResponse updatePlanStatus(Long id, UpdateMembershipPlanStatusRequest request) {
        MembershipPlan plan = findPlanEntityById(id);
        plan.setActive(request.getActive());

        MembershipPlan updatedPlan = membershipPlanRepository.save(plan);

        return MembershipPlanResponse.fromEntity(updatedPlan);
    }

    private MembershipPlan findPlanEntityById(Long id) {
        return membershipPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membership plan not found with id: " + id));
    }
}