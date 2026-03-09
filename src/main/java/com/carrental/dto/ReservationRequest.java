package com.carrental.dto;

import com.carrental.enums.CarType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record ReservationRequest(
        @NotBlank String renterName,
        @Email @NotBlank String renterEmail,
        @NotNull CarType carType,
        @NotNull LocalDateTime startDate,
        @NotNull @Positive Integer numberOfDays
) {}
