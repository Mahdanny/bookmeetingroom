package com.acme.bookmeetingroom.controllers;

import com.acme.bookmeetingroom.dtos.BookingRequest;
import com.acme.bookmeetingroom.dtos.BookingResponse;
import com.acme.bookmeetingroom.services.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
    }

    @Test
    void testGetBookings_Success() throws Exception {

        String room = "Conference Room 1";
        LocalDate date = LocalDate.of(2024, 11, 21);
        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setEmployeeEmail("employee@example.com");
        bookingResponse.setTimeFrom(LocalTime.of(9, 0));
        bookingResponse.setTimeTo(LocalTime.of(10, 0));
        bookingResponse.setDate(date);
        List<BookingResponse> bookings = List.of(bookingResponse);

        when(bookingService.getBookings(room, date)).thenReturn(bookings);

        mockMvc.perform(get("/api/bookings")
                        .param("room", room)
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeEmail").value("employee@example.com"))
                .andExpect(jsonPath("$[0].date[0]").value(2024))
                .andExpect(jsonPath("$[0].date[1]").value(11))
                .andExpect(jsonPath("$[0].date[2]").value(21))
                .andExpect(jsonPath("$[0].timeFrom[0]").value(9))
                .andExpect(jsonPath("$[0].timeFrom[1]").value(0))
                .andExpect(jsonPath("$[0].timeTo[0]").value(10))
                .andExpect(jsonPath("$[0].timeTo[1]").value(0));
    }

    @Test
    void testCreateBooking_Success() throws Exception {
        // Given
        BookingRequest bookingRequest = new BookingRequest("Conference Room 1", "employee@example.com", LocalDate.of(2024, 11, 21), LocalTime.of(9, 0), LocalTime.of(10, 0));

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"room\": \"Conference Room 1\", \"employeeEmail\": \"employee@example.com\", \"date\": \"2024-11-21\", \"timeFrom\": \"09:00\", \"timeTo\": \"10:00\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Booking created successfully for room: Conference Room 1 on date: 2024-11-21 from 09:00 to 10:00"));

        verify(bookingService, times(1)).createBooking(any(BookingRequest.class));
    }

    @Test
    void testCancelBooking_Success() throws Exception {
        Long bookingId = 1L;

        mockMvc.perform(delete("/api/bookings/{id}", bookingId))
                .andExpect(status().isOk())
                .andExpect(content().string("Booking with ID: 1 was successfully cancelled."));

        verify(bookingService, times(1)).cancelBooking(bookingId);
    }
}
