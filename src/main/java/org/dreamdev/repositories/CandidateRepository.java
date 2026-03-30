package org.dreamdev.repositories;

import org.dreamdev.models.Candidate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CandidateRepository extends MongoRepository<Candidate, String> {
    Optional<Candidate> findByCandidateId(String s);
}
