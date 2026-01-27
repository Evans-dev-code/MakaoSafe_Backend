package com.example.makaosafe.repository;

import com.example.makaosafe.entity.Property;
import com.example.makaosafe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByLandlord(User landlord);

    // FIXED SEARCH: Now checks Title, Location, AND Description (Case Insensitive)
    @Query("SELECT p FROM Property p WHERE " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.locationName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND p.isVerified = true")
    List<Property> searchProperties(@Param("keyword") String keyword);

    // GEOGRAPHIC SEARCH (Haversine Formula)
    @Query(value = "SELECT *, (6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) * " +
            "cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(latitude)))) " +
            "AS distance FROM properties HAVING distance < :radius " +
            "ORDER BY distance",
            nativeQuery = true)
    List<Property> findNearbyProperties(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radius
    );
}