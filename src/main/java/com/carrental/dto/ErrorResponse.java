package com.carrental.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        Map<String, String> validationErrors
) {}
