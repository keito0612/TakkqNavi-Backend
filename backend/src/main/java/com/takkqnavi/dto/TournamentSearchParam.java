package com.takkqnavi.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TournamentSearchParam {
    @Pattern(regexp = "^(startDate|entryDeadline)$", message = "sortBy must be 'startDate' or 'entryDeadline'")
    private String sortBy = "startDate";

    @Pattern(regexp = "^(asc|desc)$", message = "order must be 'asc' or 'desc'")
    private String order = "asc";

    private LocalDate from;
    private LocalDate to;
    private String keyword;
}
