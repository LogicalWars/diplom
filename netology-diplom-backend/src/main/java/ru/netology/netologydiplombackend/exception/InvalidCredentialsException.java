package ru.netology.netologydiplombackend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
@Getter
public class InvalidCredentialsException extends RuntimeException {
  private int code;
  public InvalidCredentialsException(String message, int code) {
    super(message);
    this.code = code;
  }
}
