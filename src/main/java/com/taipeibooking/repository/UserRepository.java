package com.taipeibooking.repository;

import com.taipeibooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);


    @Query("SELECT FUNCTION('DATE_FORMAT', u.createdAt, '%Y-%m') as month, COUNT(u.id) as count " +
           "FROM User u " +
           "WHERE u.createdAt >= :startDate " +
           "GROUP BY month ORDER BY month ASC")
    List<Object[]> countUsersPerMonthSince(@Param("startDate") LocalDateTime startDate);


    @Query("SELECT FUNCTION('DATE_FORMAT', u.createdAt, '%Y-%m') as month, COUNT(u.id) as count " +
           "FROM User u " +
           "GROUP BY month ORDER BY month ASC")
    List<Object[]> countUsersPerMonth();


}