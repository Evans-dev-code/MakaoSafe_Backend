package com.example.makaosafe.controller;

import com.example.makaosafe.dto.PropertyRequest;
import com.example.makaosafe.dto.PropertyResponse;
import com.example.makaosafe.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    public ResponseEntity<PropertyResponse> addProperty(@RequestBody PropertyRequest request) {
        return ResponseEntity.ok(propertyService.addProperty(request));
    }

    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getAllProperties() {
        return ResponseEntity.ok(propertyService.getAllProperties());
    }

    @GetMapping("/search")
    public ResponseEntity<List<PropertyResponse>> searchProperties(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(propertyService.searchProperties(keyword));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<PropertyResponse>> findNearby(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng,
            @RequestParam("radius") double radius
    ) {
        return ResponseEntity.ok(propertyService.findNearby(lat, lng, radius));
    }
}