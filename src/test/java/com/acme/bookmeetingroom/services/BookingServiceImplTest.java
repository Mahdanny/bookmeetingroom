package com.acme.bookmeetingroom.services;

import com.acme.bookmeetingroom.dtos.BookingRequest;
import com.acme.bookmeetingroom.dtos.BookingResponse;
import com.acme.bookmeetingroom.entities.Booking;
import com.acme.bookmeetingroom.exceptions.BookingConflictException;
import com.acme.bookmeetingroom.exceptions.BookingNotFoundException;
import com.acme.bookmeetingroom.repositories.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingRequest bookingRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        bookingRequest = new BookingRequest("Conference Room 1", "employee@example.com", LocalDate.of(2024, 11, 26), LocalTime.of(9, 0), LocalTime.of(10, 0));
    }

    @Test
    void testGetBookings_Success() {
        List<Booking> bookings = List.of(new Booking());
        when(bookingRepository.findByRoomAndDate(anyString(), any(LocalDate.class))).thenReturn(bookings);

        List<BookingResponse> response = bookingService.getBookings("Conference Room 1", LocalDate.of(2024, 11, 26));

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void testGetBookings_NoBookingsFound() {
        when(bookingRepository.findByRoomAndDate(anyString(), any(LocalDate.class))).thenReturn(List.of());

        assertThrows(BookingNotFoundException.class, () -> bookingService.getBookings("Conference Room 1", LocalDate.of(2024, 11, 26)));
    }

    @Test
    void testCreateBooking_Success() {

        bookingRequest = new BookingRequest(
                "Conference Room 1",
                "employee@example.com",
                LocalDate.of(2099, 10, 10),  // Future date (2099)
                LocalTime.of(9, 0),
                LocalTime.of(10, 0)
        );

        when(bookingRepository.save(any(Booking.class))).thenReturn(new Booking());

        bookingService.createBooking(bookingRequest);

        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_WrongDuration() {
        // Duration less than 1 hour is invalid
        BookingRequest invalidRequest = new BookingRequest("Conference Room 1", "employee@example.com", LocalDate.of(2024, 11, 26), LocalTime.of(9, 0), LocalTime.of(9, 30));

        assertThrows(BookingConflictException.class, () -> bookingService.createBooking(invalidRequest));
    }

    @Test
    void testCreateBooking_CannotBookForPastDate() {
        BookingRequest pastBookingRequest = new BookingRequest("Conference Room 1", "employee@example.com", LocalDate.of(2020, 11, 26), LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertThrows(BookingConflictException.class, () -> bookingService.createBooking(pastBookingRequest));
    }

    @Test
    void testCreateBooking_SlotAlreadyBooked() {
        // Create a booking that already exists for the same slot
        Booking existingBooking = new Booking();
        existingBooking.setRoom("Conference Room 1");
        existingBooking.setDate(LocalDate.of(2024, 11, 26));
        existingBooking.setTimeFrom(LocalTime.of(9, 0));
        existingBooking.setTimeTo(LocalTime.of(10, 0));

        when(bookingRepository.findByRoomAndDate(anyString(), any(LocalDate.class)))
                .thenReturn(List.of(existingBooking));

        assertThrows(BookingConflictException.class, () -> bookingService.createBooking(bookingRequest));
    }

    @Test
    void testCancelBooking_CannotCancelPastBooking() {
        Booking booking = new Booking();
        booking.setDate(LocalDate.of(2020, 11, 26));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingConflictException.class, () -> bookingService.cancelBooking(1L));
    }

    @Test
    void testCancelBooking_Success() {
        Booking booking = new Booking();
        booking.setDate(LocalDate.of(2099, 11, 26));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(1L);

        verify(bookingRepository, times(1)).deleteById(1L);
    }
}
