package com.buffalotraining.leaderboard.dto;

import com.buffalotraining.leaderboard.entity.ScoreStatus;
import com.buffalotraining.leaderboard.entity.WodScore;
import com.buffalotraining.wod.entity.ExecutionLevel;
import com.buffalotraining.wod.entity.GenderCategory;
import com.buffalotraining.wod.entity.ScoreType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class WodScoreResponse {

    private Long id;

    private Long wodId;
    private String wodTitle;
    private LocalDate wodDate;

    private Long wodVariantId;
    private GenderCategory genderCategory;
    private ExecutionLevel executionLevel;
    private ScoreType scoreType;

    private Long athleteId;
    private String athleteName;
    private String athleteEmail;

    private String scoreText;
    private Integer timeSeconds;
    private Integer reps;
    private Integer rounds;
    private BigDecimal weight;
    private BigDecimal distance;
    private Integer calories;

    private ScoreStatus status;
    private Boolean validForLeaderboard;
    private String notes;

    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;

    public static WodScoreResponse fromEntity(WodScore score) {
        return WodScoreResponse.builder()
                .id(score.getId())

                .wodId(score.getWod().getId())
                .wodTitle(score.getWod().getTitle())
                .wodDate(score.getWod().getWodDate())

                .wodVariantId(score.getWodVariant().getId())
                .genderCategory(score.getWodVariant().getGenderCategory())
                .executionLevel(score.getWodVariant().getExecutionLevel())
                .scoreType(score.getWodVariant().getScoreType())

                .athleteId(score.getAthlete().getId())
                .athleteName(score.getAthlete().getFirstName() + " " + score.getAthlete().getLastName())
                .athleteEmail(score.getAthlete().getEmail())

                .scoreText(score.getScoreText())
                .timeSeconds(score.getTimeSeconds())
                .reps(score.getReps())
                .rounds(score.getRounds())
                .weight(score.getWeight())
                .distance(score.getDistance())
                .calories(score.getCalories())

                .status(score.getStatus())
                .validForLeaderboard(score.getValidForLeaderboard())
                .notes(score.getNotes())

                .submittedAt(score.getSubmittedAt())
                .updatedAt(score.getUpdatedAt())
                .build();
    }
}