package org.dreamdev.services;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.dto.responses.ElectionResponse;
import org.dreamdev.exceptions.EmptyFileException;
import org.dreamdev.exceptions.NotFoundException;
import org.dreamdev.exceptions.PermissionNotFoundException;
import org.dreamdev.models.*;
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

        HelperClass.validateCSVHeaders(file, List.of(
                "electionId","category","state","date","startTime","stopTime"
        ));

        List<String[]> rows = HelperClass.readCSVFiles(file);
        List<Election> elections = getElectionList(rows);
        elections.forEach(this::saveElection);

        log.info("{} elections uploaded successfully", elections.size());
        return elections.size() + " elections uploaded successfully";
    }

    // endpoints to get all election
    public List<ElectionResponse> getAllElections() {
        return electionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ElectionResponse> getListOfElectionOfSameStateAndDate(State state, LocalDate date) {

        List<Election> elections = electionRepository.findByStateAndDate(state, date);

        return elections.stream()
                .map(election -> ElectionResponse.builder()
                        .electionId(election.getElectionId())
                        .category(election.getCategory())
                        .state(election.getState())
                        .date(election.getDate())
                        .startTime(election.getStartTime())
                        .stopTime(election.getStopTime())
                        .build()
                )
                .toList();
    }


    public List<ElectionResponse> getListOfElectionOfSameState(State state) {

        List<Election> elections = electionRepository.findByState(state);

        return elections.stream()
                .map(election -> ElectionResponse.builder()
                        .electionId(election.getElectionId())
                        .category(election.getCategory())
                        .state(election.getState())
                        .date(election.getDate())
                        .startTime(election.getStartTime())
                        .stopTime(election.getStopTime())
                        .build()
                )
                .toList();
    }




    private ElectionResponse mapToResponse(Election election) {
        return ElectionResponse.builder()
                .electionId(election.getElectionId())
                .category(election.getCategory())
                .state(election.getState())
                .date(election.getDate())
                .startTime(election.getStartTime())
                .stopTime(election.getStopTime())
                .build();
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
                    .category(Category.valueOf(row[1]))
                    .state(State.valueOf(row[2]))
                    .date(LocalDate.parse(row[3]))
                    .startTime(LocalTime.parse(row[4]))
                    .stopTime(LocalTime.parse(row[5]))
                    .createdAt(LocalDateTime.now())
                    .build();
            elections.add(election);
        }
        return elections;
    }

    private void saveElection(Election election) {
        if (electionRepository.existsByElectionId(election.getElectionId())) {
            log.warn("Election with ID {} already exists, skipping", election.getElectionId());
            return;
        }
        electionRepository.save(election);
        log.info("Saved election: {}", election.getElectionId());
    }
}