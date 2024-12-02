package com.volco.creditapp.persistence.repository;

import com.volco.creditapp.domain.model.Loan;
import com.volco.creditapp.persistence.entity.LoanEntity;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<LoanEntity, Long> {
    List<Loan> findByCustomerId(Long customerId);

    Optional<LoanEntity> findByIdAndCustomerId(Long id, @NotNull Long customerId);

    @Transactional(Transactional.TxType.MANDATORY)
    @Modifying
    @Query("update LoanEntity i set i.isPaid = :isPaid where i.id = :id")
    int updateIsPaid(Long id, boolean isPaid);
}
