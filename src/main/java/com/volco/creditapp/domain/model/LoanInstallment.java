package com.volco.creditapp.domain.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record LoanInstallment(
        @NotNull
        Long id,
        @NotNull
        Long loanId,
        @NotNull
        @Positive
        BigDecimal amount,
        @NotNull
        BigDecimal paidAmount,
        @NotNull
        LocalDate dueDate,
        Instant paymentDate,
        boolean isPaid
) {
        public LoanInstallment changePaidAmount(BigDecimal paidAmount) {
                return new LoanInstallment(id, loanId, amount, paidAmount, dueDate, paymentDate, isPaid);
        }
}