package com.buffalotraining.wod.controller;

import com.buffalotraining.wod.dto.CreateWodRequest;
import com.buffalotraining.wod.dto.UpdateWodRequest;
import com.buffalotraining.wod.dto.UpdateWodStatusRequest;
import com.buffalotraining.wod.dto.WodResponse;
import com.buffalotraining.wod.service.WodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/wods")
@RequiredArgsConstructor
public class WodController {

    private final WodService wodService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WodResponse createWod(@Valid @RequestBody CreateWodRequest request) {
        return wodService.createWod(request);
    }

    @GetMapping
    public List<WodResponse> getAllWods() {
        return wodService.getAllWods();
    }

    @GetMapping("/published")
    public List<WodResponse> getPublishedWods() {
        return wodService.getPublishedWods();
    }

    @GetMapping("/{id}")
    public WodResponse getWodById(@PathVariable Long id) {
        return wodService.getWodById(id);
    }

    @GetMapping("/date/{date}/published")
    public WodResponse getPublishedWodByDate(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return wodService.getPublishedWodByDate(date);
    }

    @GetMapping("/date/{date}")
    public List<WodResponse> getWodsByDate(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return wodService.getWodsByDate(date);
    }

    @PutMapping("/{id}")
    public WodResponse updateWod(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWodRequest request
    ) {
        return wodService.updateWod(id, request);
    }

    @PatchMapping("/{id}/status")
    public WodResponse updateWodStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWodStatusRequest request
    ) {
        return wodService.updateWodStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWod(@PathVariable Long id) {
        wodService.deleteWod(id);
    }
}