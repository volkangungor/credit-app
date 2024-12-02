package com.volco.creditapp.api.rest.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PayLoanRequest(
        @NotNull
        @Positive
        BigDecimal amount
) {
}
