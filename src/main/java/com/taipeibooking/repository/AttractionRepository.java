package com.taipeibooking.repository;

import com.taipeibooking.model.Attraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttractionRepository extends JpaRepository<Attraction, Long> {
    Page<Attraction> findByIsActiveTrueAndNameContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Attraction> findByIsActiveTrueAndDistrictIgnoreCaseAndNameContainingIgnoreCase(String district, String keyword, Pageable pageable);
    Page<Attraction> findByIsActiveTrueAndDistrictIgnoreCase(String district, Pageable pageable);
    Page<Attraction> findByIsActiveTrue(Pageable pageable);

    Optional<Attraction> findByIdAndIsActiveTrue(Long id);

    @Query("SELECT DISTINCT a.district FROM Attraction a WHERE a.isActive = true AND a.district IS NOT NULL AND a.district <> '' ORDER BY a.district")
    List<String> findDistinctDistrictsByIsActiveTrue();
    
    List<Attraction> findByIsActiveTrueOrderByNameAsc();
}
