package ru.netology.netologydiplombackend.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.netologydiplombackend.dto.file.FileResponse;
import ru.netology.netologydiplombackend.dto.file.FileForListResponse;
import ru.netology.netologydiplombackend.dto.file.UpdateRequest;
import ru.netology.netologydiplombackend.exception.ApiException;
import ru.netology.netologydiplombackend.model.File;
import ru.netology.netologydiplombackend.model.User;
import ru.netology.netologydiplombackend.repository.FileRepository;
import ru.netology.netologydiplombackend.repository.UserRepository;
import ru.netology.netologydiplombackend.service.impl.FileServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.netology.netologydiplombackend.exception.ErrorContainer.FILE_ALREADY_EXISTS;
import static ru.netology.netologydiplombackend.exception.ErrorContainer.FILE_NOT_FOUND;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private FileServiceImpl fileService;

    private static final String FILENAME = "test.txt";
    private static final String HASH = "testHash";
    private static final String DATA = "content";
    private static final Long SIZE = (long) DATA.length();
    private static final String LOGIN = "testLogin";
    private static final String CONTENT_TYPE = "text/plain";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void uploadFile_shouldSaveFileWhenNotExists() throws IOException {
        setUpAuthenticationUser();

        ArgumentCaptor<File> entityCaptor = ArgumentCaptor.forClass(File.class);

        when(fileRepository.findByFilename(FILENAME)).thenReturn(Optional.empty());
        when(multipartFile.getContentType()).thenReturn(CONTENT_TYPE);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(DATA.getBytes()));
        when(multipartFile.getSize()).thenReturn(SIZE);

        fileService.uploadFile(multipartFile, FILENAME, HASH);

        verify(fileRepository).save(entityCaptor.capture());

        assertEquals(FILENAME, entityCaptor.getValue().getFilename());
        assertEquals(HASH, entityCaptor.getValue().getHash());
        assertArrayEquals(DATA.getBytes(), entityCaptor.getValue().getData());
        assertEquals(SIZE, entityCaptor.getValue().getSize());
        assertEquals(CONTENT_TYPE, entityCaptor.getValue().getContentType());
        assertEquals(LOGIN, entityCaptor.getValue().getUser().getLogin());
    }

    @Test
    void uploadFile_shouldThrowExceptionWhenExists() {
        when(fileRepository.findByFilename(FILENAME)).thenReturn(Optional.of(new File()));

        ApiException exception = assertThrows(ApiException.class,
                () -> fileService.uploadFile(multipartFile, FILENAME, HASH)
        );

        assertEquals(FILE_ALREADY_EXISTS.getErrorCode(), exception.getErrorCode());
        verify(fileRepository, never()).save(any());
    }


    @Test
    void getFileByFileName_shouldReturnFileWhenExists() {
        when(fileRepository.findByFilename(FILENAME)).thenReturn(Optional.of(createFile(FILENAME, DATA.getBytes())));

        FileResponse response = fileService.getFileByFileName(FILENAME);

        assertEquals(FILENAME, response.getFilename());
        assertArrayEquals(DATA.getBytes(), response.getData());
    }

    @Test
    void getFileByName_shouldThrowExceptionWhenFileDoesNotExist() {
        when(fileRepository.findByFilename(FILENAME)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> fileService.getFileByFileName(FILENAME));

        assertEquals(FILE_NOT_FOUND.getErrorCode(), exception.getErrorCode());
    }

    @Test
    void updateFile_shouldSuccessfulUpdateFilename() {
        String newFilename = "new_filename";
        UpdateRequest request = new UpdateRequest();
        request.setFilename(newFilename);

        ArgumentCaptor<File> entityCaptor = ArgumentCaptor.forClass(File.class);

        when(fileRepository.findByFilename(FILENAME)).thenReturn(Optional.of(createFile(FILENAME, DATA.getBytes())));

        fileService.updateFile(FILENAME, request);

        verify(fileRepository).save(entityCaptor.capture());
        assertEquals(newFilename, entityCaptor.getValue().getFilename());
    }

    @Test
    void updateFile_shouldThrowExceptionWhenFileDoesNotExist() {
        when(fileRepository.findByFilename(FILENAME)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> fileService.updateFile(FILENAME, new UpdateRequest()));

        assertEquals(FILE_NOT_FOUND.getErrorCode(), exception.getErrorCode());
    }

    @Test
    void deleteFile_shouldSuccessfulDeleteFile() {
        ArgumentCaptor<String> entityCaptor = ArgumentCaptor.forClass(String.class);

        when(fileRepository.findByFilename(FILENAME)).thenReturn(Optional.of(createFile(FILENAME, DATA.getBytes())));

        fileService.deleteFile(FILENAME);

        verify(fileRepository).deleteByFilename(entityCaptor.capture());
        assertEquals(FILENAME, entityCaptor.getValue());
    }

    @Test
    void deleteFile_shouldThrowExceptionWhenFileDoesNotExist() {
        when(fileRepository.findByFilename(FILENAME)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> fileService.deleteFile(FILENAME));

        assertEquals(FILE_NOT_FOUND.getErrorCode(), exception.getErrorCode());
    }

    @Test
    void getFiles_shouldReturnFiles() {
        int limit = 3;

        List<File> listOfFiles = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            listOfFiles.add(createFile(FILENAME + "_" + i, DATA.getBytes()));
        }

        when(fileRepository.findTopByUser(setUpAuthenticationUser(), limit)).thenReturn(listOfFiles);

        List<FileForListResponse> responseList = fileService.getFiles(limit);

        assertEquals(limit, responseList.size());
        for (int i = 0; i < limit; i++) {
            assertEquals(FILENAME + "_" + i, responseList.get(i).getFilename());
        }
    }

    @Test
    void getFiles_shouldReturnEmptyList() {
        int limit = 3;
        when(fileRepository.findTopByUser(setUpAuthenticationUser(), limit)).thenReturn(List.of());

        List<FileForListResponse> responseList = fileService.getFiles(limit);

        assertEquals(0, responseList.size());
    }

    private static File createFile(String filename, byte[] data) {
        File file = new File();
        file.setFilename(filename);
        file.setData(data);
        return file;
    }

    private User setUpAuthenticationUser() {
        User user = new User();
        user.setLogin(LOGIN);
        user.setPassword("password");

        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(user));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(LOGIN, null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return user;
    }


}
