package org.dreamdev.models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class User {

    @Id
    private String id;

    private String lastName;
    private String firstName;
    private String dateOfBirth;
    private String citizenship;
    private String createdAt;
    private List<Permission> permissions;
}
