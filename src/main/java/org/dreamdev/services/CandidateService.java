package org.dreamdev.services;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.models.Candidate;
import org.dreamdev.models.CitizenshipType;
import org.dreamdev.repositories.CandidateRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;

    public String uploadCandidate(MultipartFile file) {
        if (file.isEmpty()) throw new RuntimeException("File is empty");

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = csvReader.readAll();
            rows.remove(0);

            List<Candidate> candidates = getCandidateList(rows);

            candidates.forEach(this::saveCandidate);

            log.info("{} candidates uploaded successfully", candidates.size());
            return candidates.size() + " candidates uploaded successfully";

        } catch (IOException | CsvException e) {
            log.error("Failed to read CSV file: {}", e.getMessage());
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage());
        }
    }

    private List<Candidate>  getCandidateList(List<String[]> rows ){
        List<Candidate> candidates = new ArrayList<>();
        for (String[] row : rows) {
            Candidate candidate = Candidate.builder()
                    .candidateId(row[0])
                    .numberOfVote(Integer.parseInt(row[1]))
                    .categoryId(row[2])
                    .lastName(row[3])
                    .firstName(row[4])
                    .dateOfBirth(row[5])
                    .citizenship(CitizenshipType.valueOf(row[6]))
                    .build();
            candidates.add(candidate);
        }
        return candidates;
    }

    private void saveCandidate(Candidate candidate) {
        candidateRepository.save(candidate);
        log.info("Saved candidate: {}", candidate.getCandidateId());
    }
}