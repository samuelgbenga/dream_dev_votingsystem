package org.dreamdev.dto.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CandidateVoteSummaryResponse {

    private String electionId;
    private String categoryId;
    private String categoryType;
    private String candidateId;
    private String firstName;
    private String lastName;
    private int numberOfVote;
}