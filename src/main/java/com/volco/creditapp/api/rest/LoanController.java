package com.volco.creditapp.api.rest;

import com.volco.creditapp.api.rest.model.PayLoanRequest;
import com.volco.creditapp.domain.model.CustomerAndLoanIdentifier;
import com.volco.creditapp.domain.model.Loan;
import com.volco.creditapp.domain.model.LoanInstallment;
import com.volco.creditapp.domain.model.dto.CreateLoanDto;
import com.volco.creditapp.domain.model.dto.LoanPaymentResponseDto;
import com.volco.creditapp.domain.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static com.volco.creditapp.api.rest.Constants.BASE_CUSTOMERS_LOANS_PATH;
import static com.volco.creditapp.api.rest.Constants.LOAN_ID_PATH;
import static com.volco.creditapp.api.rest.Constants.TAG_CUSTOMER_LOANS_OPS;

@RestController
@RequestMapping(BASE_CUSTOMERS_LOANS_PATH)
public class LoanController {

    private final LoanService loanService;

    public LoanController(
            LoanService loanService
    ) {
        this.loanService = loanService;
    }

    @Operation(
            tags = TAG_CUSTOMER_LOANS_OPS,
            summary = "List loans for a customer"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasPermissionForCustomer(#customerId)")
    @GetMapping(
            value = "",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<Loan>> listLoans(@PathVariable Long customerId) {
        List<Loan> loans = loanService.getLoans(customerId);
        return ResponseEntity.ok(loans);
    }

    @Operation(
            tags = TAG_CUSTOMER_LOANS_OPS,
            summary = "Create loan for a customer"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasPermissionForCustomer(#customerId)")
    @PostMapping(
            value = "",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Loan> createLoan(
            @PathVariable Long customerId,
            @RequestBody @Valid CreateLoanDto createLoanRequest
    ) {
        Loan loan = loanService.createLoan(customerId, createLoanRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path(LOAN_ID_PATH)
                .buildAndExpand(loan.id())
                .toUri();

        return ResponseEntity.created(location).body(loan);
    }

    @Operation(
            tags = TAG_CUSTOMER_LOANS_OPS,
            summary = "List installments for a loan"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasPermissionForCustomer(#customerId)")
    @GetMapping(
            value = LOAN_ID_PATH,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<LoanInstallment>> listInstallments(
            @PathVariable @NotNull Long customerId,
            @PathVariable @NotNull Long loanId
    ) {
        List<LoanInstallment> loans = loanService.getLoanInstallments(new CustomerAndLoanIdentifier(customerId, loanId));
        return ResponseEntity.ok(loans);
    }

    @Operation(
            tags = TAG_CUSTOMER_LOANS_OPS,
            summary = "Pay installment for a loan"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasPermissionForCustomer(#customerId)")
    @PostMapping(
            value = LOAN_ID_PATH,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LoanPaymentResponseDto> payLoan(
            @PathVariable @NotNull Long customerId,
            @PathVariable @NotNull Long loanId,
            @Parameter(description = "Payload for payLoan endpoint", required = true)
            @Valid
            @RequestBody
            PayLoanRequest request
    ) {
        return ResponseEntity.ok(loanService.payLoan(new CustomerAndLoanIdentifier(customerId, loanId), request.amount()));
    }
}
