package payroll.processing.system.pps.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import payroll.processing.system.pps.domain.Employee;
import payroll.processing.system.pps.domain.Record;
import payroll.processing.system.pps.domain.enumaration.EventType;
import payroll.processing.system.pps.domain.report.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {
    private final static String SEQUENCE_1 = "1";
    private final static String EMP_ID = "emp101";
    private final static String NAME = "Bill";
    private final static String SURNAME = "GATES";
    private final static String DESIGNATION = "Architect";
    private final static String EVENT = "ONBOARD";
    private final static String VALUE = "1-11-2022";
    private final static String EVENT_DATE = "10-10-2022";
    private final static String NOTE = "“Bill Gates is going to join DataOrb on 1st November as a SE.”";

    private final static String SEQUENCE_2 = "2";
    private final static String EMP_ID_2 = "emp102";
    private final static String NAME_2 = "Steve";
    private final static String SURNAME_2 = "Jobs";
    private final static String DESIGNATION_2 = "Developer";
    private final static String EVENT_2 = "ONBOARD";
    private final static String VALUE_2 = "4-8-2022";
    private final static String EVENT_DATE_2 = "11-11-2022";
    private final static String NOTE_2 = "Test Note";
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("M-dd-yyyy");
    private Report report;
    private Record<?> record;

    private List<String> errors;

    @InjectMocks
    ReportService reportService;

    @BeforeEach
    public void setUp() {
        report = Report.builder()
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

        errors = new ArrayList<>();
    }

    @Test
    public void test_increaseTotalEmployees_Should_Increase_One_Point_totalEmployees() {
        reportService.increaseTotalEmployees(report, (Record<LocalDate>) record);

        assertThat(report.getTotalEmployees()).isEqualTo(1L);
        assertThat(report.getEmployeeReports().size()).isEqualTo(1);
        assertThat(report.getEmployeeReports().get(0).getId()).isEqualTo(EMP_ID);
    }

    @Test
    public void test_increaseTotalEmployees_Should_Decrease_One_Point_totalEmployees() {
        report.setTotalEmployees(5L);
        reportService.decreaseTotalEmployees(report);

        assertThat(report.getTotalEmployees()).isEqualTo(4L);
    }

    @Test
    public void test_addOnboardedEmployee_Should_Add_MonthlyEmployeeReport_AND_Onboarded_Employee_Reports() {
        reportService.addOnboardedEmployee(report, (Record<LocalDate>) record);

        assertThat(report.getMonthlyEmployeeReports().size()).isEqualTo(1);
        assertThat(report.getMonthlyEmployeeReports().get(0).getOnboardedEmployees().get(0).getId()).isEqualTo(EMP_ID);
        assertThat(report.getMonthlyEmployeeReports().get(0).getOnboardedEmployees().get(0).getName()).isEqualTo(NAME);
        assertThat(report.getMonthlyEmployeeReports().get(0).getTotalOnboardedEmployees()).isEqualTo(1L);
    }

    @Test
    public void test_addOnboardedEmployee_Should_ONLY_Add_Onboarded_Employee_Reports() {
        var employee = EmployeeReport.builder()
                .id(EMP_ID_2)
                .name(NAME_2)
                .surname(SURNAME_2)
                .designation(DESIGNATION_2).build();



        var mer = MonthlyEmployeeReport.builder()
                .month(YearMonth.from(LocalDate.parse(VALUE, FORMATTER)))
                .totalOnboardedEmployees(1L)
                .OnboardedEmployees(new ArrayList<>())
                .totalExitedEmployee(0L)
                .exitedEmployees(new ArrayList<>()).build();

        mer.getOnboardedEmployees().add(employee);
        report.getMonthlyEmployeeReports().add(mer);

        reportService.addOnboardedEmployee(report, (Record<LocalDate>) record);

        assertThat(report.getMonthlyEmployeeReports().size()).isEqualTo(1);
        assertThat(report.getMonthlyEmployeeReports().get(0).getOnboardedEmployees().get(0).getId()).isEqualTo(EMP_ID_2);
        assertThat(report.getMonthlyEmployeeReports().get(0).getOnboardedEmployees().get(1).getId()).isEqualTo(EMP_ID);
        assertThat(report.getMonthlyEmployeeReports().get(0).getOnboardedEmployees().get(0).getName()).isEqualTo(NAME_2);
        assertThat(report.getMonthlyEmployeeReports().get(0).getOnboardedEmployees().get(1).getName()).isEqualTo(NAME);
        assertThat(report.getMonthlyEmployeeReports().get(0).getTotalOnboardedEmployees()).isEqualTo(2L);
        assertThat(report.getMonthlyEmployeeReports().get(0).getOnboardedEmployees().size()).isEqualTo(2);
    }

    @Test
    public void test_addExitedEmployee_Should_Add_MonthlyEmployeeReport_AND_Onboarded_Employee_Reports() {
        var employee = EmployeeReport.builder()
                .id(EMP_ID)
                .name(NAME)
                .surname(SURNAME)
                .designation(DESIGNATION).build();

        report.getEmployeeReports().add(employee);

        reportService.addExitedEmployee(report, (Record<LocalDate>) record, errors);

        assertThat(report.getMonthlyEmployeeReports().size()).isEqualTo(1);
        assertThat(report.getMonthlyEmployeeReports().get(0).getExitedEmployees().get(0).getId()).isEqualTo(EMP_ID);
        assertThat(report.getMonthlyEmployeeReports().get(0).getExitedEmployees().get(0).getName()).isEqualTo(NAME);
        assertThat(report.getMonthlyEmployeeReports().get(0).getTotalExitedEmployee()).isEqualTo(1L);
    }

    @Test
    public void test_addExitedEmployee_Should_ONLY_Add_Onboarded_Employee_Reports() {
        var employee = EmployeeReport.builder()
                .id(EMP_ID)
                .name(NAME)
                .surname(SURNAME)
                .designation(DESIGNATION).build();

        var employee2 = EmployeeReport.builder()
                .id(EMP_ID_2)
                .name(NAME_2)
                .surname(SURNAME_2)
                .designation(DESIGNATION_2).build();

        var mer = MonthlyEmployeeReport.builder()
                .month(YearMonth.from(LocalDate.parse(VALUE, FORMATTER)))
                .totalOnboardedEmployees(0L)
                .OnboardedEmployees(new ArrayList<>())
                .totalExitedEmployee(1L)
                .exitedEmployees(new ArrayList<>()).build();

        mer.getExitedEmployees().add(employee2);
        report.getMonthlyEmployeeReports().add(mer);

        report.getEmployeeReports().add(employee);
        report.getEmployeeReports().add(employee2);

        reportService.addExitedEmployee(report, (Record<LocalDate>) record, errors);

        assertThat(report.getMonthlyEmployeeReports().size()).isEqualTo(1);
        assertThat(report.getMonthlyEmployeeReports().get(0).getExitedEmployees().get(1).getId()).isEqualTo(EMP_ID);
        assertThat(report.getMonthlyEmployeeReports().get(0).getExitedEmployees().get(0).getId()).isEqualTo(EMP_ID_2);
        assertThat(report.getMonthlyEmployeeReports().get(0).getExitedEmployees().get(1).getName()).isEqualTo(NAME);
        assertThat(report.getMonthlyEmployeeReports().get(0).getExitedEmployees().get(0).getName()).isEqualTo(NAME_2);
        assertThat(report.getMonthlyEmployeeReports().get(0).getTotalExitedEmployee()).isEqualTo(2L);
    }

    @Test
    public void test_addExitedEmployee_Should_ONLY_Add_An_Error() {
        reportService.addExitedEmployee(report, (Record<LocalDate>) record, errors);

        assertThat(errors.size()).isEqualTo(1);
    }

    @Test
    public void test_addSalaryReport_Should_Add_MonthlySalaryReport_AND_Increase_Salary_Reports() {
        record = Record.builder()
                .SequenceNo(Long.parseLong(SEQUENCE_1))
                .employee(Employee.builder()
                        .id(EMP_ID)
                        .name(NAME)
                        .surname(SURNAME)
                        .designation(DESIGNATION).build())
                .type(EventType.SALARY)
                .value(BigDecimal.valueOf(100))
                .date(LocalDate.parse(EVENT_DATE, FORMATTER))
                .note(NOTE).build();

        reportService.addSalaryReport(report, (Record<BigDecimal>) record);

        assertThat(report.getMonthlySalaryReports().size()).isEqualTo(1);
        assertThat(report.getMonthlySalaryReports().get(0).getTotalSalary()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(report.getMonthlySalaryReports().get(0).getTotalEmployees()).isEqualTo(1L);
    }

    @Test
    public void test_addSalaryReport_Should_Increase_Salary_Reports() {
        record = Record.builder()
                .SequenceNo(Long.parseLong(SEQUENCE_1))
                .employee(Employee.builder()
                        .id(EMP_ID)
                        .name(NAME)
                        .surname(SURNAME)
                        .designation(DESIGNATION).build())
                .type(EventType.SALARY)
                .value(BigDecimal.valueOf(100))
                .date(LocalDate.parse(EVENT_DATE, FORMATTER))
                .note(NOTE).build();

        var msr = MonthlySalaryReport.builder()
                .month(YearMonth.from(LocalDate.parse(EVENT_DATE, FORMATTER)))
                .totalSalary(BigDecimal.valueOf(1200))
                .totalEmployees(3L).build();

        report.getMonthlySalaryReports().add(msr);

        reportService.addSalaryReport(report, (Record<BigDecimal>) record);

        assertThat(report.getMonthlySalaryReports().size()).isEqualTo(1);
        assertThat(report.getMonthlySalaryReports().get(0).getTotalSalary()).isEqualTo(BigDecimal.valueOf(1300));
        assertThat(report.getMonthlySalaryReports().get(0).getTotalEmployees()).isEqualTo(4L);
    }

    @Test
    public void test_addPaidReport_Should_Add_MonthlyPaidReport_AND_Increase_Paid_Reports() {
        record = Record.builder()
                .SequenceNo(Long.parseLong(SEQUENCE_1))
                .employee(Employee.builder()
                        .id(EMP_ID)
                        .name(NAME)
                        .surname(SURNAME)
                        .designation(DESIGNATION).build())
                .type(EventType.SALARY)
                .value(BigDecimal.valueOf(100))
                .date(LocalDate.parse(EVENT_DATE, FORMATTER))
                .note(NOTE).build();

        reportService.addPaidReport(report, (Record<BigDecimal>) record);

        assertThat(report.getMonthlyPaidReports().size()).isEqualTo(1);
        assertThat(report.getMonthlyPaidReports().get(0).getTotalPaid()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(report.getMonthlyPaidReports().get(0).getTotalEmployees()).isEqualTo(1L);
        assertThat(report.getMonthlyPaidReports().get(0).getEmployeeIds().get(0)).isEqualTo(EMP_ID);
    }

    @Test
    public void test_addPaidReport_Should_AND_Increase_Paid_Reports() {
        record = Record.builder()
                .SequenceNo(Long.parseLong(SEQUENCE_1))
                .employee(Employee.builder()
                        .id(EMP_ID)
                        .name(NAME)
                        .surname(SURNAME)
                        .designation(DESIGNATION).build())
                .type(EventType.SALARY)
                .value(BigDecimal.valueOf(100))
                .date(LocalDate.parse(EVENT_DATE, FORMATTER))
                .note(NOTE).build();

        var mpr = MonthlyPaidReport.builder()
                .month(YearMonth.from(LocalDate.parse(EVENT_DATE, FORMATTER)))
                .totalPaid(BigDecimal.valueOf(1200))
                .totalEmployees(3L)
                .employeeIds(new ArrayList<>()).build();

        report.getMonthlyPaidReports().add(mpr);

        reportService.addPaidReport(report, (Record<BigDecimal>) record);

        assertThat(report.getMonthlyPaidReports().size()).isEqualTo(1);
        assertThat(report.getMonthlyPaidReports().get(0).getTotalPaid()).isEqualTo(BigDecimal.valueOf(1300));
        assertThat(report.getMonthlyPaidReports().get(0).getTotalEmployees()).isEqualTo(4L);
        assertThat(report.getMonthlyPaidReports().get(0).getEmployeeIds().get(0)).isEqualTo(EMP_ID);
    }

    @Test
    public void test_addPaidReport_Should_AND_Increase_Paid_Reports_But_Should_Not_Increase_Total_Employee() {
        record = Record.builder()
                .SequenceNo(Long.parseLong(SEQUENCE_1))
                .employee(Employee.builder()
                        .id(EMP_ID)
                        .name(NAME)
                        .surname(SURNAME)
                        .designation(DESIGNATION).build())
                .type(EventType.SALARY)
                .value(BigDecimal.valueOf(100))
                .date(LocalDate.parse(EVENT_DATE, FORMATTER))
                .note(NOTE).build();

        var mpr = MonthlyPaidReport.builder()
                .month(YearMonth.from(LocalDate.parse(EVENT_DATE, FORMATTER)))
                .totalPaid(BigDecimal.valueOf(1200))
                .totalEmployees(3L)
                .employeeIds(new ArrayList<>()).build();

        mpr.getEmployeeIds().add(EMP_ID);

        report.getMonthlyPaidReports().add(mpr);

        reportService.addPaidReport(report, (Record<BigDecimal>) record);

        assertThat(report.getMonthlyPaidReports().size()).isEqualTo(1);
        assertThat(report.getMonthlyPaidReports().get(0).getTotalPaid()).isEqualTo(BigDecimal.valueOf(1300));
        assertThat(report.getMonthlyPaidReports().get(0).getTotalEmployees()).isEqualTo(3L);
        assertThat(report.getMonthlyPaidReports().get(0).getEmployeeIds().get(0)).isEqualTo(EMP_ID);
    }

    @Test
    public void test_EmployeePaid_Should_Increase_Employee_Total_Paid() {
        var employee = EmployeeReport.builder()
                .id(EMP_ID)
                .name(NAME)
                .surname(SURNAME)
                .designation(DESIGNATION)
                .totalPaid(BigDecimal.valueOf(250)).build();

        report.getEmployeeReports().add(employee);

        record = Record.builder()
                .SequenceNo(Long.parseLong(SEQUENCE_1))
                .employee(Employee.builder()
                        .id(EMP_ID)
                        .name(NAME)
                        .surname(SURNAME)
                        .designation(DESIGNATION).build())
                .type(EventType.SALARY)
                .value(BigDecimal.valueOf(100))
                .date(LocalDate.parse(EVENT_DATE, FORMATTER))
                .note(NOTE).build();

        reportService.addEmployeePaid(report, (Record<BigDecimal>) record, errors);

        assertThat(report.getEmployeeReports().get(0).getTotalPaid()).isEqualTo(BigDecimal.valueOf(350));
    }

    @Test
    public void test_EmployeePaid_Should_Add_An_Error() {
        record = Record.builder()
                .SequenceNo(Long.parseLong(SEQUENCE_1))
                .employee(Employee.builder()
                        .id(EMP_ID)
                        .name(NAME)
                        .surname(SURNAME)
                        .designation(DESIGNATION).build())
                .type(EventType.SALARY)
                .value(BigDecimal.valueOf(100))
                .date(LocalDate.parse(EVENT_DATE, FORMATTER))
                .note(NOTE).build();

        reportService.addEmployeePaid(report, (Record<BigDecimal>) record, errors);

        assertThat(errors.size()).isEqualTo(1);
    }

    @Test
    public void test_addYearlyEvent_Should_Use_Value_As_Date_And_Add_New_Year_And_Event() {
        LocalDate date2022 = LocalDate.parse("1-11-2022", FORMATTER);
        Year year2022 = Year.from(date2022);
        LocalDate date2021 = LocalDate.parse("1-11-2021", FORMATTER);
        Year year2021 = Year.from(date2021);

        ((Record<LocalDate>)record).setValue(date2022);
        record.setDate(date2021);
        reportService.addYearlyEvent(report, record);

        assertThat(report.getYearlyReports().size()).isEqualTo(1);
        assertThat(report.getYearlyReports().get(0).getYear()).isEqualTo(year2022);
        assertThat(report.getYearlyReports().get(0).getEvents().get(0).getEmployeeId()).isEqualTo(EMP_ID);
    }

    @Test
    public void test_addYearlyEvent_Should_Use_Value_As_Date_And_Add_Event() {
        LocalDate date2022 = LocalDate.parse("1-11-2022", FORMATTER);
        Year year2022 = Year.from(date2022);
        LocalDate date2021 = LocalDate.parse("1-11-2021", FORMATTER);
        Year year2021 = Year.from(date2021);

        ((Record<LocalDate>)record).setValue(date2022);
        record.setDate(date2021);

        var event = EventReport.builder()
                .employeeId(EMP_ID_2)
                .date(date2022)
                .type(EventType.ONBOARD)
                .value(VALUE_2).build();

        var yp = YearlyReport.builder()
                .year(year2022)
                .events(new ArrayList<>()).build();

        yp.getEvents().add(event);
        report.getYearlyReports().add(yp);

        reportService.addYearlyEvent(report, record);

        assertThat(report.getYearlyReports().size()).isEqualTo(1);
        assertThat(report.getYearlyReports().get(0).getYear()).isEqualTo(year2022);
        assertThat(report.getYearlyReports().get(0).getEvents().get(0).getEmployeeId()).isEqualTo(EMP_ID_2);
        assertThat(report.getYearlyReports().get(0).getEvents().get(1).getEmployeeId()).isEqualTo(EMP_ID);
    }

    @Test
    public void test_addYearlyEvent_Should_Use_Event_Date_As_Date_And_Add_New_Year_And_Event() {
        record = Record.builder()
                .SequenceNo(Long.parseLong(SEQUENCE_1))
                .employee(Employee.builder()
                        .id(EMP_ID)
                        .name(NAME)
                        .surname(SURNAME)
                        .designation(DESIGNATION).build())
                .type(EventType.SALARY)
                .value(BigDecimal.valueOf(100))
                .date(LocalDate.parse(EVENT_DATE, FORMATTER))
                .note(NOTE).build();

        LocalDate date2022 = LocalDate.parse("1-11-2022", FORMATTER);
        Year year2022 = Year.from(date2022);
        LocalDate date2021 = LocalDate.parse("1-11-2021", FORMATTER);
        Year year2021 = Year.from(date2021);

        record.setDate(date2021);
        reportService.addYearlyEvent(report, record);

        assertThat(report.getYearlyReports().size()).isEqualTo(1);
        assertThat(report.getYearlyReports().get(0).getYear()).isEqualTo(year2021);
        assertThat(report.getYearlyReports().get(0).getEvents().get(0).getEmployeeId()).isEqualTo(EMP_ID);
    }

    @Test
    public void test_addYearlyEvent_Should_Use_Event_Date_As_Date_And_Add_Event() {
        record = Record.builder()
                .SequenceNo(Long.parseLong(SEQUENCE_1))
                .employee(Employee.builder()
                        .id(EMP_ID)
                        .name(NAME)
                        .surname(SURNAME)
                        .designation(DESIGNATION).build())
                .type(EventType.SALARY)
                .value(BigDecimal.valueOf(100))
                .date(LocalDate.parse(EVENT_DATE, FORMATTER))
                .note(NOTE).build();

        LocalDate date2022 = LocalDate.parse("1-11-2022", FORMATTER);
        Year year2022 = Year.from(date2022);
        LocalDate date2021 = LocalDate.parse("1-11-2021", FORMATTER);
        Year year2021 = Year.from(date2021);

        record.setDate(date2021);

        var event = EventReport.builder()
                .employeeId(EMP_ID_2)
                .date(date2021)
                .type(EventType.ONBOARD)
                .value(VALUE_2).build();

        var yp = YearlyReport.builder()
                .year(year2021)
                .events(new ArrayList<>()).build();

        yp.getEvents().add(event);
        report.getYearlyReports().add(yp);

        reportService.addYearlyEvent(report, record);

        assertThat(report.getYearlyReports().size()).isEqualTo(1);
        assertThat(report.getYearlyReports().get(0).getYear()).isEqualTo(year2021);
        assertThat(report.getYearlyReports().get(0).getEvents().get(0).getEmployeeId()).isEqualTo(EMP_ID_2);
        assertThat(report.getYearlyReports().get(0).getEvents().get(1).getEmployeeId()).isEqualTo(EMP_ID);
    }
}
