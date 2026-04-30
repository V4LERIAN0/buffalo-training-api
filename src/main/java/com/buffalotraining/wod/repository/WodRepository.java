package com.buffalotraining.wod.repository;

import com.buffalotraining.wod.entity.Wod;
import com.buffalotraining.wod.entity.WodStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WodRepository extends JpaRepository<Wod, Long> {

    List<Wod> findByOrderByWodDateDesc();

    List<Wod> findByStatusOrderByWodDateDesc(WodStatus status);

    Optional<Wod> findFirstByWodDateAndStatus(LocalDate wodDate, WodStatus status);

    List<Wod> findByWodDateOrderByCreatedAtDesc(LocalDate wodDate);

    boolean existsByWodDateAndStatus(LocalDate wodDate, WodStatus status);
}