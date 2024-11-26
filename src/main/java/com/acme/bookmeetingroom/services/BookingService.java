package com.acme.bookmeetingroom.services;

import com.acme.bookmeetingroom.dtos.BookingRequest;
import com.acme.bookmeetingroom.dtos.BookingResponse;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    List<BookingResponse> getBookings(String room, LocalDate date);
    void createBooking(BookingRequest bookingRequest);
    void cancelBooking(Long id);
}
