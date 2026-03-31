package org.dreamdev.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dreamdev.models.CitizenshipType;
import org.dreamdev.models.Permission;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElectorateRequestDto {

    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private CitizenshipType citizenship;
    private String electorateId;
}