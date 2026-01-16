package com.example.makaosafe.controller;

import com.example.makaosafe.dto.BookingRequest;
import com.example.makaosafe.dto.BookingResponse;
import com.example.makaosafe.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    @GetMapping("/landlord")
    public ResponseEntity<List<BookingResponse>> getLandlordBookings() {
        return ResponseEntity.ok(bookingService.getBookingsForLandlord());
    }
}