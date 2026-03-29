package org.dreamdev.repositories;

import org.dreamdev.models.Vote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VoteRepository extends MongoRepository<Vote, String> {
    boolean existsByHashedVoterIdAndElectionIdAndCategoryId(String hashedVoterId, String electionId, String categoryId);
}
