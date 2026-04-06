package org.dreamdev.models;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "votes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vote {

    @Id
    private String id;

    private String electionId;

    @Setter
    private VoteStatus voteStatus;

    private String hashedVoterId;

    private String candidateId;

    @Setter
    private String jwtToken;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // later tie voter to vote only in there state of residence\
    // using dynamic permission.
    // i voter will be assign a dynamic permission
    // called CAN_VOTE_IN_[KOGI THIS WOULE BE DYNAMIC]
    // this dynamic should be assigned upon approval by electorate
    //the dynamic is based on there state of residence.
    // However with only CAN_VOTE  permission voters should be
    // able to vote for presidents
}