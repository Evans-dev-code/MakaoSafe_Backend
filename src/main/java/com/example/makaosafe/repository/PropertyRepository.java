package com.example.makaosafe.repository;

import com.example.makaosafe.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByLandlordId(Long landlordId);
    @Query("SELECT p FROM Property p WHERE " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.locationName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND p.isVerified = true")
    List<Property> searchProperties(@Param("keyword") String keyword);

    @Query(value = "SELECT * FROM properties p WHERE " +
            "(6371 * acos(cos(radians(:userLat)) * cos(radians(p.latitude)) * " +
            "cos(radians(p.longitude) - radians(:userLng)) + " +
            "sin(radians(:userLat)) * sin(radians(p.latitude)))) < :radius " +
            "AND p.is_verified = true",
            nativeQuery = true)
    List<Property> findNearbyProperties(
            @Param("userLat") double userLat,
            @Param("userLng") double userLng,
            @Param("radius") double radius
    );
}