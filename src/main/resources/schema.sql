ALTER TABLE CUSTOMER
    ADD CONSTRAINT chk_used_credit_limit CHECK (usedCreditLimit <= creditLimit);

ALTER TABLE CUSTOMER
    ADD CONSTRAINT chk_credit_limit_positive CHECK (usedCreditLimit >= 0);