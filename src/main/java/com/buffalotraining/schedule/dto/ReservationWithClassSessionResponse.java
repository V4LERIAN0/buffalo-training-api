package com.buffalotraining.schedule.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationWithClassSessionResponse {

    private String message;
    private ReservationResponse reservation;
    private ClassSessionResponse updatedClassSession;
}