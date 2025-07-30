package ru.netology.netologydiplombackend.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileResponse {
    private String filename;
    private byte[] data;
}
