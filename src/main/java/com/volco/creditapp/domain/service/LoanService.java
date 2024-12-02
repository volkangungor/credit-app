package com.volco.creditapp.domain.service;

import com.volco.creditapp.application.util.BigDecimalUtil;
import com.volco.creditapp.application.util.DateTimeUtil;
import com.volco.creditapp.domain.model.CustomerAndLoanIdentifier;
import com.volco.creditapp.domain.model.InstallmentPaymentSummary;
import com.volco.creditapp.domain.model.Loan;
import com.volco.creditapp.domain.model.LoanInstallment;
import com.volco.creditapp.domain.model.dto.CreateLoanDto;
import com.volco.creditapp.domain.model.dto.CreateLoanInstallmentDto;
import com.volco.creditapp.domain.model.dto.LoanPaymentResponseDto;
import com.volco.creditapp.persistence.dao.CustomerDao;
import com.volco.creditapp.persistence.dao.LoanDao;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class LoanService {

    private final LoanPaymentService loanPaymentService;
    private final LoanDao loanDao;
    private final CustomerDao customerDao;
    private final Clock clock;

    private static final List<Integer> ALLOWED_NUM_OF_INSTALLMENTS = List.of(6, 9, 12, 24);

    public LoanService(
            LoanPaymentService loanPaymentService,
            LoanDao loanDao,
            CustomerDao customerDao,
            Clock clock
    ) {
        this.loanPaymentService = loanPaymentService;
        this.loanDao = loanDao;
        this.customerDao = customerDao;
        this.clock = clock;
    }

    public List<Loan> getLoans(Long customerId) {
        return loanDao.getLoans(customerId);
    }

    @Transactional
    public Loan createLoan(Long customerId, CreateLoanDto createLoanDto) {
        validateNumberOfInstallments(createLoanDto.numberOfInstallment());
        customerDao.updateUsedCreditLimit(customerId, createLoanDto.amount());
        return loanDao.createLoan(customerId, createLoanDto, setupInstallments(createLoanDto));
    }

    private static void validateNumberOfInstallments(Integer numOfInstallments) {
        if (!ALLOWED_NUM_OF_INSTALLMENTS.contains(numOfInstallments)) {
            throw new IllegalArgumentException("Invalid number of installments. Allowed values: "
                    + ALLOWED_NUM_OF_INSTALLMENTS.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }
    }

    private List<CreateLoanInstallmentDto> setupInstallments(CreateLoanDto createLoanDto) {
        BigDecimal actualInterestRate = createLoanDto.interestRate().add(BigDecimal.ONE);
        BigDecimal totalPayBackAmount = createLoanDto.amount().multiply(actualInterestRate);
        BigDecimal singleInstallmentAmount =
                BigDecimalUtil.divide(totalPayBackAmount, createLoanDto.numberOfInstallment());

        LocalDate firstDayOfNextMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfNextMonth());

        return IntStream.rangeClosed(1, createLoanDto.numberOfInstallment())
                .mapToObj(i -> new CreateLoanInstallmentDto(
                        singleInstallmentAmount,
                        firstDayOfNextMonth.plusMonths((i-1))
                ))
                .toList();
    }

    public List<LoanInstallment> getLoanInstallments(CustomerAndLoanIdentifier ids) {
        return loanDao.getLoanInstallments(ids);
    }

    @Transactional
    public LoanPaymentResponseDto payLoan(CustomerAndLoanIdentifier ids, BigDecimal amount) {
        Loan loan = loanDao.getLoan(ids);
        if(loan.isPaid()){
            return new LoanPaymentResponseDto(0, BigDecimal.ZERO, true);
        }

        InstallmentPaymentSummary installmentPaymentSummary = loanPaymentService.payLoan(ids, amount);

        //update loan.isPaid if all installments paid
        if (installmentPaymentSummary.allInstallmentsPaid()) {
            loanDao.updateIsPaid(ids.loanId(), true);
        }

        // unlock used credit limit as last step in transaction
        customerDao.updateUsedCreditLimit(ids.customerId(), installmentPaymentSummary.creditLimitToUnlock().negate());

        return new LoanPaymentResponseDto(
                installmentPaymentSummary.numberOfInstallmentsPaid(),
                installmentPaymentSummary.totalAmountSpent(),
                installmentPaymentSummary.allInstallmentsPaid()
        );
    }
}
