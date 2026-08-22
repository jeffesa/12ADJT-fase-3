package com.fiap.history.domain.shared;

public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
