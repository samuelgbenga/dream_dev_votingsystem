package org.dreamdev.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.models.Election;
import org.dreamdev.models.Permission;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
public class HelperClass {

    public static boolean hasPermission(List<Permission> permissions, Permission existingPermission) {
        for(Permission permission: permissions){
            if(permission.equals(existingPermission)) return true;
        }
        return false;
    }


    public static List<String[]> readCSVFiles(MultipartFile file){
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = csvReader.readAll();
            rows.remove(0);
            return rows;

        } catch (IOException | CsvException e) {
            log.error("Failed to read CSV file: {}", e.getMessage());
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage());
        }
    }

    public static boolean isElectionValid(Election election) {
        if (election == null) return false;

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (!today.equals(election.getDate())) return false;

        if (election.getStartTime() == null || election.getStopTime() == null) return false;

        return !now.isBefore(election.getStartTime()) && !now.isAfter(election.getStopTime());
    }


    public static Optional<ResponseEntity<String>> getStringResponseEntity(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("text/csv") &&
                        !contentType.equals("application/vnd.ms-excel") &&
                        !contentType.equals("text/plain"))) {
            ResponseEntity<String>  response =  ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("Only CSV files are accepted.");
            return Optional.of(response);
        }
        return Optional.empty();
    }
}
