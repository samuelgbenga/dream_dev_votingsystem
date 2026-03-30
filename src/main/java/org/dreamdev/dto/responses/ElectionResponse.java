package org.dreamdev.dto.responses;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class ElectionResponse {

    private String electionId;
    private String electionName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime stopTime;
}