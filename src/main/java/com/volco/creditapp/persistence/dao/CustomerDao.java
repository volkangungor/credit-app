package com.volco.creditapp.persistence.dao;

import com.volco.creditapp.application.exceptions.ResourceNotFoundException;
import com.volco.creditapp.persistence.repository.CustomerRepository;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class CustomerDao {
    private final CustomerRepository customerRepository;

    public CustomerDao(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void updateUsedCreditLimit(Long customerId, BigDecimal usedCreditLimitChange){
        try {
            int updated = customerRepository.updateUsedCreditLimit(customerId, usedCreditLimitChange);
            if (updated == 0) {
                throw new ResourceNotFoundException("Customer not found");
            }
        } catch (DataIntegrityViolationException e) {
            throw new ConstraintViolationException("Credit limit exceeded", null);
        }
    }
}
