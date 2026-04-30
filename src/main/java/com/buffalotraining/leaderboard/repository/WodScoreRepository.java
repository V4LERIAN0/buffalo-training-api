package com.buffalotraining.leaderboard.repository;

import com.buffalotraining.leaderboard.entity.ScoreStatus;
import com.buffalotraining.leaderboard.entity.WodScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WodScoreRepository extends JpaRepository<WodScore, Long> {

    List<WodScore> findByWodIdOrderBySubmittedAtDesc(Long wodId);

    List<WodScore> findByWodVariantIdAndStatusAndValidForLeaderboardTrue(Long wodVariantId, ScoreStatus status);

    List<WodScore> findByAthleteIdOrderBySubmittedAtDesc(Long athleteId);

    Optional<WodScore> findFirstByWodVariantIdAndAthleteIdAndStatus(
            Long wodVariantId,
            Long athleteId,
            ScoreStatus status
    );

    boolean existsByWodVariantIdAndAthleteIdAndStatus(
            Long wodVariantId,
            Long athleteId,
            ScoreStatus status
    );

    Optional<WodScore> findFirstByWodIdAndAthleteIdAndStatus(
            Long wodId,
            Long athleteId,
            ScoreStatus status
    );

    boolean existsByWodIdAndAthleteIdAndStatus(
            Long wodId,
            Long athleteId,
            ScoreStatus status
    );
}