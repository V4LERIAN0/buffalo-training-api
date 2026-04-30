package com.buffalotraining.leaderboard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LeaderboardEntryResponse {

    private Integer position;
    private WodScoreResponse score;
}