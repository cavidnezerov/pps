package payroll.processing.system.pps.service;

import org.springframework.stereotype.Service;
import payroll.processing.system.pps.domain.Employee;
import payroll.processing.system.pps.domain.Record;
import payroll.processing.system.pps.domain.enumaration.EventType;
import payroll.processing.system.pps.domain.report.Report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Service
public class BuilderService {
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("M-dd-yyyy");

    Record<?> buildOnboardRecord(String[] line) {
        return Record.builder()
                .SequenceNo(Long.parseLong(line[0]))
                .employee(Employee.builder()
                        .id(line[1].trim())
                        .name(line[2])
                        .surname(line[3])
                        .designation(line[4]).build())
                .type(EventType.ONBOARD)
                .value(LocalDate.parse(line[6].trim(), FORMATTER))
                .date(LocalDate.parse(line[7].trim(), FORMATTER))
                .note(line[8]).build();
    }

    Record<?> buildExitRecord(String[] line) {
        return Record.builder()
                .SequenceNo(Long.parseLong(line[0]))
                .employee(Employee.builder().id(line[1].trim()).build())
                .type(EventType.EXIT)
                .value(LocalDate.parse(line[3].trim(), FORMATTER))
                .date(LocalDate.parse(line[4].trim(), FORMATTER))
                .note(line[5]).build();
    }

    Record<?> buildPaidRecord(String[] line) {
        return Record.builder()
                .SequenceNo(Long.parseLong(line[0]))
                .employee(Employee.builder().id(line[1].trim()).build())
                .type(EventType.valueOf(line[2]))
                .value(new BigDecimal(line[3].trim()))
                .date(LocalDate.parse(line[4].trim(), FORMATTER))
                .note(line[5]).build();
    }

    Report buildInitialReport() {
        return Report.builder()
                .totalEmployees(0L)
                .employeeReports(new ArrayList<>())
                .monthlyEmployeeReports(new ArrayList<>())
                .monthlyPaidReports(new ArrayList<>())
                .monthlySalaryReports(new ArrayList<>())
                .yearlyReports(new ArrayList<>())
                .build();
    }
}
