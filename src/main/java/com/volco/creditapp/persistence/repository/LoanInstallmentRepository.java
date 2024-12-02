package com.volco.creditapp.persistence.repository;

import com.volco.creditapp.domain.model.LoanInstallment;
import com.volco.creditapp.persistence.entity.LoanInstallmentEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface LoanInstallmentRepository extends JpaRepository<LoanInstallmentEntity, Long> {
    List<LoanInstallment> findByLoanId(Long loanId);

    @Transactional(Transactional.TxType.MANDATORY)
    @Modifying
    @Query("update LoanInstallmentEntity i set i.paidAmount = :paidAmount, i.paymentDate=:paymentDate, i.isPaid=true where i.id = :id and i.isPaid=false")
    int updatePaid(Long id, BigDecimal paidAmount, Instant paymentDate);
}
