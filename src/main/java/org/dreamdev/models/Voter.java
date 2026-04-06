package org.dreamdev.models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "voters")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Voter extends User {

    private String voterId;

    private State stateOfResidence;

    @Setter
    private VoterStatus status;

    private List<String> statePermissions = new ArrayList<>(); // for dynamic ones

}
