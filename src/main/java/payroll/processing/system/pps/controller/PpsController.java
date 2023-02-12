package payroll.processing.system.pps.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import payroll.processing.system.pps.domain.PayrollResponse;
import payroll.processing.system.pps.service.PpsService;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PpsController {
    private final PpsService ppsService;

    @PostMapping(value = "/reports", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    public PayrollResponse saveUsers(@RequestParam(value = "files") MultipartFile[] files) {
        return ppsService.processPayroll(files);
    }
}
