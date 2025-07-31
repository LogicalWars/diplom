package ru.netology.netologydiplombackend.integration.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.netology.netologydiplombackend.config.TestSecurityConfig;
import ru.netology.netologydiplombackend.dto.ErrorResponse;
import ru.netology.netologydiplombackend.dto.file.FileForListResponse;
import ru.netology.netologydiplombackend.dto.file.UpdateRequest;
import ru.netology.netologydiplombackend.integration.TestcontainersConfiguration;
import ru.netology.netologydiplombackend.model.File;
import ru.netology.netologydiplombackend.model.User;
import ru.netology.netologydiplombackend.repository.FileRepository;
import ru.netology.netologydiplombackend.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static ru.netology.netologydiplombackend.exception.ErrorContainer.FILE_ALREADY_EXISTS;
import static ru.netology.netologydiplombackend.exception.ErrorContainer.FILE_NOT_FOUND;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
public class FileControllerIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    private static final String FILENAME = "test.txt";
    private static final String HASH = "testHash";
    private static final String DATA = "content";
    private static final Long SIZE = (long) DATA.length();
    private static final String LOGIN = TestSecurityConfig.LOGIN;
    private static final String CONTENT_TYPE = "text/plain";
    private static final String PASSWORD = "testPassword";

    private final User user = new User();
    private static final HttpHeaders headers = new HttpHeaders();

    @BeforeEach
    void setupUser() {
        user.setLogin(LOGIN);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        if (userRepository.findByLogin(LOGIN).isEmpty()) {
            userRepository.save(user);
        }

        headers.set("auth-token", "test-token");
    }

    @AfterEach
    void tearDown() {
        fileRepository.deleteAll();
    }

    @Test
    void uploadFile_shouldReturnOk() {
        HttpEntity<MultiValueMap<String, Object>> requestEntity = getMultiValueMapHttpEntity();

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/file?filename=%s".formatted(FILENAME),
                requestEntity,
                Void.class
        );

        Optional<File> savedFile = fileRepository.findByFilename(FILENAME);

        assertTrue(savedFile.isPresent());

        assertEquals(FILENAME, savedFile.get().getFilename());
        assertEquals(HASH, savedFile.get().getHash());
        assertArrayEquals(DATA.getBytes(), savedFile.get().getData());
        assertEquals(SIZE, savedFile.get().getSize());
        assertEquals(CONTENT_TYPE, savedFile.get().getContentType());
        assertEquals(LOGIN, savedFile.get().getUser().getLogin());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    void uploadFile_shouldReturnErrorFileAlreadyExists() {
        generateFilesInDatabase();

        HttpEntity<MultiValueMap<String, Object>> requestEntity = getMultiValueMapHttpEntity();

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/file?filename=%s".formatted(FILENAME),
                requestEntity,
                ErrorResponse.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(FILE_ALREADY_EXISTS.getMessage() + ": " + FILENAME, response.getBody().getMessage());
        assertEquals(FILE_ALREADY_EXISTS.getErrorCode(), response.getBody().getId());
    }

    @Test
    void getFile_shouldReturnOkAndFileData() {
        generateFilesInDatabase();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                "/file?filename=%s".formatted(FILENAME),
                HttpMethod.GET,
                requestEntity,
                byte[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(DATA.getBytes(), response.getBody());

        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains(FILENAME));
    }

    @Test
    void getFile_shouldReturnErrorFileNotFound() {
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/file?filename=%s".formatted(FILENAME),
                HttpMethod.GET,
                requestEntity,
                ErrorResponse.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(FILE_NOT_FOUND.getMessage() + ": " + FILENAME, response.getBody().getMessage());
        assertEquals(FILE_NOT_FOUND.getErrorCode(), response.getBody().getId());
    }

    @Test
    void updateFile_shouldReturnOkAndSuccessfulUpdateFilename() {
        String newFilename = "newFilename";

        generateFilesInDatabase();

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setFilename(newFilename);

        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UpdateRequest> requestEntity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/file?filename=%s".formatted(FILENAME),
                HttpMethod.PUT,
                requestEntity,
                Void.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertTrue(fileRepository.findByFilename(newFilename).isPresent());
        assertTrue(fileRepository.findByFilename(FILENAME).isEmpty());
    }

    @Test
    void updateFile_shouldReturnErrorFileNotFound() {
        String newFilename = "newFilename";

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setFilename(newFilename);

        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UpdateRequest> requestEntity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/file?filename=%s".formatted(FILENAME),
                HttpMethod.PUT,
                requestEntity,
                ErrorResponse.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(FILE_NOT_FOUND.getMessage() + ": " + FILENAME, response.getBody().getMessage());
        assertEquals(FILE_NOT_FOUND.getErrorCode(), response.getBody().getId());
    }

    @Test
    void deleteFile_shouldReturnOkAndSuccessfulDeleteFile() {
        generateFilesInDatabase();

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/file?filename=%s".formatted(FILENAME),
                HttpMethod.DELETE,
                requestEntity,
                Void.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(fileRepository.findByFilename(FILENAME).isPresent());
    }

    @Test
    void deleteFile_shouldReturnErrorFileNotFound() {

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/file?filename=%s".formatted(FILENAME),
                HttpMethod.DELETE,
                requestEntity,
                ErrorResponse.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(FILE_NOT_FOUND.getMessage() + ": " + FILENAME, response.getBody().getMessage());
        assertEquals(FILE_NOT_FOUND.getErrorCode(), response.getBody().getId());
    }

    @Test
    void getFiles_shouldReturnOkAndListOfFilesIfExists() {
        int limit = 3;
        generateFilesInDatabase();

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<List<FileForListResponse>> response = restTemplate.exchange(
                "/list?limit=%d".formatted(limit),
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        assertTrue(response.getBody().size() <= limit);
        assertEquals(FileForListResponse.class, response.getBody().getFirst().getClass());
        assertEquals(SIZE, response.getBody().getFirst().getSize());
    }

    @Test
    void getFiles_shouldReturnEmptyList() {
        int limit = 3;

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<List<FileForListResponse>> response = restTemplate.exchange(
                "/list?limit=%d".formatted(limit),
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }


    private void generateFilesInDatabase() {
        int count = 3;
        fileRepository.save(createFile(FILENAME, DATA.getBytes()));
        for (int i = 0; i < count; i++) {
            File file = createFile(FILENAME + "_" + i, DATA.getBytes());
            fileRepository.save(file);
        }
    }

    private File createFile(String filename, byte[] data) {
        File file = new File();
        file.setFilename(filename);
        file.setData(data);
        file.setSize(SIZE);
        file.setContentType(CONTENT_TYPE);
        file.setHash(HASH);
        file.setUser(userRepository.findByLogin(LOGIN)
                .stream()
                .findFirst()
                .orElseThrow()
        );
        return file;
    }


    private static HttpEntity<MultiValueMap<String, Object>> getMultiValueMapHttpEntity() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        ByteArrayResource fileAsResource = new ByteArrayResource(DATA.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return FILENAME;
            }
        };

        body.add("file", fileAsResource);
        body.add("hash", HASH);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(body, headers);
    }
}
