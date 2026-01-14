package com.example.makaosafe.repository;

import com.example.makaosafe.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByTenantId(Long tenantId);
    List<Booking> findByPropertyId(Long propertyId);
    Optional<Booking> findByCheckoutRequestId(String checkoutRequestId);
}