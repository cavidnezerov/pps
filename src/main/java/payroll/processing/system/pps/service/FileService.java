package payroll.processing.system.pps.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import payroll.processing.system.pps.domain.FileResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

@Service
@Slf4j
public class FileService {

    public FileResponse readFile(MultipartFile[] files) {
        var lines = new ArrayList<String []>();
        var errors = new ArrayList<String>();
        BufferedReader bufferedReader;

        for (MultipartFile file : files) {
            try {
                if (!Objects.equals(file.getContentType(), "text/csv")) {
                    throw new RuntimeException("Wrong media type: " + file.getContentType() +
                             ", file: " + file.getOriginalFilename());
                }

                bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    lines.add(line.split(","));
                }
                bufferedReader.close();
            } catch (IOException e) {
                errors.add("There is a corruption in this file: " + file.getOriginalFilename());
                log.error(e.getMessage(), e);
            } catch (RuntimeException e) {
                errors.add(e.getMessage());
                log.error(e.getMessage(), e);
            }
        }

        return new FileResponse(lines, errors);
    }
}
