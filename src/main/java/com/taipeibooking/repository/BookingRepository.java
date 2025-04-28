package com.taipeibooking.repository;

import com.taipeibooking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);


    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);


    List<Booking> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);



    @Query("SELECT FUNCTION('DATE_FORMAT', b.createdAt, '%Y-%m') as month, COUNT(b.id) as count " +
           "FROM Booking b " +
           "WHERE b.createdAt >= :startDate " +
           "GROUP BY month ORDER BY month ASC")
    List<Object[]> countBookingsPerMonthSince(@Param("startDate") LocalDateTime startDate);


     @Query("SELECT FUNCTION('DATE_FORMAT', b.createdAt, '%Y-%m') as month, COUNT(b.id) as count " +
           "FROM Booking b " +
           "GROUP BY month ORDER BY month ASC")
    List<Object[]> countBookingsPerMonth();

}