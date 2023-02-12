package payroll.processing.system.pps.domain;

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
public class Record<T> {
    Long SequenceNo;
    Employee employee;
    EventType type;
    T value;
    LocalDate date;
    String note;
}
