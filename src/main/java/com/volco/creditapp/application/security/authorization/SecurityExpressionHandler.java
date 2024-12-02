package com.volco.creditapp.application.security.authorization;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class SecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
            Authentication authentication, MethodInvocation invocation) {
        CustomSecurityExpressionRoot expressionRoot = new CustomSecurityExpressionRoot(authentication);
        expressionRoot.setTrustResolver(getTrustResolver());
        expressionRoot.setPermissionEvaluator(getPermissionEvaluator());
        expressionRoot.setRoleHierarchy(getRoleHierarchy());
        return expressionRoot;
    }
}