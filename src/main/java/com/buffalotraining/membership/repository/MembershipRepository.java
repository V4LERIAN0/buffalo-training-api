package com.buffalotraining.membership.repository;

import com.buffalotraining.membership.entity.Membership;
import com.buffalotraining.membership.entity.MembershipStatus;
import com.buffalotraining.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByAthlete(User athlete);

    List<Membership> findByAthleteId(Long athleteId);

    Optional<Membership> findFirstByAthleteIdAndActiveTrueOrderByCreatedAtDesc(Long athleteId);

    List<Membership> findByStatus(MembershipStatus status);

    boolean existsByAthleteIdAndActiveTrue(Long athleteId);
}