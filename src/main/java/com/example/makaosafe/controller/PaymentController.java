package com.example.makaosafe.controller;

import com.example.makaosafe.dto.PaymentRequest;
import com.example.makaosafe.dto.StkPushResponse;
import com.example.makaosafe.entity.Booking;
import com.example.makaosafe.enums.BookingStatus;
import com.example.makaosafe.repository.BookingRepository;
import com.example.makaosafe.service.MpesaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final MpesaService mpesaService;
    private final BookingRepository bookingRepository;

    @PostMapping("/pay/{bookingId}")
    public ResponseEntity<StkPushResponse> triggerPayment(@PathVariable Long bookingId, @RequestBody PaymentRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        StkPushResponse response = mpesaService.initiateStkPush(
                request.getPhoneNumber(),
                booking.getTotalPrice().doubleValue(),
                "Booking-" + bookingId
        );

        booking.setCheckoutRequestId(response.getCheckoutRequestId());
        bookingRepository.save(booking);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/callback")
    public void handleCallback(@RequestBody Map<String, Object> payload) {
        try {
            Map<String, Object> body = (Map<String, Object>) payload.get("Body");
            Map<String, Object> stkCallback = (Map<String, Object>) body.get("stkCallback");

            String checkoutRequestId = (String) stkCallback.get("CheckoutRequestID");
            int resultCode = (int) stkCallback.get("ResultCode");

            Optional<Booking> bookingOpt = bookingRepository.findByCheckoutRequestId(checkoutRequestId);

            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();

                if (resultCode == 0) {
                    Map<String, Object> metadata = (Map<String, Object>) stkCallback.get("CallbackMetadata");
                    List<Map<String, Object>> items = (List<Map<String, Object>>) metadata.get("Item");

                    String receiptNumber = "";
                    for (Map<String, Object> item : items) {
                        if ("MpesaReceiptNumber".equals(item.get("Name"))) {
                            receiptNumber = (String) item.get("Value");
                            break;
                        }
                    }

                    booking.setStatus(BookingStatus.PAID);
                    booking.setMpesaReceiptNumber(receiptNumber);
                } else {
                    booking.setStatus(BookingStatus.PAYMENT_FAILED);
                }
                bookingRepository.save(booking);
            }
        } catch (Exception e) {
            log.error("Error processing M-Pesa callback", e);
        }
    }
}