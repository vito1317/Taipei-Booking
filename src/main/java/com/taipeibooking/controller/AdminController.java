package com.taipeibooking.controller;

import com.taipeibooking.dto.*;
import com.taipeibooking.exception.BadRequestException;
import com.taipeibooking.exception.ResourceNotFoundException;
import com.taipeibooking.model.Booking;
import com.taipeibooking.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") 
@CrossOrigin
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/users")
    public ResponseEntity<UserAdminViewDTO> createUser(@Valid @RequestBody AdminCreateUserRequestDTO createRequest) {
        UserAdminViewDTO newUser = adminService.createUserByAdmin(createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserAdminViewDTO>> getAllUsers(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<UserAdminViewDTO> users = adminService.getAllUsersForAdmin(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserAdminViewDTO> getUserById(@PathVariable Long userId) {
        UserAdminViewDTO user = adminService.getUserByIdForAdmin(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<UserAdminViewDTO> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUpdateUserRequestDTO updateRequest) {
        UserAdminViewDTO updatedUser = adminService.updateUserByAdmin(userId, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUserByAdmin(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookings")
    public ResponseEntity<Page<Booking>> getAllBookings(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Booking> bookings = adminService.getAllBookings(pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<?> getBookingById(@PathVariable Long bookingId) {
        try {
            AdminBookingDetailDTO bookingDetails = adminService.getAdminBookingDetailsById(bookingId);
            return ResponseEntity.ok(bookingDetails);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("獲取預訂詳情時出錯 (ID: " + bookingId + "): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", true, "message", "伺服器內部錯誤，無法獲取預訂詳情"));
        }
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        adminService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/attractions")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRIP_MANAGER')") 
    public ResponseEntity<Page<AttractionAdminViewDTO>> getAllAttractions(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<AttractionAdminViewDTO> attractions = adminService.getAllAttractions(pageable);
        return ResponseEntity.ok(attractions);
    }

    @GetMapping("/attractions/{attractionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRIP_MANAGER')")
    public ResponseEntity<AttractionAdminViewDTO> getAttractionById(@PathVariable Long attractionId) {
        AttractionAdminViewDTO attraction = adminService.getAttractionById(attractionId);
        return ResponseEntity.ok(attraction);
    }

    @PostMapping("/attractions")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRIP_MANAGER')")
    public ResponseEntity<?> createAttraction(@Valid @RequestBody CreateAttractionRequestDTO createRequest) {
         try {
            AttractionAdminViewDTO newAttraction = adminService.createAttraction(createRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(newAttraction);
        } catch (BadRequestException e) {
             return ResponseEntity.badRequest().body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
             System.err.println("建立行程時發生錯誤: " + e.getMessage());
             e.printStackTrace();
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", true, "message", "建立行程時發生錯誤"));
        }
    }

    @PutMapping("/attractions/{attractionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRIP_MANAGER')")
    public ResponseEntity<?> updateAttraction(
            @PathVariable Long attractionId,
            @Valid @RequestBody UpdateAttractionRequestDTO updateRequest) {
        try {
            AttractionAdminViewDTO updatedAttraction = adminService.updateAttraction(attractionId, updateRequest);
            return ResponseEntity.ok(updatedAttraction);
        } catch (ResourceNotFoundException e) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", true, "message", e.getMessage()));
        } catch (BadRequestException e) {
             return ResponseEntity.badRequest().body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
             System.err.println("更新行程時發生錯誤 (ID: " + attractionId + "): " + e.getMessage());
             e.printStackTrace();
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", true, "message", "更新行程時發生錯誤"));
        }
    }

     @PatchMapping("/attractions/{attractionId}/status")
     @PreAuthorize("hasAnyRole('ADMIN', 'TRIP_MANAGER')")
    public ResponseEntity<?> updateAttractionStatus(
            @PathVariable Long attractionId,
            @RequestBody Map<String, Boolean> payload) {
         Boolean isActive = payload.get("isActive");
         if (isActive == null) {
             return ResponseEntity.badRequest().body(Map.of("error", true, "message", "請求中缺少 'isActive' 欄位"));
         }
         try {
             AttractionAdminViewDTO updatedAttraction = adminService.updateAttractionStatus(attractionId, isActive);
             return ResponseEntity.ok(updatedAttraction);
         } catch (ResourceNotFoundException e) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", true, "message", e.getMessage()));
         } catch (Exception e) {
             System.err.println("更新行程狀態時發生錯誤 (ID: " + attractionId + "): " + e.getMessage());
             e.printStackTrace();
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", true, "message", "更新行程狀態時發生錯誤"));
         }
    }

    @DeleteMapping("/attractions/{attractionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRIP_MANAGER')")
    public ResponseEntity<?> deleteAttraction(@PathVariable Long attractionId) {
        try {
            adminService.deleteAttraction(attractionId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
             System.err.println("刪除行程時發生錯誤 (ID: " + attractionId + "): " + e.getMessage());
             e.printStackTrace();
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", true, "message", "刪除行程時發生錯誤，可能有關聯的預訂"));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        AdminStatsDTO stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/trends/registrations")
    public ResponseEntity<List<TrendDataPointDTO>> getRegistrationTrend() {
        List<TrendDataPointDTO> trendData = adminService.getRegistrationTrend();
        return ResponseEntity.ok(trendData);
    }

    @GetMapping("/trends/bookings")
    public ResponseEntity<List<TrendDataPointDTO>> getBookingTrend() {
        List<TrendDataPointDTO> trendData = adminService.getBookingTrend();
        return ResponseEntity.ok(trendData);
    }
}
