package com.volco.creditapp.persistence.repository;

import com.volco.creditapp.persistence.entity.CustomerEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    @Transactional(Transactional.TxType.MANDATORY)
    @Modifying
    @Query(
            value = "update CUSTOMER c set c.USEDCREDITLIMIT = c.USEDCREDITLIMIT + :usedCreditLimitChange where c.ID = :customerId",
            nativeQuery = true)
    int updateUsedCreditLimit(Long customerId, BigDecimal usedCreditLimitChange);
}
