package com.acme.bookmeetingroom.controllers;

import com.acme.bookmeetingroom.dtos.BookingRequest;
import com.acme.bookmeetingroom.dtos.BookingResponse;
import com.acme.bookmeetingroom.services.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Retrieve bookings for a specific room and date.
     * @param room Room name
     * @param date Date for the bookings
     * @return List of bookings or 404 if none found
     */
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getBookings(@RequestParam String room, @RequestParam LocalDate date) {
        List<BookingResponse> bookings = bookingService.getBookings(room, date);

        return ResponseEntity.ok()
                .body(bookings); // Returns 200 OK with the list of bookings
    }

    /**
     * Create a new booking.
     * @param bookingRequest Booking details
     * @return Success message or 409 in case of errors
     */
    @PostMapping
    public ResponseEntity<String> createBooking(@RequestBody @Valid BookingRequest bookingRequest) {
        bookingService.createBooking(bookingRequest);

        return ResponseEntity.ok()
                .body("Booking created successfully for room: " + bookingRequest.getRoom() +
                        " on date: " + bookingRequest.getDate() +
                        " from " + bookingRequest.getTimeFrom() +
                        " to " + bookingRequest.getTimeTo());
    }

    /**
     * Cancel a booking by ID.
     * @param id Booking ID
     * @return Success message or 404 if booking not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);

        return ResponseEntity.ok()
                .body("Booking with ID: " + id + " was successfully cancelled.");
    }
}
