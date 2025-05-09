package com.taipeibooking.service;

import com.taipeibooking.dto.AttractionBasicDTO;
import com.taipeibooking.dto.AttractionDetailDTO;
import com.taipeibooking.exception.ResourceNotFoundException;
import com.taipeibooking.model.Attraction;
import com.taipeibooking.repository.AttractionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttractionService {

    @Autowired
    private AttractionRepository attractionRepository;

    public Page<AttractionBasicDTO> getAttractions(Optional<String> district, Optional<String> keyword, Pageable pageable) {
        Page<Attraction> attractionsPage;
        String districtVal = district.orElse(null);
        String keywordVal = keyword.map(String::toLowerCase).orElse(null);

        if (districtVal != null && !districtVal.isEmpty() && keywordVal != null && !keywordVal.isEmpty()) {
            attractionsPage = attractionRepository.findByIsActiveTrueAndDistrictIgnoreCaseAndNameContainingIgnoreCase(districtVal, keywordVal, pageable);
        } else if (districtVal != null && !districtVal.isEmpty()) {
            attractionsPage = attractionRepository.findByIsActiveTrueAndDistrictIgnoreCase(districtVal, pageable);
        } else if (keywordVal != null && !keywordVal.isEmpty()) {
            attractionsPage = attractionRepository.findByIsActiveTrueAndNameContainingIgnoreCase(keywordVal, pageable);
        } else {
            attractionsPage = attractionRepository.findByIsActiveTrue(pageable);
        }
        return attractionsPage.map(this::convertToAttractionBasicDTO);
    }

    public AttractionDetailDTO getAttractionById(Long id) {
        Attraction attraction = attractionRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到景點 ID: " + id + " 或此景點目前未開放"));
        return convertToAttractionDetailDTO(attraction);
    }

    public List<String> getDistricts() {
        return attractionRepository.findDistinctDistrictsByIsActiveTrue();
    }

    public List<AttractionBasicDTO> getActiveAttractionsForFrontend() {
        List<Attraction> attractions = attractionRepository.findByIsActiveTrueOrderByNameAsc();
        return attractions.stream()
                          .map(this::convertToAttractionBasicDTO)
                          .collect(Collectors.toList());
    }


    private AttractionBasicDTO convertToAttractionBasicDTO(Attraction attraction) {
        return new AttractionBasicDTO(
                attraction.getId(),
                attraction.getName(),
                attraction.getMrt(),
                attraction.getCategory(),
                attraction.getImageUrl(),
                attraction.getDistrict()
        );
    }

    private AttractionDetailDTO convertToAttractionDetailDTO(Attraction attraction) {
        return new AttractionDetailDTO(
                attraction.getId(),
                attraction.getName(),
                attraction.getDescription(),
                attraction.getAddress(),
                attraction.getLat(),
                attraction.getLng(),
                attraction.getTransport(),
                attraction.getMrt(),
                attraction.getCategory(),
                attraction.getDistrict(),
                attraction.getImageUrl()
        );
    }
}
