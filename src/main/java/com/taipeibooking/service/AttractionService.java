package com.taipeibooking.service;

import com.taipeibooking.dto.AttractionBasicDTO;
import com.taipeibooking.model.Attraction;
import com.taipeibooking.repository.AttractionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttractionService {

    @Autowired
    private AttractionRepository attractionRepository;

    @Transactional(readOnly = true)
    public List<AttractionBasicDTO> getActiveAttractionsForFrontend() {
        return attractionRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(this::convertToAttractionBasicDTO)
                .collect(Collectors.toList());
    }

    private AttractionBasicDTO convertToAttractionBasicDTO(Attraction attraction) {
        if (attraction == null) return null;
        return new AttractionBasicDTO(
                attraction.getId(),
                attraction.getName(),
                attraction.getImageUrl()
        );
    }
}
