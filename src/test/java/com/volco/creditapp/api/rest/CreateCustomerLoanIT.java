package com.volco.creditapp.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.volco.creditapp.application.BaseIntegrationTest;
import com.volco.creditapp.application.util.BigDecimalUtil;
import com.volco.creditapp.domain.model.Loan;
import com.volco.creditapp.domain.model.LoanInstallment;
import com.volco.creditapp.domain.model.dto.CreateLoanDto;
import com.volco.creditapp.persistence.entity.CustomerEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.volco.creditapp.api.rest.Constants.BASE_CUSTOMERS_LOANS_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateCustomerLoanIT extends BaseIntegrationTest {
    private WebTestClient.ResponseSpec callApi(Long customerId, String token, CreateLoanDto createLoanDto) throws JsonProcessingException {
        Map<String, String> map = Map.of("customerId", customerId.toString());
        return callApi(
                HttpMethod.POST,
                BASE_CUSTOMERS_LOANS_PATH,
                objectMapper.writeValueAsString(createLoanDto),
                map,
                null,
                token
        );
    }

    @Test
    public void createLoan_when_customer_not_exist_then_bad_request() throws JsonProcessingException {
        String token = getAdminToken();
        CreateLoanDto createLoanDto = new CreateLoanDto(
                BigDecimal.valueOf(15000),
                BigDecimal.valueOf(0.5),
                12
        );

        callApi(5L, token, createLoanDto)
                .expectStatus().isNotFound()
                .expectBody().json(objectMapper.writeValueAsString("Customer not found"));
    }

    @Test
    public void createLoan_when_invalid_no_of_installment_then_bad_request() throws JsonProcessingException {
        String token = getAdminToken();
        CreateLoanDto createLoanDto = new CreateLoanDto(
                BigDecimal.valueOf(15000),
                BigDecimal.valueOf(0.5),
                5
        );

        callApi(5L, token, createLoanDto)
                .expectStatus().isBadRequest()
                .expectBody().json(objectMapper.writeValueAsString("Invalid number of installments. Allowed values: 6,9,12,24"));
    }

    @Test
    public void createLoan_when_credit_limit_exceeded_then_bad_request() throws JsonProcessingException {
        String token = getAdminToken();
        CreateLoanDto createLoanDto = new CreateLoanDto(
                BigDecimal.valueOf(15000),
                BigDecimal.valueOf(0.5),
                12
        );

        CustomerEntity customer = createDefaultCustomer();

        callApi(customer.getId(), token, createLoanDto)
                .expectStatus().isBadRequest()
                .expectBody().json(objectMapper.writeValueAsString("Credit limit exceeded"));
    }

    @Test
    public void createLoan_when_admin_token_then_creates_loan() throws JsonProcessingException {
        createDefaultCustomer();
        String token = getAdminToken();
        CreateLoanDto createLoanDto = new CreateLoanDto(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(0.5),
                12
        );

        callApi(defaultCustomerId, token, createLoanDto)
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.loanAmount").isEqualTo("1000.0")
                .jsonPath("$.numberOfInstallment").isEqualTo(createLoanDto.numberOfInstallment());
    }

    @Test
    public void createLoan_when_owner_customer_token_then_creates_loan() throws JsonProcessingException {
        createDefaultCustomer();
        String token = getCustomerToken(defaultCustomerId);
        CreateLoanDto createLoanDto = new CreateLoanDto(
                BigDecimal.valueOf(5000),
                new BigDecimal("0.27"),
                6
        );

        String instantExpected = "2023-11-02T12:15:00Z";
        setMockClock(Instant.parse(instantExpected));

        Loan response = callApi(defaultCustomerId, token, createLoanDto)
                .expectStatus().isCreated()
                .expectBody(Loan.class)
                .returnResult().getResponseBody();

        assertNotNull(response);
        assertEquals(BigDecimalUtil.setScale(createLoanDto.amount()), response.loanAmount());
        assertEquals(createLoanDto.numberOfInstallment(), response.numberOfInstallment());
        assertEquals(defaultCustomerId, response.customerId());
        assertFalse(response.isPaid());

        List<LoanInstallment> loanInstallments = fetchInstallments(response.id());
        assertEquals(6, loanInstallments.size());
        assertTrue(loanInstallments.stream()
                .allMatch(installment -> new BigDecimal("1058.3333").equals(installment.amount())));

        List<LocalDate> expectedDueDates = List.of(
                LocalDate.parse("2023-12-01"),
                LocalDate.parse("2024-01-01"),
                LocalDate.parse("2024-02-01"),
                LocalDate.parse("2024-03-01"),
                LocalDate.parse("2024-04-01"),
                LocalDate.parse("2024-05-01")
        );

        assertEquals(
                expectedDueDates,
                loanInstallments.stream()
                        .sorted(Comparator.comparing(LoanInstallment::id))
                        .map(LoanInstallment::dueDate).toList()
                );


    }
}
