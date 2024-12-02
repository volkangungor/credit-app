package com.volco.creditapp.api.rest;

public class Constants {
    // token paths
    public static final String BASE_TOKEN_PATH = "/auth/token";
    public static final String ADMIN_PATH = "/admin";
    public static final String CUSTOMER_PATH = "/customers/{customerId}";

    // customer loans paths
    public static final String BASE_CUSTOMERS_LOANS_PATH = "/customers/{customerId}/loans";
    public static final String LOAN_ID_PATH = "/{loanId}";

    public static final String TAG_CUSTOMER_LOANS_OPS = "Customer Loans Operations";
}
