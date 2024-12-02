package com.volco.creditapp.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.volco.creditapp.application.BaseIntegrationTest;
import com.volco.creditapp.domain.model.LoanInstallment;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.Map;

import static com.volco.creditapp.api.rest.Constants.BASE_CUSTOMERS_LOANS_PATH;
import static com.volco.creditapp.api.rest.Constants.LOAN_ID_PATH;
import static org.assertj.core.api.Assertions.assertThat;

public class ListLoanInstallmentsIT extends BaseIntegrationTest {

    private WebTestClient.ResponseSpec callApi(Long customerId, Long loanId, String token) {
        Map<String, String> map = Map.of("customerId", customerId.toString(), "loanId", loanId.toString());
        return callApi(
                HttpMethod.GET,
                BASE_CUSTOMERS_LOANS_PATH + LOAN_ID_PATH,
                null,
                map,
                null,
                token
        );
    }

    @Test
    public void testListInstallments_when_valid_loan_id_then_returns_installments() {
        Long customerId = createDefaultCustomer().getId();
        Long loanId = createLoanEntity(customerId).getId();
        String token = getCustomerToken(customerId);


        callApi(customerId, loanId, token)
                .expectStatus().isOk()
                .expectBodyList(LoanInstallment.class)
                .value(installments -> assertThat(installments).isNotEmpty());
    }

    @Test
    public void testListInstallments_when_loan_not_owned_then_resource_not_found_exception() throws JsonProcessingException {
        Long customerId = createDefaultCustomer().getId();
        Long anotherCustomerId = createCustomer(500L, new BigDecimal("10000"), BigDecimal.ZERO).getId();
        Long loanId = createLoanEntity(anotherCustomerId).getId();
        String token = getCustomerToken(customerId);

        callApi(customerId, loanId, token)
                .expectStatus().isNotFound()
                .expectBody().json(objectMapper.writeValueAsString("Loan not found"));
    }
}
