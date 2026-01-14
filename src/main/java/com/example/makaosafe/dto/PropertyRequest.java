package com.example.makaosafe.dto;

import com.example.makaosafe.enums.ListingType;
import com.example.makaosafe.enums.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PropertyRequest {
    private String title;
    private BigDecimal price;
    private String locationName;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private String videoUrl;
    private ListingType listingType;
    private PropertyType propertyType;
}