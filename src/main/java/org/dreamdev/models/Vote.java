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

    private String jwtToken;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}