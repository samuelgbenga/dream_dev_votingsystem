package org.dreamdev.models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "candidates")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@SuperBuilder
public class Candidate extends User {

    private String candidateId;

    @Setter
    private int numberOfVote;


    private String uploadedBy;
}
