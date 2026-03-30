package org.dreamdev.services;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.exceptions.EmptyFileException;
import org.dreamdev.exceptions.NotFoundException;
import org.dreamdev.exceptions.PermissionNotFoundException;
import org.dreamdev.models.Electorate;
import org.dreamdev.models.Election;
import org.dreamdev.models.Permission;
import org.dreamdev.repositories.ElectionRepository;
import org.dreamdev.repositories.ElectorateRepository;
import org.dreamdev.utils.HelperClass;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ElectionService {

    private final ElectionRepository electionRepository;
    private final ElectorateRepository electorateRepository;

    public String uploadElections(MultipartFile file, String electorateId) {

        validateElectorate(electorateId);

        if (file.isEmpty()) throw new EmptyFileException("File is empty");

        List<String[]> rows = HelperClass.readCSVFiles(file);
        List<Election> elections = getElectionList(rows);
        elections.forEach(this::saveElection);

        log.info("{} elections uploaded successfully", elections.size());
        return elections.size() + " elections uploaded successfully";
    }

    // Ensure the electorate exists and has permission to approve/upload
    private void validateElectorate(String electorateId) {
        Optional<Electorate> electorate = electorateRepository.findByElectorateId(electorateId);

        if (electorate.isEmpty()) throw new NotFoundException("Electorate with this Id not found");

        if (!HelperClass.hasPermission(electorate.get().getPermissions(), Permission.CAN_UPLOAD_FILE)) {
            throw new PermissionNotFoundException("Electorate does not have permission to approve elections");
        }
    }

    private List<Election> getElectionList(List<String[]> rows) {
        List<Election> elections = new ArrayList<>();
        for (String[] row : rows) {
            Election election = Election.builder()
                    .electionId(row[0])
                    .electionName(row[1])
                    .date(LocalDate.parse(row[2]))
                    .startTime(LocalTime.parse(row[3]))
                    .stopTime(LocalTime.parse(row[4]))
                    .createdAt(LocalDateTime.now())
                    .build();
            elections.add(election);
        }
        return elections;
    }

    private void saveElection(Election election) {
        electionRepository.save(election);
        log.info("Saved election: {}", election.getElectionId());
    }
}