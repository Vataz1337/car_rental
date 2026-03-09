package com.carrental.dto;

import com.carrental.enums.CarType;
import com.carrental.model.Reservation;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReservationResponse(
        UUID reservationId,
        String carModel,
        CarType carType,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
    public static ReservationResponse from(Reservation res) {
        return ReservationResponse.builder()
                .reservationId(res.getId())
                .carModel(res.getCar().getModel())
                .carType(res.getCar().getType())
                .startDate(res.getStartDate())
                .endDate(res.getEndDate())
                .build();
    }
}