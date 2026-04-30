package com.buffalotraining.schedule.controller;

import com.buffalotraining.schedule.dto.ClassSessionResponse;
import com.buffalotraining.schedule.dto.CreateClassSessionRequest;
import com.buffalotraining.schedule.dto.UpdateClassSessionRequest;
import com.buffalotraining.schedule.dto.UpdateClassSessionStatusRequest;
import com.buffalotraining.schedule.entity.ClassSessionStatus;
import com.buffalotraining.schedule.service.ClassSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/class-sessions")
@RequiredArgsConstructor
public class ClassSessionController {

    private final ClassSessionService classSessionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClassSessionResponse createClassSession(@Valid @RequestBody CreateClassSessionRequest request) {
        return classSessionService.createClassSession(request);
    }

    @GetMapping
    public List<ClassSessionResponse> getAllClassSessions() {
        return classSessionService.getAllClassSessions();
    }

    @GetMapping("/{id}")
    public ClassSessionResponse getClassSessionById(@PathVariable Long id) {
        return classSessionService.getClassSessionById(id);
    }

    @GetMapping("/date/{date}")
    public List<ClassSessionResponse> getClassSessionsByDate(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return classSessionService.getClassSessionsByDate(date);
    }

    @GetMapping("/status/{status}")
    public List<ClassSessionResponse> getClassSessionsByStatus(@PathVariable ClassSessionStatus status) {
        return classSessionService.getClassSessionsByStatus(status);
    }

    @GetMapping("/coach/{coachId}")
    public List<ClassSessionResponse> getClassSessionsByCoach(@PathVariable Long coachId) {
        return classSessionService.getClassSessionsByCoach(coachId);
    }

    @PutMapping("/{id}")
    public ClassSessionResponse updateClassSession(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClassSessionRequest request
    ) {
        return classSessionService.updateClassSession(id, request);
    }

    @PatchMapping("/{id}/status")
    public ClassSessionResponse updateClassSessionStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClassSessionStatusRequest request
    ) {
        return classSessionService.updateClassSessionStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClassSession(@PathVariable Long id) {
        classSessionService.deleteClassSession(id);
    }
}