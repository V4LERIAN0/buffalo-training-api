package com.buffalotraining.membership.service;

import com.buffalotraining.membership.dto.AssignMembershipRequest;
import com.buffalotraining.membership.dto.MembershipResponse;
import com.buffalotraining.membership.dto.UpdateMembershipStatusRequest;
import com.buffalotraining.membership.entity.Membership;
import com.buffalotraining.membership.entity.MembershipPlan;
import com.buffalotraining.membership.entity.MembershipStatus;
import com.buffalotraining.membership.repository.MembershipPlanRepository;
import com.buffalotraining.membership.repository.MembershipRepository;
import com.buffalotraining.user.entity.User;
import com.buffalotraining.user.entity.UserStatus;
import com.buffalotraining.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final UserRepository userRepository;

    public MembershipResponse assignMembership(AssignMembershipRequest request) {
        User athlete = userRepository.findById(request.getAthleteId())
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found with id: " + request.getAthleteId()));

        validateAthlete(athlete);

        MembershipPlan plan = membershipPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("Membership plan not found with id: " + request.getPlanId()));

        if (!Boolean.TRUE.equals(plan.getActive())) {
            throw new IllegalArgumentException("Cannot assign an inactive membership plan");
        }

        if (membershipRepository.existsByAthleteIdAndActiveTrue(athlete.getId())) {
            throw new IllegalArgumentException("This athlete already has an active membership");
        }

        LocalDate startDate = request.getStartDate();
        LocalDate dueDate = calculateNextDueDate(startDate, plan);

        Membership membership = Membership.builder()
                .athlete(athlete)
                .plan(plan)
                .startDate(startDate)
                .dueDate(dueDate)
                .basePaymentDate(startDate)
                .status(calculateStatus(dueDate, plan.getGracePeriodDays()))
                .active(true)
                .notes(request.getNotes())
                .build();

        Membership savedMembership = membershipRepository.save(membership);

        return MembershipResponse.fromEntity(savedMembership);
    }

    public List<MembershipResponse> getAllMemberships() {
        return membershipRepository.findAll()
                .stream()
                .peek(this::refreshMembershipStatusIfNeeded)
                .map(MembershipResponse::fromEntity)
                .toList();
    }

    public MembershipResponse getMembershipById(Long id) {
        Membership membership = findMembershipEntityById(id);
        refreshMembershipStatusIfNeeded(membership);
        return MembershipResponse.fromEntity(membership);
    }

    public List<MembershipResponse> getMembershipsByAthlete(Long athleteId) {
        return membershipRepository.findByAthleteId(athleteId)
                .stream()
                .peek(this::refreshMembershipStatusIfNeeded)
                .map(MembershipResponse::fromEntity)
                .toList();
    }

    public MembershipResponse getActiveMembershipByAthlete(Long athleteId) {
        Membership membership = membershipRepository.findFirstByAthleteIdAndActiveTrueOrderByCreatedAtDesc(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("No active membership found for athlete id: " + athleteId));

        refreshMembershipStatusIfNeeded(membership);

        return MembershipResponse.fromEntity(membership);
    }

    public MembershipResponse updateMembershipStatus(Long id, UpdateMembershipStatusRequest request) {
        Membership membership = findMembershipEntityById(id);

        membership.setStatus(request.getStatus());

        if (request.getStatus() == MembershipStatus.CANCELLED) {
            membership.setActive(false);
        }

        Membership updatedMembership = membershipRepository.save(membership);

        return MembershipResponse.fromEntity(updatedMembership);
    }

    public MembershipResponse cancelMembership(Long id) {
        Membership membership = findMembershipEntityById(id);

        membership.setStatus(MembershipStatus.CANCELLED);
        membership.setActive(false);

        Membership cancelledMembership = membershipRepository.save(membership);

        return MembershipResponse.fromEntity(cancelledMembership);
    }

    private Membership findMembershipEntityById(Long id) {
        return membershipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membership not found with id: " + id));
    }

    private void validateAthlete(User user) {
        if (!"ATHLETE".equals(user.getRole().getName())) {
            throw new IllegalArgumentException("Selected user is not an athlete");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot assign membership to an inactive or suspended athlete");
        }
    }

    private MembershipStatus calculateStatus(LocalDate dueDate, Integer gracePeriodDays) {
        LocalDate today = LocalDate.now();
        LocalDate expiringSoonStart = dueDate.minusDays(3);
        LocalDate graceLimitDate = dueDate.plusDays(gracePeriodDays);

        if (today.isBefore(expiringSoonStart)) {
            return MembershipStatus.ACTIVE;
        }

        if (!today.isAfter(graceLimitDate)) {
            return MembershipStatus.EXPIRING_SOON;
        }

        return MembershipStatus.EXPIRED;
    }

    private void refreshMembershipStatusIfNeeded(Membership membership) {
        if (!Boolean.TRUE.equals(membership.getActive())) {
            return;
        }

        MembershipStatus calculatedStatus = calculateStatus(
                membership.getDueDate(),
                membership.getPlan().getGracePeriodDays()
        );

        if (membership.getStatus() != calculatedStatus) {
            membership.setStatus(calculatedStatus);
            membershipRepository.save(membership);
        }
    }

    private LocalDate calculateNextDueDate(LocalDate currentDate, MembershipPlan plan) {
        return switch (plan.getBillingCycle()) {
            case MONTHLY -> currentDate.plusMonths(1);
            case DAY_BASED -> currentDate.plusDays(plan.getDurationDays());
        };
    }
}