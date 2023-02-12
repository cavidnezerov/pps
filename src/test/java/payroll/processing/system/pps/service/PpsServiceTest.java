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
import payroll.processing.system.pps.domain.RecordResponse;
import payroll.processing.system.pps.domain.enumaration.EventType;
import payroll.processing.system.pps.domain.report.Report;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class PpsServiceTest {
    private static final String URL = "src/test/resources/event.csv";
    private static final Path PATH = Paths.get(URL);
    private static final String FILE_NAME = "event.csv";
    private static final String ORIGINAL_FILE_NAME = "event.csv";
    private static final String CONTENT_TYPE = "text/csv";
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

    private MultipartFile file;
    private MultipartFile [] files;
    private Report initialReport;
    private Record<?> record;
    private RecordResponse recordResponse;


    @Mock
    BuilderService builderService;

    @Mock
    RecordService recordService;

    @Mock
    ReportService reportService;

    @InjectMocks
    PpsService ppsService;


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

        initialReport = Report.builder()
                .totalEmployees(0L)
                .employeeReports(new ArrayList<>())
                .monthlyEmployeeReports(new ArrayList<>())
                .monthlyPaidReports(new ArrayList<>())
                .monthlySalaryReports(new ArrayList<>())
                .yearlyReports(new ArrayList<>())
                .build();

        record = Record.builder()
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

        recordResponse = new RecordResponse(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        recordResponse.getRecords().add(record);
    }

    @Test
    public void test_processPayroll_Should_Work_On_ONBOARD_Condition() {
        when(builderService.buildInitialReport()).thenReturn(initialReport);
        when(recordService.getRecords(files)).thenReturn(recordResponse);
        doNothing().when(reportService).increaseTotalEmployees(initialReport, (Record<LocalDate>) record);
        doNothing().when(reportService).addOnboardedEmployee(initialReport, (Record<LocalDate>) record);
        doNothing().when(reportService).addYearlyEvent(initialReport, record);

        var response = ppsService.processPayroll(files);

        response.getReport().getMonthlyPaidReports().forEach(monthlyPaidReport -> {
            assertThat(monthlyPaidReport.getEmployeeIds()).isNull();
        });

        verify(builderService, times(1)).buildInitialReport();
        verifyNoMoreInteractions(builderService);
        verify(recordService, times(1)).getRecords(files);
        verifyNoMoreInteractions(recordService);
        verify(reportService, times(1)).increaseTotalEmployees(initialReport, (Record<LocalDate>) record);
        verify(reportService, times(1)).addOnboardedEmployee(initialReport, (Record<LocalDate>) record);
        verify(reportService, times(1)).addYearlyEvent(initialReport, record);
        verifyNoMoreInteractions(reportService);
    }

    @Test
    public void test_processPayroll_Should_Work_On_EXIT_Condition() {
        record.setType(EventType.EXIT);
        when(builderService.buildInitialReport()).thenReturn(initialReport);
        when(recordService.getRecords(files)).thenReturn(recordResponse);
        doNothing().when(reportService).decreaseTotalEmployees(initialReport);
        doNothing().when(reportService).addExitedEmployee(eq(initialReport), eq((Record<LocalDate>) record), anyList());
        doNothing().when(reportService).addYearlyEvent(initialReport, record);

        var response = ppsService.processPayroll(files);

        response.getReport().getMonthlyPaidReports().forEach(monthlyPaidReport -> {
            assertThat(monthlyPaidReport.getEmployeeIds()).isNull();
        });

        verify(builderService, times(1)).buildInitialReport();
        verifyNoMoreInteractions(builderService);
        verify(recordService, times(1)).getRecords(files);
        verifyNoMoreInteractions(recordService);
        verify(reportService, times(1)).decreaseTotalEmployees(initialReport);
        verify(reportService, times(1)).addExitedEmployee(eq(initialReport), eq((Record<LocalDate>) record), anyList());
        verify(reportService, times(1)).addYearlyEvent(initialReport, record);
        verifyNoMoreInteractions(reportService);
    }

    @Test
    public void test_processPayroll_Should_Work_On_SALARY_Condition() {
        record.setType(EventType.SALARY);

        when(builderService.buildInitialReport()).thenReturn(initialReport);
        when(recordService.getRecords(files)).thenReturn(recordResponse);
        doNothing().when(reportService).addSalaryReport(initialReport, (Record<BigDecimal>) record);
        doNothing().when(reportService).addPaidReport(initialReport, (Record<BigDecimal>) record);
        doNothing().when(reportService).addEmployeePaid(eq(initialReport), eq((Record<BigDecimal>) record), anyList());
        doNothing().when(reportService).addYearlyEvent(initialReport, record);

        var response = ppsService.processPayroll(files);

        response.getReport().getMonthlyPaidReports().forEach(monthlyPaidReport -> {
            assertThat(monthlyPaidReport.getEmployeeIds()).isNull();
        });

        verify(builderService, times(1)).buildInitialReport();
        verifyNoMoreInteractions(builderService);
        verify(recordService, times(1)).getRecords(files);
        verifyNoMoreInteractions(recordService);
        verify(reportService, times(1)).addSalaryReport(initialReport, (Record<BigDecimal>) record);
        verify(reportService, times(1)).addPaidReport(initialReport, (Record<BigDecimal>) record);
        verify(reportService, times(1)).addEmployeePaid(eq(initialReport), eq((Record<BigDecimal>) record), anyList());
        verify(reportService, times(1)).addYearlyEvent(initialReport, record);
        verifyNoMoreInteractions(reportService);
    }

    @Test
    public void test_processPayroll_Should_Work_On_BONUS_Condition() {
        record.setType(EventType.BONUS);

        when(builderService.buildInitialReport()).thenReturn(initialReport);
        when(recordService.getRecords(files)).thenReturn(recordResponse);
        doNothing().when(reportService).addPaidReport(initialReport, (Record<BigDecimal>) record);
        doNothing().when(reportService).addEmployeePaid(eq(initialReport), eq((Record<BigDecimal>) record), anyList());
        doNothing().when(reportService).addYearlyEvent(initialReport, record);

        var response = ppsService.processPayroll(files);

        response.getReport().getMonthlyPaidReports().forEach(monthlyPaidReport -> {
            assertThat(monthlyPaidReport.getEmployeeIds()).isNull();
        });

        verify(builderService, times(1)).buildInitialReport();
        verifyNoMoreInteractions(builderService);
        verify(recordService, times(1)).getRecords(files);
        verifyNoMoreInteractions(recordService);
        verify(reportService, times(1)).addPaidReport(initialReport, (Record<BigDecimal>) record);
        verify(reportService, times(1)).addEmployeePaid(eq(initialReport), eq((Record<BigDecimal>) record), anyList());
        verify(reportService, times(1)).addYearlyEvent(initialReport, record);
        verifyNoMoreInteractions(reportService);
    }

    @Test
    public void test_processPayroll_Should_Add_An_Error_To_Response() {
        record.setType(EventType.BONUS);
        initialReport.setTotalEmployees(-1L);

        when(builderService.buildInitialReport()).thenReturn(initialReport);
        when(recordService.getRecords(files)).thenReturn(recordResponse);
        doNothing().when(reportService).addPaidReport(initialReport, (Record<BigDecimal>) record);
        doNothing().when(reportService).addEmployeePaid(eq(initialReport), eq((Record<BigDecimal>) record), anyList());
        doNothing().when(reportService).addYearlyEvent(initialReport, record);

        var response = ppsService.processPayroll(files);

        response.getReport().getMonthlyPaidReports().forEach(monthlyPaidReport -> {
            assertThat(monthlyPaidReport.getEmployeeIds()).isNull();
        });

        assertThat(response.getError().getReportErrors().size()).isEqualTo(1);

        verify(builderService, times(1)).buildInitialReport();
        verifyNoMoreInteractions(builderService);
        verify(recordService, times(1)).getRecords(files);
        verifyNoMoreInteractions(recordService);
        verify(reportService, times(1)).addPaidReport(initialReport, (Record<BigDecimal>) record);
        verify(reportService, times(1)).addEmployeePaid(eq(initialReport), eq((Record<BigDecimal>) record), anyList());
        verify(reportService, times(1)).addYearlyEvent(initialReport, record);
        verifyNoMoreInteractions(reportService);
    }
}
