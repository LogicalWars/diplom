package ru.netology.netologydiplombackend.integration.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.netology.netologydiplombackend.config.TestSecurityConfig;
import ru.netology.netologydiplombackend.dto.ErrorResponse;
import ru.netology.netologydiplombackend.integration.TestcontainersConfiguration;
import ru.netology.netologydiplombackend.model.File;
import ru.netology.netologydiplombackend.model.User;
import ru.netology.netologydiplombackend.repository.FileRepository;
import ru.netology.netologydiplombackend.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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

        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
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
        File alreadyExistsFile = new File();
        alreadyExistsFile.setFilename(FILENAME);
        alreadyExistsFile.setData(DATA.getBytes());
        alreadyExistsFile.setUser(user);

        fileRepository.save(alreadyExistsFile);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = getMultiValueMapHttpEntity();

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/file?filename=%s".formatted(FILENAME),
                requestEntity,
                ErrorResponse.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("File already exists: %s".formatted(FILENAME), response.getBody().getMessage());
        assertEquals(40002, response.getBody().getId());
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

        return new HttpEntity<>(body, headers);
    }
}
