package org.dreamdev.repositories;

import jakarta.annotation.Nonnull;
import org.dreamdev.models.Vote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface VoteRepository extends MongoRepository<Vote, String> {
    boolean existsByHashedVoterIdAndElectionIdAndCategoryId(String hashedVoterId, String electionId, String categoryId);
//    List<Vote> findByCategoryIdAndElectionId(String categoryId, String electionId);
    Page<Vote> findByCategoryIdAndElectionId(String categoryId, String electionId, Pageable pageable);

}
