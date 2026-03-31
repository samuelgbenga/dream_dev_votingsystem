package org.dreamdev.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.models.Election;
import org.dreamdev.models.Permission;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
}
