package org.dreamdev.repositories;

import org.dreamdev.models.Election;
import org.dreamdev.models.State;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ElectionRepository extends MongoRepository<Election,String> {
    Optional<Election> findByElectionId(String s);

    List<Election> findByStateAndDate(State state, LocalDate date);

    List<Election> findByState(State state);

    boolean existsByElectionId(String electionId);
}
