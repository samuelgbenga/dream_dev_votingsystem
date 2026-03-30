package org.dreamdev.repositories;

import org.dreamdev.models.Election;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ElectionRepository extends MongoRepository<Election,String> {
    Optional<Election> findByElectionId(String s);
}
