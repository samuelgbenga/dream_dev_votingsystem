package org.dreamdev.dto.responses;

import lombok.Builder;
import lombok.Data;
import org.dreamdev.models.CitizenshipType;

@Data
@Builder
public class ElectorateResponse {

    private String lastName;
    private String firstName;
    private String dateOfBirth;
    private CitizenshipType citizenship;
    private String electorateId;
}