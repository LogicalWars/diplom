package ru.netology.netologydiplombackend.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final int errorCode;

    public ApiException(ErrorContainer errorContainer) {
        super(errorContainer.getMessage());
        this.errorCode = errorContainer.getErrorCode();
    }

    public ApiException(ErrorContainer errorContainer, String message) {
        super(errorContainer.getMessage()+ ": " + message);
        this.errorCode = errorContainer.getErrorCode();
    }
}
