package com.buffalotraining.attendance.controller;

import com.buffalotraining.attendance.dto.AttendanceResponse;
import com.buffalotraining.attendance.dto.CheckInRequest;
import com.buffalotraining.attendance.dto.ManualAttendanceRequest;
import com.buffalotraining.attendance.dto.ValidateAttendanceRequest;
import com.buffalotraining.attendance.entity.AttendanceStatus;
import com.buffalotraining.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponse checkIn(@Valid @RequestBody CheckInRequest request) {
        return attendanceService.checkIn(request);
    }

    @PostMapping("/manual")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponse manualAttendance(@Valid @RequestBody ManualAttendanceRequest request) {
        return attendanceService.manualAttendance(request);
    }

    @GetMapping
    public List<AttendanceResponse> getAllAttendance() {
        return attendanceService.getAllAttendance();
    }

    @GetMapping("/{id}")
    public AttendanceResponse getAttendanceById(@PathVariable Long id) {
        return attendanceService.getAttendanceById(id);
    }

    @GetMapping("/class-session/{classSessionId}")
    public List<AttendanceResponse> getAttendanceByClassSession(@PathVariable Long classSessionId) {
        return attendanceService.getAttendanceByClassSession(classSessionId);
    }

    @GetMapping("/athlete/{athleteId}")
    public List<AttendanceResponse> getAttendanceByAthlete(@PathVariable Long athleteId) {
        return attendanceService.getAttendanceByAthlete(athleteId);
    }

    @GetMapping("/status/{status}")
    public List<AttendanceResponse> getAttendanceByStatus(@PathVariable AttendanceStatus status) {
        return attendanceService.getAttendanceByStatus(status);
    }

    @PatchMapping("/{id}/validate")
    public AttendanceResponse validateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody ValidateAttendanceRequest request
    ) {
        return attendanceService.validateAttendance(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public AttendanceResponse cancelAttendance(@PathVariable Long id) {
        return attendanceService.cancelAttendance(id);
    }
}