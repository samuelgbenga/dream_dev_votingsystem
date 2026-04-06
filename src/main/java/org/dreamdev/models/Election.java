package org.dreamdev.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Document(collection = "elections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Election {

    @Id
    private String id;

    private String electionId;

    private Category category;

    private State  state;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime stopTime;

    private LocalDateTime createdAt;

}