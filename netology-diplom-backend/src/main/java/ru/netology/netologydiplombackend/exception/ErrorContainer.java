package ru.netology.netologydiplombackend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorContainer {
    INVALID_CREDENTIALS("Invalid login or password", 40001),
    FILE_ALREADY_EXISTS("File already exists", 40002),
    FILE_NOT_FOUND("File not found", 40003);
    private final String message;
    private final int errorCode;
}
