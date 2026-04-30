package com.buffalotraining.membership.repository;

import com.buffalotraining.membership.entity.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {

    Optional<MembershipPlan> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<MembershipPlan> findByActiveTrue();
}