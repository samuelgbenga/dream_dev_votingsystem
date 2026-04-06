package org.dreamdev.dto.responses;

import lombok.Builder;
import lombok.Data;
import org.dreamdev.models.Category;

@Data
@Builder
public class CandidateVoteSummaryResponse {

    private String electionId;
    private Category category;
    private String candidateId;
    private String firstName;
    private String lastName;
    private int numberOfVote;
}