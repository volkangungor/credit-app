package com.volco.creditapp.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CUSTOMER")
@Data
public class CustomerEntity {

    @Id
    private Long id;

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @NotBlank
    private String surname;

    @Column(name = "CREDITLIMIT", nullable = false)
    @PositiveOrZero
    private BigDecimal creditLimit;

    @Column(name = "USEDCREDITLIMIT", nullable = false)
    @PositiveOrZero
    private BigDecimal usedCreditLimit;
}