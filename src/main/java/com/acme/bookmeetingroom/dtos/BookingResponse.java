package com.acme.bookmeetingroom.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingResponse {

    @Schema(example = "employee@example.com")
    private String employeeEmail;

    @Schema(example = "2024-11-21", description = "Booking date in the format yyyy-MM-dd")
    private LocalDate date;

    @Schema(example = "09:00", description = "Start time in the format HH:mm")
    private LocalTime timeFrom;

    @Schema(example = "10:00", description = "End time in the format HH:mm")
    private LocalTime timeTo;
}
