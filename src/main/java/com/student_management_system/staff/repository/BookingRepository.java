package com.student_management_system.staff.repository;

import com.student_management_system.staff.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // This is a custom query to find any bookings that overlap with a given time range for a specific facility.
    // It's the core of our conflict-checking logic.
    @Query("SELECT b FROM Booking b WHERE b.facility.id = :facilityId AND " +
            "((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findOverlappingBookings(@Param("facilityId") Long facilityId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);
}