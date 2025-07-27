package ru.netology.netologydiplombackend.dto.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginResponse {

    @JsonProperty("auth-token")
    private String token;
}
