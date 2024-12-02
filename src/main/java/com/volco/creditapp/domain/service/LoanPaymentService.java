package com.volco.creditapp.domain.service;

import com.volco.creditapp.application.util.DateTimeUtil;
import com.volco.creditapp.domain.model.CustomerAndLoanIdentifier;
import com.volco.creditapp.domain.model.InstallmentPaymentSummary;
import com.volco.creditapp.domain.model.LoanInstallment;
import com.volco.creditapp.persistence.dao.LoanDao;
import com.volco.creditapp.persistence.dao.LoanInstallmentDao;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanPaymentService {
    private final LoanDao loanDao;
    private final LoanInstallmentDao loanInstallmentDao;
    private final Clock clock;

    private static final Integer ALLOWED_INSTALLMENT_PAYMENT_MONTHS = 3;

    public LoanPaymentService(
            LoanDao loanDao,
            LoanInstallmentDao loanInstallmentDao,
            Clock clock
    ) {
        this.loanDao = loanDao;
        this.loanInstallmentDao = loanInstallmentDao;
        this.clock = clock;
    }

    public InstallmentPaymentSummary payLoan(CustomerAndLoanIdentifier ids, BigDecimal amount) {
        List<LoanInstallment> installments = loanDao.getLoanInstallments(ids);
        List<LoanInstallment> payableInstallments = filterPayableInstallments(installments, amount);

        if (payableInstallments.isEmpty()) {
            return new InstallmentPaymentSummary(0, BigDecimal.ZERO, BigDecimal.ZERO, false);
        }

        // process payment
        payableInstallments.forEach(installment ->
                loanInstallmentDao.updatePaid(installment.id(), installment.paidAmount(), Instant.now())
        );

        // collect summary info
        int numberOfInstallmentsPaid = payableInstallments.size();
        boolean isAllInstallmentsPaid =
                installments.stream().filter(i -> !i.isPaid()).count() == numberOfInstallmentsPaid;
        BigDecimal totalAmountSpent = payableInstallments.stream()
                .map(LoanInstallment::paidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal creditLimitToUnlock = payableInstallments.stream()
                .map(LoanInstallment::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new InstallmentPaymentSummary(numberOfInstallmentsPaid, totalAmountSpent, creditLimitToUnlock, isAllInstallmentsPaid);
    }

    private List<LoanInstallment> filterPayableInstallments(List<LoanInstallment> installments, BigDecimal amount) {
        List<LoanInstallment> payableInstallments = new ArrayList<>();
        BigDecimal remainingAmount = amount;
        LocalDate firstDayOfMonth = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate monthCantBePaid = firstDayOfMonth.plusMonths(ALLOWED_INSTALLMENT_PAYMENT_MONTHS);

        List<LoanInstallment> filteredInstallments = installments.stream()
                .filter(installment -> installment.dueDate().isBefore(monthCantBePaid))
                .filter(installment -> !installment.isPaid())
                .toList();

        for (LoanInstallment installment : filteredInstallments) {
            BigDecimal amountToBePaid = calculateDiscountOrPenalty(installment);
            if (remainingAmount.compareTo(amountToBePaid) >= 0) {
                remainingAmount = remainingAmount.subtract(amountToBePaid);
                payableInstallments.add(installment.changePaidAmount(amountToBePaid));
            } else {
                break;
            }
        }
        return payableInstallments;
    }

    private BigDecimal calculateDiscountOrPenalty(LoanInstallment installment) {
        // TODO make discount or penalty calculations
        return installment.amount();
    }
}
