package org.dreamdev.repositories;

import org.dreamdev.models.Electorate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ElectorateRepository extends MongoRepository<Electorate, String> {
    Optional<Electorate> findByElectorateId(String electorateId);
}
