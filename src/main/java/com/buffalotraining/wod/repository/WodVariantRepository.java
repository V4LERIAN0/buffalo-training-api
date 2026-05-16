package com.buffalotraining.wod.repository;

import com.buffalotraining.wod.entity.ExecutionLevel;
import com.buffalotraining.wod.entity.GenderCategory;
import com.buffalotraining.wod.entity.WodVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WodVariantRepository extends JpaRepository<WodVariant, Long> {

    Optional<WodVariant> findByWodIdAndGenderCategoryAndExecutionLevel(
            Long wodId,
            GenderCategory genderCategory,
            ExecutionLevel executionLevel
    );
}