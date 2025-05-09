package com.taipeibooking.repository;

import com.taipeibooking.model.Attraction;
import com.taipeibooking.model.Booking;
import com.taipeibooking.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserOrderByDateDesc(User user);
    Optional<Booking> findByIdAndUser(Long id, User user);

    Optional<Booking> findByUserAndAttractionAndDateAndStatus(User user, Attraction attraction, LocalDate date, String status);

    boolean existsByAttraction_IdAndStatusIn(Long attractionId, List<String> statuses);

    @Modifying
    @Query("DELETE FROM Booking b WHERE b.user.id = :userId")
    void deleteByUser_Id(@Param("userId") Long userId);

    @Query("SELECT b.createdAt, COUNT(b) FROM Booking b WHERE b.createdAt >= :startDate AND b.createdAt <= :endDate GROUP BY FUNCTION('DATE', b.createdAt) ORDER BY FUNCTION('DATE', b.createdAt) ASC")
    List<Object[]> findBookingTrendsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    Page<Booking> findByUser(User user, Pageable pageable);

}
