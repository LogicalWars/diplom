package ru.netology.netologydiplombackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.netologydiplombackend.dto.file.FileResponse;
import ru.netology.netologydiplombackend.dto.file.FileForListResponse;
import ru.netology.netologydiplombackend.dto.file.UpdateRequest;
import ru.netology.netologydiplombackend.service.FileService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(@RequestParam("filename") String filename,
                                        @RequestPart(value = "hash", required = false) String hash,
                                        @RequestPart("file") MultipartFile file) {
        fileService.uploadFile(file, filename, hash);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/file")
    public ResponseEntity<byte[]> getFile(@RequestParam("filename") String filename) {
        System.out.println("test");
        FileResponse fileResponse = fileService.getFileByFileName(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResponse.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileResponse.getData());
    }

    @PutMapping("/file")
    public ResponseEntity<?> updateFile(@RequestParam("filename") String filename,
                                        @Valid @RequestBody UpdateRequest updateRequest) {
        fileService.updateFile(filename, updateRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(@RequestParam("filename") String filename) {
        fileService.deleteFile(filename);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public List<FileForListResponse> getFiles(@RequestParam("limit") int limit) {
        return fileService.getFiles(limit);
    }
}
