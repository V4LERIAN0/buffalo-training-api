package com.buffalotraining.wod.entity;

import com.buffalotraining.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "wod_date", nullable = false)
    private LocalDate wodDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "wod_type", nullable = false, length = 30)
    private WodType wodType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private WodStatus status;

    @Column(name = "warm_up", columnDefinition = "TEXT")
    private String warmUp;

    @Column(name = "skill_or_strength", columnDefinition = "TEXT")
    private String skillOrStrength;

    @Column(name = "video_url", length = 255)
    private String videoUrl;

    @Column(name = "movement_notes", columnDefinition = "TEXT")
    private String movementNotes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "wod", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WodVariant> variants = new ArrayList<>();

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = WodStatus.DRAFT;
        }

        if (this.status == WodStatus.PUBLISHED && this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();

        if (this.status == WodStatus.PUBLISHED && this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }
}