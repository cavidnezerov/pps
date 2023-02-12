package payroll.processing.system.pps.domain.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import payroll.processing.system.pps.domain.enumaration.EventType;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventReport<T> {
    EventType type;
    String employeeId;
    T value;
    LocalDate date;
}
