package com.acme.bookmeetingroom.services;

import com.acme.bookmeetingroom.dtos.BookingRequest;
import com.acme.bookmeetingroom.dtos.BookingResponse;
import com.acme.bookmeetingroom.entities.Booking;
import com.acme.bookmeetingroom.exceptions.BookingConflictException;
import com.acme.bookmeetingroom.exceptions.BookingNotFoundException;
import com.acme.bookmeetingroom.repositories.BookingRepository;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    public BookingServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<BookingResponse> getBookings(String room, LocalDate date) {
        List<Booking> bookings = bookingRepository.findByRoomAndDate(room, date);
        log.info("Fetching bookings for room '{}' on date '{}'", room, date);

        if (bookings.isEmpty()) {
            log.warn("No bookings found for room '{}' on date '{}'", room, date);
            throw new BookingNotFoundException(
                    "No bookings found for room: " + room + " on date: " + date
            );
        }
        log.info("Found {} bookings for room '{}' on date '{}'", bookings.size(), room, date);
        return bookings.stream()
                .map(booking -> {
                    BookingResponse response = new BookingResponse();
                    response.setEmployeeEmail(booking.getEmployeeEmail());
                    response.setTimeFrom(booking.getTimeFrom());
                    response.setTimeTo(booking.getTimeTo());
                    response.setDate(booking.getDate());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void createBooking(BookingRequest bookingRequest) {

        log.info("Creating booking for room '{}' by employee '{}' on date '{}' from {} to {}",
                bookingRequest.getRoom(),
                bookingRequest.getEmployeeEmail(),
                bookingRequest.getDate(),
                bookingRequest.getTimeFrom(),
                bookingRequest.getTimeTo()
        );

        long hours = java.time.Duration.between(bookingRequest.getTimeFrom(), bookingRequest.getTimeTo()).toHours();

        if (hours < 1 || hours % 1 != 0) {
            log.error("Invalid booking duration: {} hours", hours);
            throw new BookingConflictException("Booking duration must be at least 1 hour or a multiple of 1 hour.");
        }

        if (bookingRequest.getDate().isBefore(LocalDate.now())) {
            log.error("Attempt to book a past date: {}", bookingRequest.getDate());
            throw new BookingConflictException("Cannot book for past dates.");
        }

        List<Booking> conflictingBookings = bookingRepository.findByRoomAndDate(
                        bookingRequest.getRoom(),
                        bookingRequest.getDate()
                ).stream()
                .filter(existingBooking -> isTimeOverlap(
                        existingBooking.getTimeFrom(),
                        existingBooking.getTimeTo(),
                        bookingRequest.getTimeFrom(),
                        bookingRequest.getTimeTo()
                ))
                .collect(Collectors.toList());

        if (!conflictingBookings.isEmpty()) {
            log.error("Booking conflict detected for room '{}' on date '{}' from {} to {}",
                    bookingRequest.getRoom(),
                    bookingRequest.getDate(),
                    bookingRequest.getTimeFrom(),
                    bookingRequest.getTimeTo()
            );
            String conflictDetails = conflictingBookings.stream()
                    .map(booking -> "Existing booking from " + booking.getTimeFrom() + " to " + booking.getTimeTo())
                    .collect(Collectors.joining("; "));
            throw new BookingConflictException("Slot already booked for the given time. Conflicts: " + conflictDetails);
        }

        Booking booking = new Booking();
        booking.setRoom(bookingRequest.getRoom());
        booking.setEmployeeEmail(bookingRequest.getEmployeeEmail());
        booking.setDate(bookingRequest.getDate());
        booking.setTimeFrom(bookingRequest.getTimeFrom());
        booking.setTimeTo(bookingRequest.getTimeTo());
        bookingRepository.save(booking);

        log.info("Booking successfully created with id '{}' for room '{}' on date '{}' from {} to {}",
                booking.getId(),
                bookingRequest.getRoom(),
                bookingRequest.getDate(),
                bookingRequest.getTimeFrom(),
                bookingRequest.getTimeTo()
        );
    }

    @Override
    public void cancelBooking(Long id) {

        log.info("Attempting to cancel booking with id '{}'", id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Booking not found with id '{}'", id);
                    return new RuntimeException("Booking not found.");
                });

        if (booking.getDate().isBefore(LocalDate.now())) {
            log.error("Attempt to cancel a past booking with id '{}'", id);
            throw new BookingConflictException("Cannot cancel past bookings.");
        }

        bookingRepository.deleteById(id);
        log.info("Booking with id '{}' successfully cancelled", id);
    }


    private boolean isTimeOverlap(LocalTime existingFrom, LocalTime existingTo, LocalTime newFrom, LocalTime newTo) {
        return (newFrom.isBefore(existingTo) && newTo.isAfter(existingFrom));
    }
}
