package org.dreamdev.dto.responses;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import org.dreamdev.models.CitizenshipType;

@Data
@Builder
public class CandidateResponse {
    private String lastName;
    private String firstName;
    private String dateOfBirth;
    private CitizenshipType citizenship;
    private String candidateId;
    private int numberOfVote;
}