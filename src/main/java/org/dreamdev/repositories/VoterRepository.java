package org.dreamdev.repositories;

import org.dreamdev.models.Voter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface VoterRepository extends MongoRepository<Voter, String> {

    boolean existsByVoterId(String voterId);

    Optional<Voter> findByVoterId(String voterId);
}
