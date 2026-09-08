package com.leonardo.erp.common.exception;

import java.util.UUID;

/**
 * Thrown when a requested entity does not exist. Mapped to {@code 404 Not Found}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String entityName, UUID id) {
        super(entityName + " not found: " + id);
    }
}
