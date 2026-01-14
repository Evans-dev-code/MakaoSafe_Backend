package com.example.makaosafe.dto;

import com.example.makaosafe.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    private Long id;
    private String propertyTitle;
    private String locationName;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private LocalDate startDate;
    private LocalDate endDate;
    private BookingStatus status;
    private Long tenantId;
}