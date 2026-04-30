package com.buffalotraining.schedule.controller;

import com.buffalotraining.schedule.dto.CreateReservationRequest;
import com.buffalotraining.schedule.dto.ReservationResponse;
import com.buffalotraining.schedule.dto.ReservationWithClassSessionResponse;
import com.buffalotraining.schedule.entity.ReservationStatus;
import com.buffalotraining.schedule.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationWithClassSessionResponse createReservation(
            @Valid @RequestBody CreateReservationRequest request
    ) {
        return reservationService.createReservation(request);
    }

    @GetMapping
    public List<ReservationResponse> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/{id}")
    public ReservationResponse getReservationById(@PathVariable Long id) {
        return reservationService.getReservationById(id);
    }

    @GetMapping("/class-session/{classSessionId}")
    public List<ReservationResponse> getReservationsByClassSession(@PathVariable Long classSessionId) {
        return reservationService.getReservationsByClassSession(classSessionId);
    }

    @GetMapping("/athlete/{athleteId}")
    public List<ReservationResponse> getReservationsByAthlete(@PathVariable Long athleteId) {
        return reservationService.getReservationsByAthlete(athleteId);
    }

    @GetMapping("/status/{status}")
    public List<ReservationResponse> getReservationsByStatus(@PathVariable ReservationStatus status) {
        return reservationService.getReservationsByStatus(status);
    }

    @PatchMapping("/{id}/cancel")
    public ReservationWithClassSessionResponse cancelReservation(@PathVariable Long id) {
        return reservationService.cancelReservation(id);
    }

    @PatchMapping("/{id}/no-show")
    public ReservationWithClassSessionResponse markNoShow(@PathVariable Long id) {
        return reservationService.markNoShow(id);
    }
}