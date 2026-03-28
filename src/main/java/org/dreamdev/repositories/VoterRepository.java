package org.dreamdev.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface VoterRepository extends MongoRepository<VoterRepository, String> {
}
