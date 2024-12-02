package com.volco.creditapp.persistence.entity;

import com.volco.creditapp.domain.model.LoanInstallment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static com.volco.creditapp.application.util.BigDecimalUtil.PRECISION;
import static com.volco.creditapp.application.util.BigDecimalUtil.SCALE;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "LOAN_INSTALLMENT")
@Data
public class LoanInstallmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loanId", nullable = false)
    private LoanEntity loan;

    @NotNull
    @Positive
    @Column(precision = PRECISION, scale = SCALE)
    private BigDecimal amount;

    @NotNull
    @PositiveOrZero
    @Column(precision = PRECISION, scale = SCALE)
    private BigDecimal paidAmount;

    @NotNull
    private LocalDate dueDate;

    private Instant paymentDate;

    private boolean isPaid;

    public LoanInstallmentEntity(LoanEntity loan, BigDecimal amount, LocalDate dueDate) {
        this.loan = loan;
        this.amount = amount;
        this.dueDate = dueDate;
        this.paidAmount = BigDecimal.ZERO;
    }

    public LoanInstallment toRecord() {
        return new LoanInstallment(
                id,
                loan.getId(),
                amount,
                paidAmount,
                dueDate,
                paymentDate,
                isPaid
        );
    }
}