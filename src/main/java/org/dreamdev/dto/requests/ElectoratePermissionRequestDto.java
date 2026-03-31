package org.dreamdev.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dreamdev.models.CitizenshipType;
import org.dreamdev.models.Permission;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElectoratePermissionRequestDto {
    private String electorateId;
    private Permission permission;
}