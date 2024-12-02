package com.volco.creditapp.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volco.creditapp.domain.model.LoanInstallment;
import com.volco.creditapp.persistence.dao.CustomerDao;
import com.volco.creditapp.persistence.entity.CustomerEntity;
import com.volco.creditapp.persistence.entity.LoanEntity;
import com.volco.creditapp.persistence.entity.LoanInstallmentEntity;
import com.volco.creditapp.persistence.repository.CustomerRepository;
import com.volco.creditapp.persistence.repository.LoanInstallmentRepository;
import com.volco.creditapp.persistence.repository.LoanRepository;
import org.instancio.Instancio;
import org.instancio.Select;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.volco.creditapp.api.rest.Constants.ADMIN_PATH;
import static com.volco.creditapp.api.rest.Constants.BASE_TOKEN_PATH;
import static com.volco.creditapp.api.rest.Constants.CUSTOMER_PATH;
import static org.instancio.Select.field;

@ExtendWith(InstancioExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles(value = "test")
public abstract class BaseIntegrationTest {
    @Autowired
    public WebTestClient webTestClient;

    @SpyBean
    public Clock mockClock;

    @Autowired
    public LoanRepository loanRepository;

    @Autowired
    public LoanInstallmentRepository loanInstallmentRepository;

    @Autowired
    public CustomerRepository customerRepository;

    @Autowired
    public CustomerDao customerDao;

    @Autowired
    public ObjectMapper objectMapper;

    public final Long defaultCustomerId = 1000L;

    @AfterEach
    public void afterEach() {
        customerRepository.deleteAll();
        loanRepository.deleteAll();
        loanInstallmentRepository.deleteAll();
        Mockito.reset();
    }

    public void setMockClock(Instant instant) {
        Mockito.when(mockClock.instant()).thenReturn(instant);
        Mockito.when(mockClock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    public String getAdminToken() {
        WebTestClient.ResponseSpec response =
                callApi(HttpMethod.GET, BASE_TOKEN_PATH + ADMIN_PATH, null, Collections.emptyMap(), null, null);
        return response.expectBody(String.class).returnResult().getResponseBody();
    }

    public String getCustomerToken(Long customerId) {
        WebTestClient.ResponseSpec response = callApi(
                HttpMethod.GET,
                BASE_TOKEN_PATH + CUSTOMER_PATH,
                null,
                Map.of("customerId", customerId.toString()),
                null,
                null
        );
        return response.expectBody(String.class).returnResult().getResponseBody();
    }

    public WebTestClient.ResponseSpec callApi(
            HttpMethod httpMethod,
            String uri,
            String bodyValue,
            Map<String, String> uriVariables,
            MultiValueMap<String, String> queryParams,
            String token
    ) {
        WebTestClient.RequestBodySpec req = webTestClient.method(httpMethod)
                .uri(uriBuilder -> {
                    uriBuilder.path(uri);
                    if (queryParams != null) {
                        uriBuilder.queryParams(queryParams);
                    }
                    return uriBuilder.build(uriVariables);
                })
                .headers(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    if (token != null) {
                        headers.setBearerAuth(token);
                    }
                });

        if (bodyValue != null) {
            req.bodyValue(bodyValue);
        }

        return req.exchange();
    }

    public CustomerEntity createDefaultCustomer() {
        return createCustomer(defaultCustomerId, new BigDecimal("10000"), new BigDecimal("0"));
    }

    public CustomerEntity createCustomer(Long customerId, BigDecimal creditLimit, BigDecimal usedCreditLimit) {
        return customerRepository.save(
                new CustomerEntity(
                        customerId,
                        "name",
                        "surname",
                        creditLimit,
                        usedCreditLimit
                )
        );
    }

    public LoanEntity createLoanEntity(Long customerId) {
        LoanEntity loanEntity = Instancio.of(LoanEntity.class)
                .set(field(LoanEntity::getId), null)
                .set(field(LoanEntity::getCustomerId), customerId)
                .set(field(LoanEntity::getCreateDate), Instant.now())
                .set(field(LoanEntity::getInstallments), null)
                .create();
        List<LoanInstallmentEntity> loanInstallments = Instancio.ofList(LoanInstallmentEntity.class)
                .size(6)
                .set(Select.field(LoanInstallmentEntity::getId), null)
                .set(Select.field(LoanInstallmentEntity::getLoan), loanEntity)
                .create();
        loanEntity.setInstallments(loanInstallments);
        return loanRepository.save(loanEntity);
    }

    public List<LoanInstallment> fetchInstallments(Long loanId) {
        return loanInstallmentRepository.findByLoanId(loanId);
    }
}