package org.dreamdev.services;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.dto.responses.CandidateResponse;
import org.dreamdev.exceptions.EmptyFileException;
import org.dreamdev.exceptions.NotFoundException;
import org.dreamdev.exceptions.PermissionNotFoundException;
import org.dreamdev.models.Candidate;
import org.dreamdev.models.CitizenshipType;
import org.dreamdev.models.Electorate;
import org.dreamdev.models.Permission;
import org.dreamdev.repositories.CandidateRepository;
import org.dreamdev.repositories.ElectionRepository;
import org.dreamdev.repositories.ElectorateRepository;
import org.dreamdev.utils.HelperClass;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;

    private final ElectorateRepository electorateRepository;

    public String uploadCandidate(MultipartFile file, String electorateId) {

        validate(electorateId);
        if (file.isEmpty()) throw new EmptyFileException("File is empty");
        HelperClass.validateCSVHeaders(file, List.of(
                "candidateId", "lastName", "firstName", "dateOfBirth", "citizenship"
        ));
        List<Candidate> candidates = getCandidateList(HelperClass.readCSVFiles(file));
        candidates.forEach(this::saveCandidate);

        log.info("{} candidates uploaded successfully", candidates.size());
        return candidates.size() + " candidates uploaded successfully";

    }

    public List<CandidateResponse> getAllCandidates() {
        return candidateRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<CandidateResponse> getAllCandidatesByElectionId(String electionId) {
        return candidateRepository.findByCandidateIdStartingWith(electionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CandidateResponse mapToResponse(Candidate candidate) {
        return CandidateResponse.builder()
                .lastName(candidate.getLastName())
                .firstName(candidate.getFirstName())
                .dateOfBirth(candidate.getDateOfBirth())
                .citizenship(candidate.getCitizenship())
                .candidateId(candidate.getCandidateId())
                .numberOfVote(candidate.getNumberOfVote())
                .build();
    }

    private void validate(String electorateId) {
        Optional<Electorate> electorate = electorateRepository.findByElectorateId(electorateId);

        if(electorate.isEmpty()) throw new NotFoundException("Electorate with this Id not found");

        if(!HelperClass.hasPermission(electorate.get().getPermissions(), Permission.CAN_UPLOAD_FILE)) {
            throw new PermissionNotFoundException("Electorate does not have permission to vote" );
        }
    }

    private List<Candidate>  getCandidateList(List<String[]> rows ){
        List<Candidate> candidates = new ArrayList<>();
        for (String[] row : rows) {
            Candidate candidate = Candidate.builder()
                    .candidateId(row[0])
                    .lastName(row[1])
                    .firstName(row[2])
                    .dateOfBirth(row[3])
                    .citizenship(CitizenshipType.valueOf(row[4]))
                    .build();
            candidates.add(candidate);
        }
        return candidates;
    }

    private void saveCandidate(Candidate candidate) {
        if (candidateRepository.existsByCandidateId(candidate.getCandidateId())) {
            log.warn("Candidate with ID {} already exists, skipping", candidate.getCandidateId());
            return;
        }
        candidateRepository.save(candidate);
        log.info("Saved candidate: {}", candidate.getCandidateId());
    }
}