package org.dreamdev.models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.List;

@Document(collection = "electorates")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@SuperBuilder
public class Electorate extends User{
    private String electorateId;

}