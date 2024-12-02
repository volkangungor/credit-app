package com.volco.creditapp.domain.model.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateLoanDto(
        @NotNull
        @Positive
        BigDecimal amount,
        @NotNull
        @DecimalMin("0.1")
        @DecimalMax("0.5")
        BigDecimal interestRate,
        @NotNull
        @Positive
        Integer numberOfInstallment
) {
}
