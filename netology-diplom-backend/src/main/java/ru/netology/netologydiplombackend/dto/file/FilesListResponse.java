package ru.netology.netologydiplombackend.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FilesListResponse {
    private String filename;
    private Long size;
}
