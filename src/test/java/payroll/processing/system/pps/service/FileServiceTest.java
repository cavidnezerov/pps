package payroll.processing.system.pps.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import static org.assertj.core.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {
    private static final String URL = "src/test/resources/event.csv";
    private static final Path PATH = Paths.get(URL);
    private static final String FILE_NAME = "event.csv";
    private static final String ORIGINAL_FILE_NAME = "event.csv";
    private static final String CONTENT_TYPE = "text/csv";

    private static final String WRONG_URL = "src/test/resources/event.jpeg";
    private static final Path WRONG_PATH = Paths.get(WRONG_URL);
    private static final String WRONG_FILE_NAME = "event.jpeg";
    private static final String WRONG_ORIGINAL_FILE_NAME = "event.jpeg";
    private static final String WRONG_CONTENT_TYPE = "image/jpeg";

    private MultipartFile file;
    private MultipartFile [] files;

    @InjectMocks
    FileService fileService;

    @BeforeEach
    public void setUp() {
        byte[] content;
        try {
            content = Files.readAllBytes(PATH);
            file = new MockMultipartFile(FILE_NAME, ORIGINAL_FILE_NAME, CONTENT_TYPE, content);
            files = new MultipartFile[1];
            files[0] = file;
        } catch (final IOException e) {

        }
    }

    @Test
    public void test_ReadFile_Should_Return_4_lines() {
       var response = fileService.readFile(files);

        assertThat(response.getLines().size()).isEqualTo(4);
        assertThat(response.getLines().get(0).length).isEqualTo(9);
        assertThat(response.getLines().get(3).length).isEqualTo(6);
        assertThat(response.getErrors().size()).isEqualTo(0);
    }

    @Test
    public void test_ReadFile_Should_Return_1_error() {
        byte[] content;
        try {
            content = Files.readAllBytes(WRONG_PATH);
            file = new MockMultipartFile(WRONG_FILE_NAME, WRONG_ORIGINAL_FILE_NAME, WRONG_CONTENT_TYPE, content);
            files = new MultipartFile[1];
            files[0] = file;
        } catch (final IOException e) {

        }
        var response = fileService.readFile(files);

        assertThat(response.getLines().size()).isEqualTo(0);
        assertThat(response.getErrors().size()).isEqualTo(1);
    }

}
