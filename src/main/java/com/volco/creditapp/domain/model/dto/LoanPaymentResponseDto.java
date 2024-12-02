package com.volco.creditapp.domain.model.dto;

import java.math.BigDecimal;

public record LoanPaymentResponseDto(
        int numberOfInstallmentsPaid,
        BigDecimal totalAmountSpent,
        boolean isLoanPaid
) {
}
