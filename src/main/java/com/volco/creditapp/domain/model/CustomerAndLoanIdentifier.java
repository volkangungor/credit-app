package com.volco.creditapp.domain.model;

public record CustomerAndLoanIdentifier(
        Long customerId,
        Long loanId
) {
}
