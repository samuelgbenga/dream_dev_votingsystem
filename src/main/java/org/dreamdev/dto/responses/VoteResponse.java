package org.dreamdev.dto.responses;

import lombok.Builder;
import lombok.Data;
import org.dreamdev.models.VoteStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class VoteResponse {
    private String electionId;
    private String candidateId;
    private String categoryId;
    private VoteStatus voteStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}