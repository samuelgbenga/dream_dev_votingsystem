package org.dreamdev.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface VoteRepository extends MongoRepository<VoteRepository, String> {
}
