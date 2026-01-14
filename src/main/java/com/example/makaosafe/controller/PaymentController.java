package com.example.makaosafe.controller;

import com.example.makaosafe.dto.PaymentRequest;
import com.example.makaosafe.dto.StkPushResponse;
import com.example.makaosafe.entity.Booking;
import com.example.makaosafe.repository.BookingRepository;
import com.example.makaosafe.service.MpesaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final MpesaService mpesaService;
    private final BookingRepository bookingRepository;

    @PostMapping("/pay/{bookingId}")
    public ResponseEntity<StkPushResponse> triggerPayment(
            @PathVariable Long bookingId,
            @RequestBody PaymentRequest request) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        StkPushResponse response = mpesaService.initiateStkPush(
                request.getPhoneNumber(),
                booking.getTotalPrice().doubleValue(),
                "Booking-" + bookingId
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/callback")
    public void handleCallback(@RequestBody Map<String, Object> payload) {
        log.info("M-Pesa Callback received: {}", payload);
    }
}