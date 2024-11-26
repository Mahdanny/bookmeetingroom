package com.acme.bookmeetingroom.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class BookingRequest {

    @NotBlank
    @Schema(example = "Conference Room 1")
    private String room;

    @Email
    @NotBlank
    @Schema(example = "employee@example.com")
    private String employeeEmail;

    @NotNull
    @Schema(example = "2024-11-21", description = "Booking date in the format yyyy-MM-dd")
    private LocalDate date;

    @NotNull
    @Schema(example = "09:00", description = "Start time in the format HH:mm")
    private LocalTime timeFrom;

    @NotNull
    @Schema(example = "10:00", description = "End time in the format HH:mm")
    private LocalTime timeTo;

}
