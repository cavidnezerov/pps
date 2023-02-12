package payroll.processing.system.pps.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import payroll.processing.system.pps.domain.Employee;
import payroll.processing.system.pps.domain.FileResponse;
import payroll.processing.system.pps.domain.Record;
import payroll.processing.system.pps.domain.enumaration.EventType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class RecordServiceTest {
    private static final String URL = "src/test/resources/event.csv";
    private static final Path PATH = Paths.get(URL);
    private static final String FILE_NAME = "event.csv";
    private static final String ORIGINAL_FILE_NAME = "event.csv";
    private static final String CONTENT_TYPE = "text/csv";

    private MultipartFile file;
    private MultipartFile [] files;

    private final static String[] LINE_1 = new String[] {"1", "emp101", "Bill", "Gates", "Software Engineer", "ONBOARD", "1-11-2022", "10-10-2022", "“Bill Gates is going to join DataOrb on 1st November as a SE.”"};
    private final static String[] LINE_2 = new String[] {"2", "emp102", "Steve", "Jobs", "Architect", "ONBOARD", "1-10-2022", "10-10-2022", "“Steve Jobs joined DataOrb on 1st October as an Architect.”"};
    private final static String[] LINE_3 = new String[] {"3", "emp102", "SALARY", "3000", "10-10-2022", "“Oct Salary of Steve.”"};
    private final static String[] LINE_4 = new String[] {"4", "emp102", "EXIT", "12-10-2022", "12-9-2022", "“EXIT”"};
    private final static String[] LINE_5 = new String[] {"5", "emp101", "BONUS, 1000", "12-9-2022", "“BONUS”"};

    private static List<String[]> LINES;

    private static FileResponse FILE_RESPONSE;

    private final static String SEQUENCE_1 = "1";
    private final static String EMP_ID = "emp101";
    private final static String NAME = "Bill";
    private final static String SURNAME = "GATES";
    private final static String DESIGNATION = "Architect";
    private final static String EVENT = "ONBOARD";
    private final static String VALUE = "1-11-2022";
    private final static String EVENT_DATE = "10-10-2022";
    private final static String NOTE = "“Bill Gates is going to join DataOrb on 1st November as a SE.”";
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("M-dd-yyyy");

    private static Record<?> ONBOARD_RECORD;

    @Mock
    FileService fileService;

    @Mock
    BuilderService builderService;

    @InjectMocks
    RecordService recordService;

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

        LINES = Arrays.asList(LINE_1, LINE_2, LINE_3, LINE_4, LINE_5);
        FILE_RESPONSE = new FileResponse(LINES, new ArrayList<>());
        ONBOARD_RECORD = Record.builder()
                .SequenceNo(Long.parseLong(SEQUENCE_1))
                .employee(Employee.builder()
                        .id(EMP_ID)
                        .name(NAME)
                        .surname(SURNAME)
                        .designation(DESIGNATION).build())
                .type(EventType.ONBOARD)
                .value(LocalDate.parse(VALUE, FORMATTER))
                .date(LocalDate.parse(EVENT_DATE, FORMATTER))
                .note(NOTE).build();
    }

    @Test
    public void test_getRecords_Should_Return_5_Records_0_Error() {
        //when
        when(fileService.readFile(any())).thenReturn(FILE_RESPONSE);
        doReturn(ONBOARD_RECORD).when(builderService).buildOnboardRecord(any());
        doReturn(ONBOARD_RECORD).when(builderService).buildExitRecord(any());
        doReturn(ONBOARD_RECORD).when(builderService).buildPaidRecord(any());

        var response = recordService.getRecords(files);

        assertThat(response.getRecords().size()).isEqualTo(5);
        assertThat(response.getRecords().get(0).getEmployee().getId()).isEqualTo("emp101");
        assertThat(response.getFileErrors().size()).isEqualTo(0);
        assertThat(response.getRecordErrors().size()).isEqualTo(0);

        verify(fileService, times(1)).readFile(files);
        verifyNoMoreInteractions(fileService);
        verify(builderService, times(2)).buildOnboardRecord(any());
        verify(builderService, times(1)).buildExitRecord(LINE_4);
        verify(builderService, times(2)).buildPaidRecord(any());
        verifyNoMoreInteractions(builderService);
    }

    @Test
    public void test_getRecords_Should_Return_4_Records_1_Error() {
        String[] LINE_6 = new String[] {"1", "Steve"};
        LINES.set(0, LINE_6);

        //when
        when(fileService.readFile(any())).thenReturn(FILE_RESPONSE);
        doReturn(ONBOARD_RECORD).when(builderService).buildOnboardRecord(any());
        doReturn(ONBOARD_RECORD).when(builderService).buildExitRecord(any());
        doReturn(ONBOARD_RECORD).when(builderService).buildPaidRecord(any());

        var response = recordService.getRecords(files);

        assertThat(response.getRecords().size()).isEqualTo(4);
        assertThat(response.getRecords().get(1).getEmployee().getId()).isEqualTo("emp101");
        assertThat(response.getFileErrors().size()).isEqualTo(0);
        assertThat(response.getRecordErrors().size()).isEqualTo(1);

        verify(fileService, times(1)).readFile(files);
        verifyNoMoreInteractions(fileService);
        verify(builderService, times(1)).buildOnboardRecord(any());
        verify(builderService, times(1)).buildExitRecord(LINE_4);
        verify(builderService, times(2)).buildPaidRecord(any());
        verifyNoMoreInteractions(builderService);
    }
}
