package org.dreamdev.models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "voters")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Voter extends User {

    private String voterId;

    @Setter
    private VoterStatus status;
}
