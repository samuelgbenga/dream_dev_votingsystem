package org.dreamdev.dto.requests;
import lombok.Data;
import org.dreamdev.models.CitizenshipType;

@Data
public class VoterRequest {
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private CitizenshipType citizenship;
}