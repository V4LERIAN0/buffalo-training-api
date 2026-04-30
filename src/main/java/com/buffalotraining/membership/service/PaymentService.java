package com.buffalotraining.membership.service;

import com.buffalotraining.membership.dto.PaymentResponse;
import com.buffalotraining.membership.dto.PaymentWithMembershipResponse;
import com.buffalotraining.membership.dto.RegisterMembershipPaymentRequest;
import com.buffalotraining.membership.entity.Membership;
import com.buffalotraining.membership.entity.MembershipStatus;
import com.buffalotraining.membership.entity.Payment;
import com.buffalotraining.membership.entity.PaymentStatus;
import com.buffalotraining.membership.repository.MembershipRepository;
import com.buffalotraining.membership.repository.PaymentRepository;
import com.buffalotraining.user.entity.User;
import com.buffalotraining.user.entity.UserStatus;
import com.buffalotraining.user.repository.UserRepository;
import com.buffalotraining.membership.dto.MembershipResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.buffalotraining.membership.entity.MembershipPlan;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public PaymentWithMembershipResponse registerMembershipPayment(RegisterMembershipPaymentRequest request) {
        Membership membership = membershipRepository.findById(request.getMembershipId())
                .orElseThrow(() -> new IllegalArgumentException("Membership not found with id: " + request.getMembershipId()));

        if (membership.getStatus() == MembershipStatus.CANCELLED || !Boolean.TRUE.equals(membership.getActive())) {
            throw new IllegalArgumentException("Cannot register payment for a cancelled or inactive membership");
        }

        User athlete = membership.getAthlete();

        User registeredBy = userRepository.findById(request.getRegisteredByUserId())
                .orElseThrow(() -> new IllegalArgumentException("Registering user not found with id: " + request.getRegisteredByUserId()));

        validateRegisteringUser(registeredBy);

        Payment payment = Payment.builder()
                .membership(membership)
                .athlete(athlete)
                .registeredBy(registeredBy)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.APPROVED)
                .referenceCode(request.getReferenceCode())
                .notes(request.getNotes())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        extendMembershipAfterPayment(membership);

        Membership updatedMembership = membershipRepository.save(membership);

        return PaymentWithMembershipResponse.builder()
                .message("Membership payment registered successfully")
                .payment(PaymentResponse.fromEntity(savedPayment))
                .updatedMembership(MembershipResponse.fromEntity(updatedMembership))
                .build();
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(PaymentResponse::fromEntity)
                .toList();
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with id: " + id));

        return PaymentResponse.fromEntity(payment);
    }

    public List<PaymentResponse> getPaymentsByMembership(Long membershipId) {
        return paymentRepository.findByMembershipIdOrderByPaymentDateDesc(membershipId)
                .stream()
                .map(PaymentResponse::fromEntity)
                .toList();
    }

    public List<PaymentResponse> getPaymentsByAthlete(Long athleteId) {
        return paymentRepository.findByAthleteIdOrderByPaymentDateDesc(athleteId)
                .stream()
                .map(PaymentResponse::fromEntity)
                .toList();
    }

    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatusOrderByPaymentDateDesc(status)
                .stream()
                .map(PaymentResponse::fromEntity)
                .toList();
    }

    private LocalDate calculateNextDueDate(LocalDate currentDate, MembershipPlan plan) {
        return switch (plan.getBillingCycle()) {
            case MONTHLY -> currentDate.plusMonths(1);
            case DAY_BASED -> currentDate.plusDays(plan.getDurationDays());
        };
    }

    private void extendMembershipAfterPayment(Membership membership) {
        LocalDate currentDueDate = membership.getDueDate();
        LocalDate newDueDate = calculateNextDueDate(currentDueDate, membership.getPlan());

        /*
         * Key Buffalo Training rule:
         * The new due date is calculated from the current due date,
         * NOT from the payment date.
         *
         * Example:
         * Current due date: Feb 1
         * Athlete pays late: Feb 10
         * New due date: Mar 3 if duration is 30 days
         * NOT Mar 10.
         *
         * This prevents late payments from moving the athlete's base payment cycle.
         */

        membership.setDueDate(newDueDate);
        membership.setStatus(calculateStatus(newDueDate, membership.getPlan().getGracePeriodDays()));
        membership.setActive(true);
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

    private void validateRegisteringUser(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("The user registering the payment is not active");
        }

        String roleName = user.getRole().getName();

        if (!"ADMIN".equals(roleName) && !"COACH".equals(roleName)) {
            throw new IllegalArgumentException("Only an admin or coach can register payments");
        }
    }
}