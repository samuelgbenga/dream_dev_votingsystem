package org.dreamdev.repositories;

import org.dreamdev.models.Candidate;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CandidateRepository extends MongoRepository<Candidate, String> {
    Candidate findByCandidateId(String s);
}
