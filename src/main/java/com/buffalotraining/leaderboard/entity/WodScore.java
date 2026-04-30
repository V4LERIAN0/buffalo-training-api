package com.buffalotraining.leaderboard.entity;

import com.buffalotraining.user.entity.User;
import com.buffalotraining.wod.entity.Wod;
import com.buffalotraining.wod.entity.WodVariant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "wod_scores",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_active_score_per_wod",
                        columnNames = {"wod_id", "athlete_id", "active_score_key"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WodScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Parent WOD for easier querying.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wod_id", nullable = false)
    private Wod wod;

    /*
     * Specific category/version: MALE RX, FEMALE RX, etc.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wod_variant_id", nullable = false)
    private WodVariant wodVariant;

    /*
     * Athlete who submitted the score.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "athlete_id", nullable = false)
    private User athlete;

    /*
     * Human-readable score.
     * Examples:
     * "12:35"
     * "7 rounds + 12 reps"
     * "185 lb"
     */
    @Column(name = "score_text", nullable = false, length = 120)
    private String scoreText;

    /*
     * Normalized numeric fields for sorting.
     * Depending on score type, only some fields are used.
     */
    @Column(name = "time_seconds")
    private Integer timeSeconds;

    @Column(name = "reps")
    private Integer reps;

    @Column(name = "rounds")
    private Integer rounds;

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "distance", precision = 10, scale = 2)
    private BigDecimal distance;

    @Column(name = "calories")
    private Integer calories;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ScoreStatus status;

    @Column(name = "active_score_key", length = 20)
    private String activeScoreKey;

    @Column(name = "valid_for_leaderboard", nullable = false)
    private Boolean validForLeaderboard;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.submittedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = ScoreStatus.ACTIVE;
        }

        if (this.validForLeaderboard == null) {
            this.validForLeaderboard = true;
        }

        if (this.status == ScoreStatus.ACTIVE) {
            this.activeScoreKey = "ACTIVE";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}