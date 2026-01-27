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
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public PropertyResponse addProperty(PropertyRequest request, MultipartFile imageFile) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User landlord = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String imageUrl = "https://placehold.co/600x400?text=" + request.getTitle().replaceAll(" ", "+");

        Property property = Property.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .locationName(request.getLocationName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .imageUrl(imageUrl)
                .videoUrl(request.getVideoUrl())
                .listingType(request.getListingType())
                .propertyType(request.getPropertyType())
                .amenities(request.getAmenities())
                .landlord(landlord)
                .isVerified(false)
                .build();

        Property savedProperty = propertyRepository.save(property);
        return mapToResponse(savedProperty);
    }

    // NEW FEATURE: Generate Secure WhatsApp Link
    public Map<String, String> getChatLink(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        String phone = property.getLandlord().getPhoneNumber();
        // Standardize phone format (Ensure it starts with 254 if it starts with 0)
        if (phone.startsWith("0")) {
            phone = "254" + phone.substring(1);
        }

        String message = "Hi " + property.getLandlord().getFullName() +
                ", I'm interested in your property: " + property.getTitle() +
                " listed on MakaoSafe.";

        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        String whatsappUrl = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + encodedMessage;

        return Map.of("url", whatsappUrl);
    }

    public List<PropertyResponse> getMyProperties() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User landlord = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return propertyRepository.findByLandlord(landlord).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PropertyResponse> getAllProperties() {
        return propertyRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PropertyResponse getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));
        return mapToResponse(property);
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
                .description(property.getDescription())
                .price(property.getPrice())
                .locationName(property.getLocationName())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .imageUrl(property.getImageUrl())
                .videoUrl(property.getVideoUrl())
                .listingType(property.getListingType())
                .propertyType(property.getPropertyType())
                .amenities(property.getAmenities())
                .isVerified(property.isVerified())
                .landlordId(property.getLandlord().getId())
                .landlordName(property.getLandlord().getFullName())
                // REMOVED: .landlordPhone(property.getLandlord().getPhoneNumber())
                // We don't expose it here anymore for privacy
                .build();
    }
}