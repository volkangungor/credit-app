package com.volco.creditapp.persistence.entity;

import com.volco.creditapp.domain.model.Loan;
import com.volco.creditapp.domain.model.dto.CreateLoanDto;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "LOAN")
@Data
public class LoanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long customerId;

    @NotNull
    @Positive
    private BigDecimal loanAmount;

    @NotNull
    @Positive
    private Integer numberOfInstallment;

    @NotNull
    private Instant createDate;

    private boolean isPaid;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "loan")
    @OrderBy("dueDate ASC")
    private List<LoanInstallmentEntity> installments;

    public LoanEntity(Long customerId, CreateLoanDto createLoanDto) {
        this.customerId = customerId;
        this.loanAmount = createLoanDto.amount();
        this.numberOfInstallment = createLoanDto.numberOfInstallment();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createDate == null) {
            this.createDate = Instant.now();
        }
    }

    public Loan toRecord(){
        return new Loan(
                this.id,
                this.customerId,
                this.loanAmount,
                this.numberOfInstallment,
                this.createDate,
                this.isPaid
        );
    }
}