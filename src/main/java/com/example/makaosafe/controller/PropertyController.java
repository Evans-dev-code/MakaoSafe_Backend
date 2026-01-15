package com.example.makaosafe.controller;

import com.example.makaosafe.dto.PropertyRequest;
import com.example.makaosafe.dto.PropertyResponse;
import com.example.makaosafe.service.PropertyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PropertyResponse> addProperty(
            @RequestPart("data") String propertyRequestJson,
            @RequestPart("image") MultipartFile imageFile
    ) throws IOException {
        PropertyRequest request = objectMapper.readValue(propertyRequestJson, PropertyRequest.class);
        return ResponseEntity.ok(propertyService.addProperty(request, imageFile));
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