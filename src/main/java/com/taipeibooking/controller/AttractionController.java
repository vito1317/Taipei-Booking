package com.taipeibooking.controller;

import com.taipeibooking.dto.AttractionBasicDTO;
import com.taipeibooking.dto.AttractionDetailDTO;
import com.taipeibooking.service.AttractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AttractionController {

    @Autowired
    private AttractionService attractionService;

    @GetMapping("/attractions")
    public ResponseEntity<?> getAttractions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam Optional<String> keyword,
            @RequestParam Optional<String> district) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AttractionBasicDTO> attractionsPage = attractionService.getAttractions(district, keyword, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", attractionsPage.getContent());
        response.put("nextPage", attractionsPage.hasNext() ? attractionsPage.getNumber() + 1 : null);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/attractions/{id}")
    public ResponseEntity<?> getAttractionById(@PathVariable Long id) {
        AttractionDetailDTO attractionDetail = attractionService.getAttractionById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", attractionDetail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/districts")
    public ResponseEntity<?> getDistricts() {
        List<String> districts = attractionService.getDistricts();
        Map<String, Object> response = new HashMap<>();
        response.put("data", districts);
        return ResponseEntity.ok(response);
    }
}
