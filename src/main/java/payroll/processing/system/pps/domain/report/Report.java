package payroll.processing.system.pps.domain.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Report {
    Long totalEmployees;
    List<EmployeeReport> employeeReports;
    List<MonthlyEmployeeReport> monthlyEmployeeReports;
    List<MonthlySalaryReport> monthlySalaryReports;
    List<MonthlyPaidReport> monthlyPaidReports;
    List<YearlyReport> yearlyReports;
}
