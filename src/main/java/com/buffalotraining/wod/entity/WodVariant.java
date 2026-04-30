package com.buffalotraining.wod.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "wod_variants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_wod_gender_level",
                        columnNames = {"wod_id", "gender_category", "execution_level"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WodVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wod_id", nullable = false)
    private Wod wod;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_category", nullable = false, length = 20)
    private GenderCategory genderCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_level", nullable = false, length = 20)
    private ExecutionLevel executionLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "score_type", nullable = false, length = 30)
    private ScoreType scoreType;

    @Column(name = "main_workout", nullable = false, columnDefinition = "TEXT")
    private String mainWorkout;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}