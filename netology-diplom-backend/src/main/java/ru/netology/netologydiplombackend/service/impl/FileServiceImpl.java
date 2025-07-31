package ru.netology.netologydiplombackend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.netologydiplombackend.dto.file.FileResponse;
import ru.netology.netologydiplombackend.dto.file.FileForListResponse;
import ru.netology.netologydiplombackend.dto.file.UpdateRequest;
import ru.netology.netologydiplombackend.exception.ApiException;
import ru.netology.netologydiplombackend.model.File;
import ru.netology.netologydiplombackend.model.User;
import ru.netology.netologydiplombackend.repository.FileRepository;
import ru.netology.netologydiplombackend.repository.UserRepository;
import ru.netology.netologydiplombackend.service.FileService;

import java.io.IOException;
import java.util.List;

import static ru.netology.netologydiplombackend.exception.ErrorContainer.FILE_ALREADY_EXISTS;
import static ru.netology.netologydiplombackend.exception.ErrorContainer.FILE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;


    @Override
    public void uploadFile(MultipartFile file, String fileName, String hash) {
        if (checkExistsFile(fileName)) {
            throw new ApiException(FILE_ALREADY_EXISTS, fileName);
        }
        File entityFile = buildEntityFile(file, fileName, hash);
        fileRepository.save(entityFile);
    }

    @Override
    public FileResponse getFileByFileName(String filename) {
        return fileRepository.findByFilename(filename)
                .stream()
                .map(file -> new FileResponse(file.getFilename(), file.getData()))
                .findFirst()
                .orElseThrow(() -> new ApiException(FILE_NOT_FOUND, filename));
    }

    @Override
    public void updateFile(String filename, UpdateRequest updateRequest) {
        File file = fileRepository.findByFilename(filename)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(FILE_NOT_FOUND, filename));

        file.setFilename(updateRequest.getFilename());
        fileRepository.save(file);
    }

    @Override
    @Transactional
    public void deleteFile(String filename) {
        if (checkExistsFile(filename)) {
            fileRepository.deleteByFilename(filename);
        } else {
            throw new ApiException(FILE_NOT_FOUND, filename);
        }
    }

    @Override
    public List<FileForListResponse> getFiles(int limit) {
        return fileRepository.findTopByUser(getCurrentUser(), limit)
                .stream()
                .map(file -> new FileForListResponse(file.getFilename(), file.getSize()))
                .toList();
    }

    private boolean checkExistsFile(String filename) {
        return fileRepository.findByFilename(filename).isPresent();
    }

    private User getCurrentUser() {
        return userRepository.findByLogin(
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName())
                .stream().findFirst()
                .orElseThrow();
    }

    private File buildEntityFile(MultipartFile file, String fileName, String hash) {
        File entityFile = new File();
        try {
            entityFile.setFilename(fileName);
            entityFile.setHash(hash);
            entityFile.setContentType(file.getContentType());
            entityFile.setData(file.getInputStream().readAllBytes());
            entityFile.setSize(file.getSize());
            entityFile.setUser(getCurrentUser());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return entityFile;
    }
}
