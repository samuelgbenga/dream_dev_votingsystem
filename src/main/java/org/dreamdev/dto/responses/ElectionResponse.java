package org.dreamdev.dto.responses;

import lombok.Builder;
import lombok.Data;
import org.dreamdev.models.Category;
import org.dreamdev.models.State;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class ElectionResponse {

    private String electionId;
    private Category category;
    private State state;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime stopTime;
}