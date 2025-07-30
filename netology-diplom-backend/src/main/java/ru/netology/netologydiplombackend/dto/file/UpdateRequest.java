package ru.netology.netologydiplombackend.dto.file;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRequest {

    @NotBlank
    private String filename;
}
