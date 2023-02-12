package payroll.processing.system.pps.service;

import org.springframework.stereotype.Service;
import payroll.processing.system.pps.domain.Record;
import payroll.processing.system.pps.domain.enumaration.EventType;
import payroll.processing.system.pps.domain.report.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    public void increaseTotalEmployees(Report report, Record<LocalDate> record) {
        var totalEmployees = report.getTotalEmployees() + 1;
        report.setTotalEmployees(totalEmployees);

        var employee = EmployeeReport.builder()
                .id(record.getEmployee().getId())
                .name(record.getEmployee().getName())
                .surname(record.getEmployee().getSurname())
                .designation(record.getEmployee().getDesignation())
                .totalPaid(BigDecimal.ZERO).build();

        report.getEmployeeReports().add(employee);
    }

    public void decreaseTotalEmployees(Report report) {
        var totalEmployees = report.getTotalEmployees() - 1;
        report.setTotalEmployees(totalEmployees);
    }

    public void addOnboardedEmployee(Report report, Record<LocalDate> record) {
        var employee = EmployeeReport.builder()
                .id(record.getEmployee().getId())
                .name(record.getEmployee().getName())
                .surname(record.getEmployee().getSurname())
                .designation(record.getEmployee().getDesignation()).build();

        report.getMonthlyEmployeeReports()
                .stream()
                .filter(mer -> mer.getMonth().compareTo(YearMonth.from(record.getValue())) == 0)
                .findAny()
                .ifPresentOrElse(
                        mer -> {
                            var totalOnboardedEmployees = mer.getTotalOnboardedEmployees() + 1;
                            mer.setTotalOnboardedEmployees(totalOnboardedEmployees);
                            mer.getOnboardedEmployees().add(employee);
                        },
                        () -> {
                            var mer = MonthlyEmployeeReport.builder()
                                    .month(YearMonth.from(record.getValue()))
                                    .totalOnboardedEmployees(1L)
                                    .OnboardedEmployees(new ArrayList<>())
                                    .totalExitedEmployee(0L)
                                    .exitedEmployees(new ArrayList<>()).build();

                            mer.getOnboardedEmployees().add(employee);
                            report.getMonthlyEmployeeReports().add(mer);
                        }
                );
    }

    public void addExitedEmployee(Report report, Record<LocalDate> record, List<String> errors) {
        report.getEmployeeReports()
                .stream()
                .filter(ep -> ep.getId().equals(record.getEmployee().getId()))
                .findAny()
                .ifPresentOrElse(
                        employee -> report.getMonthlyEmployeeReports()
                                .stream()
                                .filter(mer -> mer.getMonth().compareTo(YearMonth.from(record.getValue())) == 0)
                                .findAny()
                                .ifPresentOrElse(
                                        mer -> {
                                            var totalExitedEmployees = mer.getTotalExitedEmployee() + 1;
                                            mer.setTotalExitedEmployee(totalExitedEmployees);

                                            mer.getExitedEmployees().add(employee);
                                        },
                                        () -> {
                                            var mer = MonthlyEmployeeReport.builder()
                                                    .month(YearMonth.from(record.getValue()))
                                                    .totalOnboardedEmployees(0L)
                                                    .OnboardedEmployees(new ArrayList<>())
                                                    .totalExitedEmployee(1L)
                                                    .exitedEmployees(new ArrayList<>()).build();

                                            mer.getExitedEmployees().add(employee);
                                            report.getMonthlyEmployeeReports().add(mer);
                                        }
                                ),
                        () -> errors.add("There is no any employee appropriate to this record: " + record +
                                " ~ This can cause to wrong report result")
                );
    }

    public void addSalaryReport(Report report, Record<BigDecimal> record) {
        report.getMonthlySalaryReports()
                .stream()
                .filter(msr -> msr.getMonth().compareTo(YearMonth.from(record.getDate())) == 0)
                .findAny()
                .ifPresentOrElse(
                        msr -> {
                           var totalEmployees = msr.getTotalEmployees() + 1;
                           msr.setTotalEmployees(totalEmployees);

                           var totalSalary = msr.getTotalSalary().add(record.getValue());
                           msr.setTotalSalary(totalSalary);
                        },
                        () -> {
                            var msr = MonthlySalaryReport.builder()
                                    .month(YearMonth.from(record.getDate()))
                                    .totalSalary(record.getValue())
                                    .totalEmployees(1L).build();

                            report.getMonthlySalaryReports().add(msr);
                        }
                );
    }

    public void addPaidReport(Report report, Record<BigDecimal> record) {
        report.getMonthlyPaidReports()
                .stream()
                .filter(mpr -> mpr.getMonth().compareTo(YearMonth.from(record.getDate())) == 0)
                .findAny()
                .ifPresentOrElse(
                        mpr -> {
                            var totalPaid = mpr.getTotalPaid().add(record.getValue());
                            mpr.setTotalPaid(totalPaid);

                            mpr.getEmployeeIds().stream()
                                    .filter(empId -> empId.equals(record.getEmployee().getId()))
                                    .findAny()
                                    .ifPresentOrElse(
                                            empId -> {

                                            },
                                            () -> {
                                                var totalEmployees = mpr.getTotalEmployees() + 1;
                                                mpr.setTotalEmployees(totalEmployees);

                                                mpr.getEmployeeIds().add(record.getEmployee().getId());
                                            }

                                    );
                        },
                        () -> {
                            var mpr = MonthlyPaidReport.builder()
                                    .month(YearMonth.from(record.getDate()))
                                    .totalPaid(record.getValue())
                                    .employeeIds(new ArrayList<>())
                                    .totalEmployees(1L).build();

                            mpr.getEmployeeIds().add(record.getEmployee().getId());
                            report.getMonthlyPaidReports().add(mpr);
                        }
                );
    }

    public void addEmployeePaid(Report report, Record<BigDecimal> record, List<String> errors) {
        report.getEmployeeReports()
                .stream()
                .filter(ep -> ep.getId().equals(record.getEmployee().getId()))
                .findAny()
                .ifPresentOrElse(
                        employee ->  {
                            var totalPaid = employee.getTotalPaid().add(record.getValue());employee.setTotalPaid(totalPaid);
                        },
                        () -> errors.add("There is no any employee appropriate to this record: " + record +
                                " ~ This can cause to wrong report result")
                );
    }

    public void addYearlyEvent(Report report, Record<?> record) {
        Year year;
        if (record.getType().equals(EventType.ONBOARD) || record.getType().equals(EventType.EXIT)) {
            year = Year.from((LocalDate) record.getValue());
        } else {
            year = Year.from(record.getDate());
        }

        report.getYearlyReports()
                .stream()
                .filter(yp -> yp.getYear().compareTo(year) == 0)
                .findAny()
                .ifPresentOrElse(
                        yp -> yp.getEvents()
                                .add(new EventReport<>(record.getType(), record.getEmployee().getId(), record.getValue(), record.getDate())),
                        () -> {
                            var yp = new YearlyReport(year, new ArrayList<>());
                            yp.getEvents()
                                    .add(new EventReport<>(record.getType(), record.getEmployee().getId(), record.getValue(), record.getDate()));
                            report.getYearlyReports().add(yp);
                        }
                );
    }

}
