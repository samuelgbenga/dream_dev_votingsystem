package org.dreamdev.dto.responses;

import lombok.Builder;
import lombok.Data;
import org.dreamdev.models.CitizenshipType;
import org.dreamdev.models.VoterStatus;

@Data
@Builder
public class VoterResponse {
    private String lastName;
    private String firstName;
    private String dateOfBirth;
    private CitizenshipType citizenship;
    private String voterId;
    private VoterStatus status;
}