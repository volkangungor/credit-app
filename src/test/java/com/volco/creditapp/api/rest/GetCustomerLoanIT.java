package com.volco.creditapp.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.volco.creditapp.application.BaseIntegrationTest;
import com.volco.creditapp.domain.model.Loan;
import com.volco.creditapp.persistence.entity.LoanEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;

import static com.volco.creditapp.api.rest.Constants.BASE_CUSTOMERS_LOANS_PATH;

public class GetCustomerLoanIT extends BaseIntegrationTest {

    private WebTestClient.ResponseSpec callApi(Long customerId, String token) {
        Map<String, String> map = Map.of("customerId", customerId.toString());
        return callApi(
                HttpMethod.GET,
                BASE_CUSTOMERS_LOANS_PATH,
                null,
                map,
                null,
                token
        );
    }

    @Test
    public void getLoans_when_no_token_then_returns_unauthorized() {
        callApi(defaultCustomerId, null)
                .expectStatus().isUnauthorized();
    }

    @Test
    public void getLoans_when_nonowner_customer_token_then_returns_forbidden() {
        String token = getCustomerToken(2L);
        callApi(defaultCustomerId, token)
                .expectStatus().isForbidden();
    }

    private List<Loan> createTestData(Long customerId) {
        LoanEntity entity1 = createLoanEntity(customerId);
        LoanEntity entity2 = createLoanEntity(customerId);
        return List.of(entity1.toRecord(), entity2.toRecord());
    }

    @Test
    public void getLoans_when_admin_token_then_returns_loans() throws JsonProcessingException {
        String token = getAdminToken();

        List<Loan> loans = createTestData(defaultCustomerId);
        createTestData(5000L);

        callApi(defaultCustomerId, token)
                .expectStatus().isOk()
                .expectBody()
                .json(objectMapper.writeValueAsString(loans));
    }

    @Test
    public void getLoans_when_owner_customer_token_then_returns_loans() throws JsonProcessingException {
        String token = getCustomerToken(defaultCustomerId);

        List<Loan> loans = createTestData(defaultCustomerId);

        callApi(defaultCustomerId, token)
                .expectStatus().isOk()
                .expectBody()
                .json(objectMapper.writeValueAsString(loans));
    }
}