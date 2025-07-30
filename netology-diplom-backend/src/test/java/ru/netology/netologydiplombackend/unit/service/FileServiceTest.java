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
import ru.netology.netologydiplombackend.exception.ApiException;
import ru.netology.netologydiplombackend.model.File;
import ru.netology.netologydiplombackend.model.User;
import ru.netology.netologydiplombackend.repository.FileRepository;
import ru.netology.netologydiplombackend.repository.UserRepository;
import ru.netology.netologydiplombackend.service.impl.FileServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.netology.netologydiplombackend.exception.ErrorContainer.FILE_ALREADY_EXISTS;

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

    private void setUpAuthenticationUser() {
        User user = new User();
        user.setLogin(LOGIN);
        user.setPassword("password");

        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(user));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(LOGIN, null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
