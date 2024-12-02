package com.volco.creditapp.persistence.dao;

import com.volco.creditapp.application.exceptions.ResourceNotFoundException;
import com.volco.creditapp.domain.model.CustomerAndLoanIdentifier;
import com.volco.creditapp.domain.model.Loan;
import com.volco.creditapp.domain.model.LoanInstallment;
import com.volco.creditapp.domain.model.dto.CreateLoanDto;
import com.volco.creditapp.domain.model.dto.CreateLoanInstallmentDto;
import com.volco.creditapp.persistence.entity.LoanEntity;
import com.volco.creditapp.persistence.entity.LoanInstallmentEntity;
import com.volco.creditapp.persistence.repository.LoanRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LoanDao {

    private final LoanRepository loanRepository;

    public LoanDao(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public List<Loan> getLoans(Long customerId) {
        return loanRepository.findByCustomerId(customerId);
    }

    public List<LoanInstallment> getLoanInstallments(CustomerAndLoanIdentifier ids) {
        LoanEntity loanEntity = getLoanEntity(ids);
        return loanEntity.getInstallments().stream().map(LoanInstallmentEntity::toRecord).toList();
    }

    public Loan getLoan(CustomerAndLoanIdentifier ids) {
        return getLoanEntity(ids).toRecord();
    }

    private LoanEntity getLoanEntity(CustomerAndLoanIdentifier ids) {
        return loanRepository.findByIdAndCustomerId(ids.loanId(), ids.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
    }

    @Transactional(Transactional.TxType.MANDATORY)
    public Loan createLoan(
            Long customerId,
            CreateLoanDto createLoanDto,
            List<CreateLoanInstallmentDto> installmentDtos
    ) {
        LoanEntity loanEntity = new LoanEntity(customerId, createLoanDto);
        List<LoanInstallmentEntity> installments = installmentDtos.stream().map(dto ->
                new LoanInstallmentEntity(loanEntity, dto.amount(), dto.dueDate())).toList();
        loanEntity.setInstallments(installments);
        return loanRepository.save(loanEntity).toRecord();
    }

    public void updateIsPaid(Long loanId, boolean isPaid) {
        int updated = loanRepository.updateIsPaid(loanId, isPaid);
        if (updated == 0) {
            throw new ResourceNotFoundException("Loan not found");
        }
    }
}
