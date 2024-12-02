package com.volco.creditapp.persistence.dao;

import com.volco.creditapp.application.exceptions.InvalidStateException;
import com.volco.creditapp.persistence.repository.LoanInstallmentRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;

@Repository
public class LoanInstallmentDao {

    private final LoanInstallmentRepository loanInstallmentRepository;

    public LoanInstallmentDao(LoanInstallmentRepository loanInstallmentRepository) {
        this.loanInstallmentRepository = loanInstallmentRepository;
    }

    public void updatePaid(Long installmentId, BigDecimal paidAmount, Instant paymentDate) {
        int updated = loanInstallmentRepository.updatePaid(installmentId, paidAmount, paymentDate);
        if (updated == 0) {
            throw new InvalidStateException("Installment can't be paid because of state.");
        }
    }
}
