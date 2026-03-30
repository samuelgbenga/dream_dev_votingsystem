package org.dreamdev.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.models.Permission;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
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
}
