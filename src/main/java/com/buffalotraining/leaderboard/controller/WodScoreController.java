package com.buffalotraining.leaderboard.controller;

import com.buffalotraining.leaderboard.dto.LeaderboardResponse;
import com.buffalotraining.leaderboard.dto.SubmitWodScoreRequest;
import com.buffalotraining.leaderboard.dto.UpdateWodScoreRequest;
import com.buffalotraining.leaderboard.dto.WodScoreResponse;
import com.buffalotraining.leaderboard.service.WodScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wod-scores")
@RequiredArgsConstructor
public class WodScoreController {

    private final WodScoreService wodScoreService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WodScoreResponse submitScore(@Valid @RequestBody SubmitWodScoreRequest request) {
        return wodScoreService.submitScore(request);
    }

    @GetMapping
    public List<WodScoreResponse> getAllScores() {
        return wodScoreService.getAllScores();
    }

    @GetMapping("/{id}")
    public WodScoreResponse getScoreById(@PathVariable Long id) {
        return wodScoreService.getScoreById(id);
    }

    @GetMapping("/wod/{wodId}")
    public List<WodScoreResponse> getScoresByWod(@PathVariable Long wodId) {
        return wodScoreService.getScoresByWod(wodId);
    }

    @GetMapping("/athlete/{athleteId}")
    public List<WodScoreResponse> getScoresByAthlete(@PathVariable Long athleteId) {
        return wodScoreService.getScoresByAthlete(athleteId);
    }

    @PutMapping("/{id}")
    public WodScoreResponse updateScore(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWodScoreRequest request
    ) {
        return wodScoreService.updateScore(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public WodScoreResponse cancelScore(@PathVariable Long id) {
        return wodScoreService.cancelScore(id);
    }

    @GetMapping("/leaderboard/variant/{wodVariantId}")
    public LeaderboardResponse getLeaderboardByVariant(@PathVariable Long wodVariantId) {
        return wodScoreService.getLeaderboardByVariant(wodVariantId);
    }
}