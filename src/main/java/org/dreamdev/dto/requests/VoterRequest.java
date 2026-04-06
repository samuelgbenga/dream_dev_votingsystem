package org.dreamdev.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.dreamdev.models.CitizenshipType;
import org.dreamdev.models.State;

@Data
public class VoterRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Date of birth is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date of birth must be in format YYYY-MM-DD")
    private String dateOfBirth;

    @NotNull(message = "State of residence is required")
    private State stateOfResidence;

    @NotNull(message = "Citizenship type is required")
    private CitizenshipType citizenship;
}