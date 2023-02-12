package payroll.processing.system.pps.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import payroll.processing.system.pps.domain.Error;
import payroll.processing.system.pps.domain.PayrollResponse;
import payroll.processing.system.pps.domain.Record;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static payroll.processing.system.pps.domain.enumaration.EventType.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PpsService {
    private final RecordService recordService;
    private final ReportService reportService;
    private final BuilderService builderService;

    public PayrollResponse processPayroll(MultipartFile[] files) {
        var report = builderService.buildInitialReport();
        var recordResponse = recordService.getRecords(files);
        var records = recordResponse.getRecords();
        var errors = new ArrayList<String>();

        records.forEach(record -> {
            if (record.getType().equals(ONBOARD)) {
                var aRecord = (Record<LocalDate>) record;
                reportService.increaseTotalEmployees(report, aRecord);
                reportService.addOnboardedEmployee(report, aRecord);
                reportService.addYearlyEvent(report, aRecord);
            } else if (record.getType().equals(EXIT)) {
                var aRecord = (Record<LocalDate>) record;
                reportService.decreaseTotalEmployees(report);
                reportService.addExitedEmployee(report, aRecord, errors);
                reportService.addYearlyEvent(report, record);
            } else if (record.getType().equals(SALARY)) {
                var aRecord = (Record<BigDecimal>) record;
                reportService.addSalaryReport(report, aRecord);
                reportService.addPaidReport(report, aRecord);
                reportService.addEmployeePaid(report, aRecord, errors);
                reportService.addYearlyEvent(report, aRecord);
            } else {
                var aRecord = (Record<BigDecimal>) record;
                reportService.addPaidReport(report, aRecord);
                reportService.addEmployeePaid(report, aRecord, errors);
                reportService.addYearlyEvent(report, aRecord);
            }
        });

        if (report.getTotalEmployees() < 0) {
            errors.add(0, "Some exited employees dont found. " +
                    "Because of that total number of employees is negative. " +
                    "For more information look at other error logs");
        }

        report.getMonthlyPaidReports().forEach(mpr -> mpr.setEmployeeIds(null));

        return PayrollResponse.builder()
                .report(report)
                .error(Error.builder()
                        .reportErrors(errors)
                        .recordErrors(recordResponse.getRecordErrors())
                        .fileErrors(recordResponse.getFileErrors())
                        .build())
                .build();
    }

}