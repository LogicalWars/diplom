package ru.netology.netologydiplombackend.service;

import org.springframework.web.multipart.MultipartFile;
import ru.netology.netologydiplombackend.dto.file.FileResponse;
import ru.netology.netologydiplombackend.dto.file.FileForListResponse;
import ru.netology.netologydiplombackend.dto.file.UpdateRequest;

import java.util.List;

public interface FileService {
    void uploadFile(MultipartFile file, String fileName, String hash);

    FileResponse getFileByFileName(String filename);

    void updateFile(String filename, UpdateRequest updateRequest);

    void deleteFile(String filename);

    List<FileForListResponse> getFiles(int limit);
}
