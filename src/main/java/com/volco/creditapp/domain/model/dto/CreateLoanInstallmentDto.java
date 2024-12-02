package com.volco.creditapp.domain.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateLoanInstallmentDto(
        @NotNull
        @Positive
        BigDecimal amount,
        @NotNull
        LocalDate dueDate
) {
}
