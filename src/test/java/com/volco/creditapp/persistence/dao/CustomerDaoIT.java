package com.volco.creditapp.persistence.dao;

import com.volco.creditapp.application.BaseIntegrationTest;
import com.volco.creditapp.application.exceptions.ResourceNotFoundException;
import com.volco.creditapp.persistence.entity.CustomerEntity;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CustomerDaoIT extends BaseIntegrationTest {

    @Transactional
    @Test
    public void updateUsedCreditLimit_when_no_customer_exist_then_no_resource_found_exception() {
        BigDecimal exceededLimit = new BigDecimal("100");
        assertThrows(ResourceNotFoundException.class, () -> customerDao.updateUsedCreditLimit(5L,
                exceededLimit), "Customer not found");
    }

    @Transactional
    @Test
    public void updateUsedCreditLimit_when_limit_exceeded_then_exception() {

        CustomerEntity customer = createCustomer(1L, new BigDecimal("1000"), new BigDecimal("500"));

        // valid credit limit usage
        BigDecimal newUsedCreditLimit = new BigDecimal("500");
        customerDao.updateUsedCreditLimit(customer.getId(), newUsedCreditLimit);

        // exceeded credit limit usage
        BigDecimal exceededLimit = new BigDecimal("100");
        assertThrows(ConstraintViolationException.class, () -> customerDao.updateUsedCreditLimit(customer.getId(),
                exceededLimit), "Credit limit exceeded");

    }
}
