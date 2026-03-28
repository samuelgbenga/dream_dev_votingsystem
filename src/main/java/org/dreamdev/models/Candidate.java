package org.dreamdev.models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "candidates")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Candidate extends User {

    private int numberOfVote;

    private String categoryId;

    private String uploadedBy;
}
