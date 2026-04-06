package org.dreamdev.dto.requests;

import lombok.Data;

@Data
public class VoteRequest {
    private String voterId;
    private String candidateId;
}