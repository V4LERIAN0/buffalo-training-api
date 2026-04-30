package com.buffalotraining.wod.dto;

import com.buffalotraining.wod.entity.Wod;
import com.buffalotraining.wod.entity.WodStatus;
import com.buffalotraining.wod.entity.WodType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class WodResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate wodDate;
    private WodType wodType;
    private WodStatus status;

    private String warmUp;
    private String skillOrStrength;
    private String videoUrl;
    private String movementNotes;

    private Long createdByUserId;
    private String createdByName;

    private List<WodVariantResponse> variants;

    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WodResponse fromEntity(Wod wod) {
        return WodResponse.builder()
                .id(wod.getId())
                .title(wod.getTitle())
                .description(wod.getDescription())
                .wodDate(wod.getWodDate())
                .wodType(wod.getWodType())
                .status(wod.getStatus())
                .warmUp(wod.getWarmUp())
                .skillOrStrength(wod.getSkillOrStrength())
                .videoUrl(wod.getVideoUrl())
                .movementNotes(wod.getMovementNotes())
                .createdByUserId(wod.getCreatedBy().getId())
                .createdByName(wod.getCreatedBy().getFirstName() + " " + wod.getCreatedBy().getLastName())
                .variants(
                        wod.getVariants()
                                .stream()
                                .map(WodVariantResponse::fromEntity)
                                .toList()
                )
                .publishedAt(wod.getPublishedAt())
                .createdAt(wod.getCreatedAt())
                .updatedAt(wod.getUpdatedAt())
                .build();
    }
}