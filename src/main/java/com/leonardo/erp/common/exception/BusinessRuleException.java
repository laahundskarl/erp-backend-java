package com.leonardo.erp.common.exception;

/**
 * Thrown when a request is well-formed but violates a domain business rule
 * (e.g. discounting a closed order). Mapped to {@code 409 Conflict}.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
