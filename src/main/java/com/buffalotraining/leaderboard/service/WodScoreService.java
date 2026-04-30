package com.buffalotraining.leaderboard.service;

import com.buffalotraining.leaderboard.dto.LeaderboardEntryResponse;
import com.buffalotraining.leaderboard.dto.LeaderboardResponse;
import com.buffalotraining.leaderboard.dto.SubmitWodScoreRequest;
import com.buffalotraining.leaderboard.dto.UpdateWodScoreRequest;
import com.buffalotraining.leaderboard.dto.WodScoreResponse;
import com.buffalotraining.leaderboard.entity.ScoreStatus;
import com.buffalotraining.leaderboard.entity.WodScore;
import com.buffalotraining.leaderboard.repository.WodScoreRepository;
import com.buffalotraining.membership.entity.Membership;
import com.buffalotraining.membership.entity.MembershipStatus;
import com.buffalotraining.membership.repository.MembershipRepository;
import com.buffalotraining.user.entity.User;
import com.buffalotraining.user.entity.UserStatus;
import com.buffalotraining.user.repository.UserRepository;
import com.buffalotraining.wod.entity.ScoreType;
import com.buffalotraining.wod.entity.WodStatus;
import com.buffalotraining.wod.entity.WodVariant;
import com.buffalotraining.wod.repository.WodVariantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class WodScoreService {

    private final WodScoreRepository wodScoreRepository;
    private final WodVariantRepository wodVariantRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public WodScoreResponse submitScore(SubmitWodScoreRequest request) {
        WodVariant variant = wodVariantRepository.findById(request.getWodVariantId())
                .orElseThrow(() -> new IllegalArgumentException("WOD variant not found with id: " + request.getWodVariantId()));

        if (variant.getWod().getStatus() != WodStatus.PUBLISHED) {
            throw new IllegalArgumentException("Scores can only be submitted for published WODs");
        }

        User athlete = userRepository.findById(request.getAthleteId())
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found with id: " + request.getAthleteId()));

        validateAthlete(athlete);
        validateAthleteMembership(athlete);
        validateScoreFields(variant.getScoreType(), request);

        if (wodScoreRepository.existsByWodIdAndAthleteIdAndStatus(
                variant.getWod().getId(),
                athlete.getId(),
                ScoreStatus.ACTIVE
        )) {
            throw new IllegalArgumentException("Athlete already has an active score for this WOD");
        }

        WodScore score = WodScore.builder()
                .wod(variant.getWod())
                .wodVariant(variant)
                .athlete(athlete)
                .scoreText(request.getScoreText().trim())
                .timeSeconds(request.getTimeSeconds())
                .reps(request.getReps())
                .rounds(request.getRounds())
                .weight(request.getWeight())
                .distance(request.getDistance())
                .calories(request.getCalories())
                .status(ScoreStatus.ACTIVE)
                .activeScoreKey("ACTIVE")
                .validForLeaderboard(true)
                .notes(request.getNotes())
                .build();

        WodScore savedScore = wodScoreRepository.save(score);

        return WodScoreResponse.fromEntity(savedScore);
    }

    public List<WodScoreResponse> getAllScores() {
        return wodScoreRepository.findAll()
                .stream()
                .map(WodScoreResponse::fromEntity)
                .toList();
    }

    public WodScoreResponse getScoreById(Long id) {
        WodScore score = findScoreEntityById(id);
        return WodScoreResponse.fromEntity(score);
    }

    public List<WodScoreResponse> getScoresByWod(Long wodId) {
        return wodScoreRepository.findByWodIdOrderBySubmittedAtDesc(wodId)
                .stream()
                .map(WodScoreResponse::fromEntity)
                .toList();
    }

    public List<WodScoreResponse> getScoresByAthlete(Long athleteId) {
        return wodScoreRepository.findByAthleteIdOrderBySubmittedAtDesc(athleteId)
                .stream()
                .map(WodScoreResponse::fromEntity)
                .toList();
    }

    @Transactional
    public WodScoreResponse updateScore(Long id, UpdateWodScoreRequest request) {
        WodScore score = findScoreEntityById(id);

        if (score.getStatus() == ScoreStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled scores cannot be updated");
        }

        validateUpdatedScoreFields(score.getWodVariant().getScoreType(), request);

        if (request.getScoreText() != null && !request.getScoreText().isBlank()) {
            score.setScoreText(request.getScoreText().trim());
        }

        if (request.getTimeSeconds() != null) {
            score.setTimeSeconds(request.getTimeSeconds());
        }

        if (request.getReps() != null) {
            score.setReps(request.getReps());
        }

        if (request.getRounds() != null) {
            score.setRounds(request.getRounds());
        }

        if (request.getWeight() != null) {
            score.setWeight(request.getWeight());
        }

        if (request.getDistance() != null) {
            score.setDistance(request.getDistance());
        }

        if (request.getCalories() != null) {
            score.setCalories(request.getCalories());
        }

        if (request.getNotes() != null) {
            score.setNotes(request.getNotes());
        }

        score.setStatus(ScoreStatus.ACTIVE);
        score.setActiveScoreKey("ACTIVE");
        score.setValidForLeaderboard(true);

        WodScore updatedScore = wodScoreRepository.save(score);

        return WodScoreResponse.fromEntity(updatedScore);
    }

    @Transactional
    public WodScoreResponse cancelScore(Long id) {
        WodScore score = findScoreEntityById(id);

        if (score.getStatus() == ScoreStatus.CANCELLED) {
            throw new IllegalArgumentException("Score is already cancelled");
        }

        score.setStatus(ScoreStatus.CANCELLED);
        score.setActiveScoreKey(null);
        score.setValidForLeaderboard(false);

        WodScore cancelledScore = wodScoreRepository.save(score);

        return WodScoreResponse.fromEntity(cancelledScore);
    }

    public LeaderboardResponse getLeaderboardByVariant(Long wodVariantId) {
        WodVariant variant = wodVariantRepository.findById(wodVariantId)
                .orElseThrow(() -> new IllegalArgumentException("WOD variant not found with id: " + wodVariantId));

        List<WodScore> scores = wodScoreRepository
                .findByWodVariantIdAndStatusAndValidForLeaderboardTrue(wodVariantId, ScoreStatus.ACTIVE);

        List<WodScore> sortedScores = sortScoresForLeaderboard(scores, variant.getScoreType());

        AtomicInteger position = new AtomicInteger(1);

        List<LeaderboardEntryResponse> entries = sortedScores.stream()
                .map(score -> LeaderboardEntryResponse.builder()
                        .position(position.getAndIncrement())
                        .score(WodScoreResponse.fromEntity(score))
                        .build())
                .toList();

        return LeaderboardResponse.builder()
                .wodId(variant.getWod().getId())
                .wodTitle(variant.getWod().getTitle())
                .wodDate(variant.getWod().getWodDate())
                .wodVariantId(variant.getId())
                .genderCategory(variant.getGenderCategory())
                .executionLevel(variant.getExecutionLevel())
                .scoreType(variant.getScoreType())
                .entries(entries)
                .build();
    }

    private WodScore findScoreEntityById(Long id) {
        return wodScoreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WOD score not found with id: " + id));
    }

    private void validateAthlete(User user) {
        if (!"ATHLETE".equals(user.getRole().getName())) {
            throw new IllegalArgumentException("Selected user is not an athlete");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Athlete user is not active");
        }
    }

    private void validateAthleteMembership(User athlete) {
        Membership membership = membershipRepository.findFirstByAthleteIdAndActiveTrueOrderByCreatedAtDesc(athlete.getId())
                .orElseThrow(() -> new IllegalArgumentException("Athlete does not have an active membership"));

        MembershipStatus status = calculateMembershipStatus(
                membership.getDueDate(),
                membership.getPlan().getGracePeriodDays()
        );

        if (membership.getStatus() != status) {
            membership.setStatus(status);
            membershipRepository.save(membership);
        }

        if (status == MembershipStatus.EXPIRED || status == MembershipStatus.CANCELLED) {
            throw new IllegalArgumentException("Athlete membership is not valid for score submission");
        }
    }

    private MembershipStatus calculateMembershipStatus(LocalDate dueDate, Integer gracePeriodDays) {
        LocalDate today = LocalDate.now();
        LocalDate expiringSoonStart = dueDate.minusDays(3);
        LocalDate graceLimitDate = dueDate.plusDays(gracePeriodDays);

        if (today.isBefore(expiringSoonStart)) {
            return MembershipStatus.ACTIVE;
        }

        if (!today.isAfter(graceLimitDate)) {
            return MembershipStatus.EXPIRING_SOON;
        }

        return MembershipStatus.EXPIRED;
    }

    private void validateScoreFields(ScoreType scoreType, SubmitWodScoreRequest request) {
        switch (scoreType) {
            case TIME -> {
                if (request.getTimeSeconds() == null) {
                    throw new IllegalArgumentException("timeSeconds is required for TIME scores");
                }
            }
            case REPS -> {
                if (request.getReps() == null) {
                    throw new IllegalArgumentException("reps is required for REPS scores");
                }
            }
            case WEIGHT -> {
                if (request.getWeight() == null) {
                    throw new IllegalArgumentException("weight is required for WEIGHT scores");
                }
            }
            case ROUNDS_REPS -> {
                if (request.getRounds() == null || request.getReps() == null) {
                    throw new IllegalArgumentException("rounds and reps are required for ROUNDS_REPS scores");
                }
            }
            case DISTANCE -> {
                if (request.getDistance() == null) {
                    throw new IllegalArgumentException("distance is required for DISTANCE scores");
                }
            }
            case CALORIES -> {
                if (request.getCalories() == null) {
                    throw new IllegalArgumentException("calories is required for CALORIES scores");
                }
            }
            case NO_SCORE -> {
                // No numeric field required.
            }
        }
    }

    private void validateUpdatedScoreFields(ScoreType scoreType, UpdateWodScoreRequest request) {
        switch (scoreType) {
            case TIME -> {
                if (request.getTimeSeconds() != null && request.getTimeSeconds() < 0) {
                    throw new IllegalArgumentException("timeSeconds cannot be negative");
                }
            }
            case REPS -> {
                if (request.getReps() != null && request.getReps() < 0) {
                    throw new IllegalArgumentException("reps cannot be negative");
                }
            }
            case WEIGHT -> {
                if (request.getWeight() != null && request.getWeight().signum() < 0) {
                    throw new IllegalArgumentException("weight cannot be negative");
                }
            }
            case ROUNDS_REPS -> {
                if (request.getRounds() != null && request.getRounds() < 0) {
                    throw new IllegalArgumentException("rounds cannot be negative");
                }
                if (request.getReps() != null && request.getReps() < 0) {
                    throw new IllegalArgumentException("reps cannot be negative");
                }
            }
            case DISTANCE -> {
                if (request.getDistance() != null && request.getDistance().signum() < 0) {
                    throw new IllegalArgumentException("distance cannot be negative");
                }
            }
            case CALORIES -> {
                if (request.getCalories() != null && request.getCalories() < 0) {
                    throw new IllegalArgumentException("calories cannot be negative");
                }
            }
            case NO_SCORE -> {
                // No numeric field required.
            }
        }
    }

    private List<WodScore> sortScoresForLeaderboard(List<WodScore> scores, ScoreType scoreType) {
        Comparator<WodScore> comparator = switch (scoreType) {
            case TIME -> Comparator.comparing(WodScore::getTimeSeconds);
            case REPS -> Comparator.comparing(WodScore::getReps, Comparator.nullsLast(Comparator.reverseOrder()));
            case WEIGHT -> Comparator.comparing(WodScore::getWeight, Comparator.nullsLast(Comparator.reverseOrder()));
            case ROUNDS_REPS -> Comparator
                    .comparing(WodScore::getRounds, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(WodScore::getReps, Comparator.nullsLast(Comparator.reverseOrder()));
            case DISTANCE -> Comparator.comparing(WodScore::getDistance, Comparator.nullsLast(Comparator.reverseOrder()));
            case CALORIES -> Comparator.comparing(WodScore::getCalories, Comparator.nullsLast(Comparator.reverseOrder()));
            case NO_SCORE -> Comparator.comparing(WodScore::getSubmittedAt);
        };

        return scores.stream()
                .sorted(comparator)
                .toList();
    }
}