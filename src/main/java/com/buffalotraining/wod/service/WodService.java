package com.buffalotraining.wod.service;

import com.buffalotraining.user.entity.User;
import com.buffalotraining.user.entity.UserStatus;
import com.buffalotraining.user.repository.UserRepository;
import com.buffalotraining.wod.dto.CreateWodRequest;
import com.buffalotraining.wod.dto.CreateWodVariantRequest;
import com.buffalotraining.wod.dto.UpdateWodRequest;
import com.buffalotraining.wod.dto.UpdateWodStatusRequest;
import com.buffalotraining.wod.dto.WodResponse;
import com.buffalotraining.wod.entity.Wod;
import com.buffalotraining.wod.entity.WodStatus;
import com.buffalotraining.wod.entity.WodVariant;
import com.buffalotraining.wod.repository.WodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.buffalotraining.wod.entity.ExecutionLevel;
import com.buffalotraining.wod.entity.GenderCategory;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WodService {

    private final WodRepository wodRepository;
    private final UserRepository userRepository;

    @Transactional
    public WodResponse createWod(CreateWodRequest request) {
        User creator = userRepository.findById(request.getCreatedByUserId())
                .orElseThrow(() -> new IllegalArgumentException("Creator user not found with id: " + request.getCreatedByUserId()));

        validateWodCreator(creator);

        WodStatus requestedStatus = request.getStatus() == null ? WodStatus.DRAFT : request.getStatus();

        if (requestedStatus == WodStatus.PUBLISHED &&
                wodRepository.existsByWodDateAndStatus(request.getWodDate(), WodStatus.PUBLISHED)) {
            throw new IllegalArgumentException("There is already a published WOD for this date");
        }

        validateVariantCombinations(request.getVariants());

        Wod wod = Wod.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .wodDate(request.getWodDate())
                .wodType(request.getWodType())
                .status(requestedStatus)
                .warmUp(request.getWarmUp())
                .skillOrStrength(request.getSkillOrStrength())
                .videoUrl(request.getVideoUrl())
                .movementNotes(request.getMovementNotes())
                .createdBy(creator)
                .build();

        List<WodVariant> variants = buildVariantsForWod(wod, request.getVariants());
        wod.getVariants().addAll(variants);

        Wod savedWod = wodRepository.save(wod);

        return WodResponse.fromEntity(savedWod);
    }

    public List<WodResponse> getAllWods() {
        return wodRepository.findByOrderByWodDateDesc()
                .stream()
                .map(WodResponse::fromEntity)
                .toList();
    }

    public List<WodResponse> getPublishedWods() {
        return wodRepository.findByStatusOrderByWodDateDesc(WodStatus.PUBLISHED)
                .stream()
                .map(WodResponse::fromEntity)
                .toList();
    }

    public WodResponse getWodById(Long id) {
        Wod wod = findWodEntityById(id);
        return WodResponse.fromEntity(wod);
    }

    public WodResponse getPublishedWodByDate(LocalDate date) {
        Wod wod = wodRepository.findFirstByWodDateAndStatus(date, WodStatus.PUBLISHED)
                .orElseThrow(() -> new IllegalArgumentException("No published WOD found for date: " + date));

        return WodResponse.fromEntity(wod);
    }

    public List<WodResponse> getWodsByDate(LocalDate date) {
        return wodRepository.findByWodDateOrderByCreatedAtDesc(date)
                .stream()
                .map(WodResponse::fromEntity)
                .toList();
    }

    @Transactional
    public WodResponse updateWod(Long id, UpdateWodRequest request) {
        Wod wod = findWodEntityById(id);

        LocalDate finalWodDate = request.getWodDate() != null ? request.getWodDate() : wod.getWodDate();
        WodStatus finalStatus = request.getStatus() != null ? request.getStatus() : wod.getStatus();

        validateSinglePublishedWodPerDate(wod, finalStatus, finalWodDate);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            wod.setTitle(request.getTitle().trim());
        }

        if (request.getDescription() != null) {
            wod.setDescription(request.getDescription());
        }

        if (request.getWodDate() != null) {
            wod.setWodDate(request.getWodDate());
        }

        if (request.getWodType() != null) {
            wod.setWodType(request.getWodType());
        }

        if (request.getStatus() != null) {
            wod.setStatus(request.getStatus());
        }

        if (request.getWarmUp() != null) {
            wod.setWarmUp(request.getWarmUp());
        }

        if (request.getSkillOrStrength() != null) {
            wod.setSkillOrStrength(request.getSkillOrStrength());
        }

        if (request.getVideoUrl() != null) {
            wod.setVideoUrl(request.getVideoUrl());
        }

        if (request.getMovementNotes() != null) {
            wod.setMovementNotes(request.getMovementNotes());
        }

        if (request.getVariants() != null) {
            if (request.getVariants().isEmpty()) {
                throw new IllegalArgumentException("At least one WOD variant is required");
            }

            validateVariantCombinations(request.getVariants());

            updateWodVariants(wod, request.getVariants());
        }

        Wod updatedWod = wodRepository.save(wod);

        return WodResponse.fromEntity(updatedWod);
    }

    public WodResponse updateWodStatus(Long id, UpdateWodStatusRequest request) {
        Wod wod = findWodEntityById(id);

        validateSinglePublishedWodPerDate(wod, request.getStatus(), wod.getWodDate());

        wod.setStatus(request.getStatus());

        Wod updatedWod = wodRepository.save(wod);

        return WodResponse.fromEntity(updatedWod);
    }

    public void deleteWod(Long id) {
        Wod wod = findWodEntityById(id);
        wodRepository.delete(wod);
    }

    private Wod findWodEntityById(Long id) {
        return wodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WOD not found with id: " + id));
    }

    private void validateWodCreator(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Creator user is not active");
        }

        String roleName = user.getRole().getName();

        if (!"ADMIN".equals(roleName) && !"COACH".equals(roleName)) {
            throw new IllegalArgumentException("Only an admin or coach can create WODs");
        }
    }

    private void validateSinglePublishedWodPerDate(Wod currentWod, WodStatus targetStatus, LocalDate targetDate) {
        if (targetStatus != WodStatus.PUBLISHED) {
            return;
        }

        wodRepository.findFirstByWodDateAndStatus(targetDate, WodStatus.PUBLISHED)
                .ifPresent(existingWod -> {
                    if (!existingWod.getId().equals(currentWod.getId())) {
                        throw new IllegalArgumentException("There is already a published WOD for this date");
                    }
                });
    }

    private List<WodVariant> buildVariantsForWod(Wod wod, List<CreateWodVariantRequest> variantRequests) {
        return variantRequests
                .stream()
                .map(variantRequest -> WodVariant.builder()
                        .wod(wod)
                        .genderCategory(variantRequest.getGenderCategory())
                        .executionLevel(variantRequest.getExecutionLevel())
                        .scoreType(variantRequest.getScoreType())
                        .mainWorkout(variantRequest.getMainWorkout())
                        .notes(variantRequest.getNotes())
                        .build())
                .toList();
    }

    private void validateVariantCombinations(List<CreateWodVariantRequest> variants) {
        Set<String> combinations = new HashSet<>();

        for (CreateWodVariantRequest variant : variants) {
            String combination = variant.getGenderCategory() + "-" + variant.getExecutionLevel();

            if (!combinations.add(combination)) {
                throw new IllegalArgumentException(
                        "Duplicate WOD variant for " + variant.getGenderCategory() + " " + variant.getExecutionLevel()
                );
            }
        }
    }

    private void updateWodVariants(Wod wod, List<CreateWodVariantRequest> variantRequests) {
        Map<String, WodVariant> existingVariantsByCombination = new HashMap<>();

        for (WodVariant existingVariant : wod.getVariants()) {
            String key = buildVariantKey(
                    existingVariant.getGenderCategory(),
                    existingVariant.getExecutionLevel()
            );

            existingVariantsByCombination.put(key, existingVariant);
        }

        List<WodVariant> updatedVariants = new ArrayList<>();

        for (CreateWodVariantRequest variantRequest : variantRequests) {
            String key = buildVariantKey(
                    variantRequest.getGenderCategory(),
                    variantRequest.getExecutionLevel()
            );

            WodVariant variant = existingVariantsByCombination.get(key);

            if (variant == null) {
                variant = WodVariant.builder()
                        .wod(wod)
                        .genderCategory(variantRequest.getGenderCategory())
                        .executionLevel(variantRequest.getExecutionLevel())
                        .scoreType(variantRequest.getScoreType())
                        .mainWorkout(variantRequest.getMainWorkout())
                        .notes(variantRequest.getNotes())
                        .build();
            } else {
                variant.setScoreType(variantRequest.getScoreType());
                variant.setMainWorkout(variantRequest.getMainWorkout());
                variant.setNotes(variantRequest.getNotes());
            }

            updatedVariants.add(variant);
        }

        wod.getVariants().clear();
        wod.getVariants().addAll(updatedVariants);
    }

    private String buildVariantKey(GenderCategory genderCategory, ExecutionLevel executionLevel) {
        return genderCategory.name() + "-" + executionLevel.name();
    }
}