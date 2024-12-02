package com.volco.creditapp.domain.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record Loan(
        @NotNull
        Long id,
        @NotNull
        Long customerId,
        @NotNull
        @Positive
        BigDecimal loanAmount,
        @NotNull
        @Positive
        Integer numberOfInstallment,
        @NotNull
        Instant createDate,
        boolean isPaid
) {
}
