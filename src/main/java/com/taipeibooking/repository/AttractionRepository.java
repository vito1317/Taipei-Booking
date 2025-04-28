package com.taipeibooking.repository;

import com.taipeibooking.model.Attraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    List<Attraction> findByIsActiveTrueOrderByNameAsc();

    Page<Attraction> findAll(Pageable pageable);

    boolean existsByName(String name);
}
