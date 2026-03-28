package org.dreamdev.repositories;

import org.dreamdev.models.Electorate;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ElectorateRepository extends MongoRepository<Electorate, String> {
}
