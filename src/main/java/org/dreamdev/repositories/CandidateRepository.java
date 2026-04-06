package org.dreamdev.repositories;

import org.dreamdev.models.Candidate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends MongoRepository<Candidate, String> {
    Optional<Candidate> findByCandidateId(String s);

    List<Candidate> findByCandidateIdStartingWith(String electionId);

    boolean existsByCandidateId(String candidateId);
}
