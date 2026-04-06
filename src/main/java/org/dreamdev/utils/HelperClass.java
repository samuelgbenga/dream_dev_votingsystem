package org.dreamdev.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.exceptions.*;
import org.dreamdev.models.Election;
import org.dreamdev.models.Permission;
import org.dreamdev.models.State;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
public class HelperClass {

    public static boolean hasPermission(List<Permission> permissions, Permission existingPermission) {
        return permissions.contains(existingPermission);
    }

    public static boolean hasStatePermission(List<String> statePermissions, State state) {
        return statePermissions.contains(Permission.forState(state));
    }


    public static List<String[]> readCSVFiles(MultipartFile file) {
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = csvReader.readAll();
            rows.remove(0); // removes header
            return rows;
        } catch (IOException | CsvException e) {
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage());
        }
    }

    public static String[] readCSVHeaders(MultipartFile file) {
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = csvReader.readNext();
            if (headers == null) throw new EmptyFileException("File is empty");
            return headers;
        } catch (IOException | CsvException e) {
            throw new RuntimeException("Failed to read CSV headers: " + e.getMessage());
        }
    }

    public static void validateCSVHeaders(MultipartFile file, List<String> expectedHeaders) {
        String[] actualHeaders = readCSVHeaders(file);
        List<String> actual = Arrays.stream(actualHeaders)
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();

        List<String> expected = expectedHeaders.stream()
                .map(String::toLowerCase)
                .toList();

        if (!actual.equals(expected)) {
            throw new InvalidFileException(
                    "Invalid CSV structure. Expected columns: " + expectedHeaders +
                            ", but got: " + Arrays.toString(actualHeaders)
            );
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

    public static String extractElectionId(String candidateId) {
        log.info("Checking if the candidate id is null or does not contain -");
        if (candidateId == null || !candidateId.contains("-")) {
            log.info("Invalid candidate ID: {}", candidateId);
            throw new InvalidIdFormat("Invalid candidateId format");
        }
        String[] parts = candidateId.split("-");
        log.info("Checking if the length is less than 3");
        if (parts.length < 3) {
            log.info("Invalid candidate ID: {}", candidateId);
            throw new InvalidIdFormat("Invalid candidateId format");
        }

        return parts[0];
    }
}
