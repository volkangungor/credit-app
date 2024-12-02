package com.volco.creditapp.domain.model;

import java.math.BigDecimal;

public record InstallmentPaymentSummary(
        int numberOfInstallmentsPaid,
        BigDecimal totalAmountSpent,
        BigDecimal creditLimitToUnlock,
        boolean allInstallmentsPaid
) {
}
