package com.buffalotraining.leaderboard.dto;

import com.buffalotraining.wod.entity.ExecutionLevel;
import com.buffalotraining.wod.entity.GenderCategory;
import com.buffalotraining.wod.entity.ScoreType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class LeaderboardResponse {

    private Long wodId;
    private String wodTitle;
    private LocalDate wodDate;

    private Long wodVariantId;
    private GenderCategory genderCategory;
    private ExecutionLevel executionLevel;
    private ScoreType scoreType;

    private List<LeaderboardEntryResponse> entries;
}