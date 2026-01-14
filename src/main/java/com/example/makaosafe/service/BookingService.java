package com.example.makaosafe.service;

import com.example.makaosafe.dto.BookingRequest;
import com.example.makaosafe.dto.BookingResponse;
import com.example.makaosafe.entity.Booking;
import com.example.makaosafe.entity.Property;
import com.example.makaosafe.entity.User;
import com.example.makaosafe.enums.BookingStatus;
import com.example.makaosafe.enums.ListingType;
import com.example.makaosafe.repository.BookingRepository;
import com.example.makaosafe.repository.PropertyRepository;
import com.example.makaosafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public BookingResponse createBooking(BookingRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User tenant = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new RuntimeException("Property not found"));

        BigDecimal totalPrice;

        if (property.getListingType() == ListingType.BNB) {
            long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
            if (days < 1) days = 1;
            totalPrice = property.getPrice().multiply(BigDecimal.valueOf(days));
        } else {
            totalPrice = property.getPrice();
        }

        Booking booking = Booking.builder()
                .property(property)
                .tenant(tenant)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalPrice(totalPrice)
                .status(BookingStatus.PENDING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    public List<BookingResponse> getMyBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User tenant = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return bookingRepository.findByTenantId(tenant.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .propertyTitle(booking.getProperty().getTitle())
                .locationName(booking.getProperty().getLocationName())
                .price(booking.getProperty().getPrice())
                .totalPrice(booking.getTotalPrice())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .status(booking.getStatus())
                .tenantId(booking.getTenant().getId())
                .build();
    }
}