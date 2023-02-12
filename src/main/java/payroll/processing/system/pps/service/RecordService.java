package payroll.processing.system.pps.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import payroll.processing.system.pps.domain.Record;
import payroll.processing.system.pps.domain.RecordResponse;
import payroll.processing.system.pps.domain.enumaration.EventType;

import java.util.ArrayList;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordService {
    private final FileService fileService;
    private final BuilderService builderService;

    public RecordResponse getRecords(MultipartFile[] files) {
        var records = new ArrayList<Record<?>>();
        var errors = new ArrayList<String>();

        var fileResponse = fileService.readFile(files);

        for (String [] line : fileResponse.getLines()) {
            try {
                line[line.length - 4] = line[line.length - 4].trim();
                if (EventType.ONBOARD.name().equals(line[line.length - 4])) {
                    records.add(builderService.buildOnboardRecord(line));
                } else if (EventType.EXIT.name().equals(line[line.length - 4])) {
                    records.add(builderService.buildExitRecord(line));
                } else {
                    records.add(builderService.buildPaidRecord(line));
                }
            } catch (Exception e) {
                errors.add("There is a error in this line: " + Arrays.deepToString(line));
                log.error("There is a error in this line: " + Arrays.deepToString(line));
                log.error(e.getMessage(), e);
            }
        }

        return RecordResponse.builder()
                .records(records)
                .fileErrors(fileResponse.getErrors())
                .recordErrors(errors).build();
    }
}
