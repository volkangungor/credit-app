package com.volco.creditapp.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.volco.creditapp.api.rest.model.PayLoanRequest;
import com.volco.creditapp.application.BaseIntegrationTest;
import com.volco.creditapp.application.util.BigDecimalUtil;
import com.volco.creditapp.application.util.DateTimeUtil;
import com.volco.creditapp.domain.model.LoanInstallment;
import com.volco.creditapp.domain.model.dto.LoanPaymentResponseDto;
import com.volco.creditapp.persistence.entity.CustomerEntity;
import com.volco.creditapp.persistence.entity.LoanEntity;
import com.volco.creditapp.persistence.entity.LoanInstallmentEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.volco.creditapp.api.rest.Constants.BASE_CUSTOMERS_LOANS_PATH;
import static com.volco.creditapp.api.rest.Constants.LOAN_ID_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PayCustomerLoanIT extends BaseIntegrationTest {

    private WebTestClient.ResponseSpec callApi(Long customerId, Long loanId, PayLoanRequest request, String token) throws JsonProcessingException {
        Map<String, String> map = Map.of("customerId", customerId.toString(), "loanId", loanId.toString());
        return callApi(
                HttpMethod.POST,
                BASE_CUSTOMERS_LOANS_PATH + LOAN_ID_PATH,
                objectMapper.writeValueAsString(request),
                map,
                null,
                token
        );
    }

    @Test
    public void payLoan_when_loan_not_owned_then_resource_not_found_exception() throws JsonProcessingException {
        Long customerId = createDefaultCustomer().getId();
        Long anotherCustomerId = createCustomer(500L, new BigDecimal("10000"), BigDecimal.ZERO).getId();
        Long loanId = createLoanEntity(anotherCustomerId).getId();
        String token = getCustomerToken(customerId);
        PayLoanRequest request = new PayLoanRequest(new BigDecimal("700"));

        callApi(customerId, loanId, request, token)
                .expectStatus().isNotFound()
                .expectBody().json(objectMapper.writeValueAsString("Loan not found"));
    }

    private List<LoanInstallmentEntity> createLoanInstallments(LoanEntity loanEntity, int numOfInstallments) {
        LocalDate firstDayOfMonth = LocalDate.now(mockClock).with(TemporalAdjusters.firstDayOfMonth());
        return IntStream.rangeClosed(1, numOfInstallments).mapToObj(i ->
                new LoanInstallmentEntity(
                    null,
                    loanEntity,
                    new BigDecimal("200"),
                    BigDecimal.ZERO,
                    firstDayOfMonth.plusMonths(i-1),
                    null,
                    false
        )).collect(Collectors.toList());
    }

    private LoanEntity createPaidLoanEntity(Long customerId, int noOfPaidInstallments) {
        LoanEntity loanEntity = new LoanEntity(
                null,
                customerId,
                new BigDecimal("1000"),
                6,
                Instant.now(),
                false,
                null
        );
        List<LoanInstallmentEntity> installments = createLoanInstallments(loanEntity, 6);
        installments.subList(0, noOfPaidInstallments)
                .forEach(installment -> {
                    installment.setPaid(true);
                    installment.setPaymentDate(Instant.now());
                    installment.setPaidAmount(installment.getAmount());
                });
        loanEntity.setInstallments(installments);
        return loanRepository.save(loanEntity);
    }

    @Test
    public void payLoan_when_due_dates_after_3_months_then_no_payment() throws JsonProcessingException {
        Long customerId = createDefaultCustomer().getId();
        Long loanId = createPaidLoanEntity(customerId, 3).getId();
        String token = getCustomerToken(customerId);
        PayLoanRequest request = new PayLoanRequest(new BigDecimal("100000"));

        LoanPaymentResponseDto expectedResponse = new LoanPaymentResponseDto(
                0,
                BigDecimalUtil.setScale(BigDecimal.ZERO),
                false
        );

        callApi(customerId, loanId, request, token)
                .expectStatus().isOk()
                .expectBody(LoanPaymentResponseDto.class)
                .value(responseDto -> assertEquals(expectedResponse, responseDto));
    }

    @Test
    public void payLoan_when_amount_is_not_enough_then_no_payment() throws JsonProcessingException {
        Long customerId = createDefaultCustomer().getId();
        Long loanId = createPaidLoanEntity(customerId, 1).getId();
        String token = getCustomerToken(customerId);
        PayLoanRequest request = new PayLoanRequest(new BigDecimal("100"));

        LoanPaymentResponseDto expectedResponse = new LoanPaymentResponseDto(
                0,
                BigDecimalUtil.setScale(BigDecimal.ZERO),
                false
        );

        callApi(customerId, loanId, request, token)
                .expectStatus().isOk()
                .expectBody(LoanPaymentResponseDto.class)
                .value(responseDto -> assertEquals(expectedResponse, responseDto));
    }

    @Test
    public void payLoan_when_excess_amount_then_two_installment_paid() throws JsonProcessingException {
        Long customerId = createCustomer(defaultCustomerId, new BigDecimal("5000"), new BigDecimal("800")).getId();
        Long loanId = createPaidLoanEntity(customerId, 1).getId();
        String token = getCustomerToken(customerId);
        PayLoanRequest request = new PayLoanRequest(new BigDecimal("600"));

        LoanPaymentResponseDto expectedResponse = new LoanPaymentResponseDto(
                2,
                BigDecimalUtil.setScale(new BigDecimal("400")),
                false
        );

        callApi(customerId, loanId, request, token)
                .expectStatus().isOk()
                .expectBody(LoanPaymentResponseDto.class)
                .value(responseDto -> assertEquals(expectedResponse, responseDto));

        CustomerEntity customerEntity = customerRepository.findById(defaultCustomerId).get();
        assertEquals(new BigDecimal("400.00"), customerEntity.getUsedCreditLimit());
        assertFalse(loanRepository.findById(loanId).get().isPaid());
        // expect second and third installments are paid
        List<LoanInstallment> loanInstallments = fetchInstallments(loanId);
        LoanInstallment loanInstallment = loanInstallments.get(1);
        assertEquals(new BigDecimal("200.0000"), loanInstallment.paidAmount());
        assertNotNull(loanInstallment.paymentDate());
        assertTrue(loanInstallment.isPaid());
        loanInstallment = loanInstallments.get(2);
        assertEquals(new BigDecimal("200.0000"), loanInstallment.paidAmount());
        assertNotNull(loanInstallment.paymentDate());
        assertTrue(loanInstallment.isPaid());
    }

    @Test
    public void payLoan_when_excess_amount_for_last_installments_then_all_paid() throws JsonProcessingException {
        Long customerId = createCustomer(defaultCustomerId, new BigDecimal("5000"), new BigDecimal("600")).getId();
        Long loanId = createPaidLoanEntity(customerId, 3).getId();
        String token = getCustomerToken(customerId);
        PayLoanRequest request = new PayLoanRequest(new BigDecimal("650"));

        LoanPaymentResponseDto expectedResponse = new LoanPaymentResponseDto(
                3,
                BigDecimalUtil.setScale(new BigDecimal("600")),
                true
        );

        setMockClock(DateTimeUtil.toInstant(LocalDate.now().plusMonths(3)));

        callApi(customerId, loanId, request, token)
                .expectStatus().isOk()
                .expectBody(LoanPaymentResponseDto.class)
                .value(responseDto -> assertEquals(expectedResponse, responseDto));

        CustomerEntity customerEntity = customerRepository.findById(defaultCustomerId).get();
        assertEquals(new BigDecimal("0.00"), customerEntity.getUsedCreditLimit());
        assertTrue(loanRepository.findById(loanId).get().isPaid());
        // expect all installments are paid
        List<LoanInstallment> loanInstallments = fetchInstallments(loanId);
        loanInstallments.forEach(loanInstallment -> {
            assertEquals(new BigDecimal("200.0000"), loanInstallment.paidAmount());
            assertNotNull(loanInstallment.paymentDate());
            assertTrue(loanInstallment.isPaid());
        });
    }
}