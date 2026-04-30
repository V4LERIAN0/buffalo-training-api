package com.buffalotraining.wod.dto;

import com.buffalotraining.wod.entity.ExecutionLevel;
import com.buffalotraining.wod.entity.GenderCategory;
import com.buffalotraining.wod.entity.ScoreType;
import com.buffalotraining.wod.entity.WodVariant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WodVariantResponse {

    private Long id;
    private GenderCategory genderCategory;
    private ExecutionLevel executionLevel;
    private ScoreType scoreType;
    private String mainWorkout;
    private String notes;

    public static WodVariantResponse fromEntity(WodVariant variant) {
        return WodVariantResponse.builder()
                .id(variant.getId())
                .genderCategory(variant.getGenderCategory())
                .executionLevel(variant.getExecutionLevel())
                .scoreType(variant.getScoreType())
                .mainWorkout(variant.getMainWorkout())
                .notes(variant.getNotes())
                .build();
    }
}