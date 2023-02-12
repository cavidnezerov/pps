package payroll.processing.system.pps.domain.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MonthlyPaidReport {
    YearMonth month;
    BigDecimal totalPaid;
    Long totalEmployees;
    List<String> employeeIds;
}
