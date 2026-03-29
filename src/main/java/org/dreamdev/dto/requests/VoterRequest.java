package org.dreamdev.dto.requests;
import lombok.Data;

@Data
public class VoterRequest {
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String citizenship;
}