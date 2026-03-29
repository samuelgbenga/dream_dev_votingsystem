package org.dreamdev.repositories;

import org.dreamdev.models.Voter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VoterRepository extends MongoRepository<Voter, String> {

}
