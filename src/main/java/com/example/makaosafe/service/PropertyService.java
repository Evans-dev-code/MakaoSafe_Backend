package com.example.makaosafe.service;

import com.example.makaosafe.dto.PropertyRequest;
import com.example.makaosafe.dto.PropertyResponse;
import com.example.makaosafe.entity.Property;
import com.example.makaosafe.entity.User;
import com.example.makaosafe.repository.PropertyRepository;
import com.example.makaosafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public PropertyResponse addProperty(PropertyRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User landlord = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Property property = Property.builder()
                .title(request.getTitle())
                .price(request.getPrice())
                .locationName(request.getLocationName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .imageUrl(request.getImageUrl())
                .videoUrl(request.getVideoUrl())
                .listingType(request.getListingType())
                .propertyType(request.getPropertyType())
                .landlord(landlord)
                .isVerified(false)
                .build();

        Property savedProperty = propertyRepository.save(property);

        return mapToResponse(savedProperty);
    }

    public List<PropertyResponse> getAllProperties() {
        return propertyRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PropertyResponse> searchProperties(String keyword) {
        return propertyRepository.searchProperties(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PropertyResponse> findNearby(double lat, double lng, double radius) {
        return propertyRepository.findNearbyProperties(lat, lng, radius).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PropertyResponse mapToResponse(Property property) {
        return PropertyResponse.builder()
                .id(property.getId())
                .title(property.getTitle())
                .price(property.getPrice())
                .locationName(property.getLocationName())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .imageUrl(property.getImageUrl())
                .videoUrl(property.getVideoUrl())
                .listingType(property.getListingType())
                .propertyType(property.getPropertyType())
                .isVerified(property.isVerified())
                .landlordId(property.getLandlord().getId())
                .landlordName(property.getLandlord().getFullName())
                .landlordPhone(property.getLandlord().getPhoneNumber())
                .build();
    }
}