package com.taipeibooking.controller;

import com.taipeibooking.dto.*;
import com.taipeibooking.exception.BadRequestException;
import com.taipeibooking.exception.IllegalBookingStateException;
import com.taipeibooking.exception.ResourceNotFoundException;
import com.taipeibooking.service.AdminService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody AdminCreateUserRequestDTO createRequest) {
        try {
            logger.info("Received request to create user: {}", createRequest.getEmail());
            UserAdminViewDTO newUser = adminService.createUser(createRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (BadRequestException e) {
            logger.warn("Bad request while creating user {}: {}", createRequest.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", true, "message", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error creating user {}: {}", createRequest.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", true, "message", "創建用戶時發生內部錯誤：" + e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserAdminViewDTO>> getAllUsers(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        logger.info("Received request to get all users with pageable: {}", pageable);
        Page<UserAdminViewDTO> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            logger.info("Received request to get user by ID: {}", userId);
            UserAdminViewDTO user = adminService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (ResourceNotFoundException e) {
            logger.warn("User not found with ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUpdateUserRequestDTO updateRequest) {
        try {
            logger.info("Received request to update user ID: {}", userId);
            UserAdminViewDTO updatedUser = adminService.updateUser(userId, updateRequest);
            return ResponseEntity.ok(updatedUser);
        } catch (ResourceNotFoundException e) {
            logger.warn("User not found for update with ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", true, "message", e.getMessage()));
        } catch (BadRequestException e) {
            logger.warn("Bad request while updating user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", true, "message", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error updating user ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", true, "message", "更新用戶時發生內部錯誤：" + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            logger.info("Received request to delete user ID: {}", userId);
            adminService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("User not found for deletion with ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deleting user ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", true, "message", "刪除用戶時發生錯誤：" + e.getMessage()));
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<Page<BookingAdminViewDTO>> getAllBookings(
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        logger.info("Received request to get all bookings with pageable: {}", pageable);
        Page<BookingAdminViewDTO> bookings = adminService.getAllBookings(pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<?> getBookingById(@PathVariable Long bookingId) {
        try {
            logger.info("Received request to get booking by ID: {}", bookingId);
            AdminBookingDetailDTO bookingDetails = adminService.getBookingDetails(bookingId);
            return ResponseEntity.ok(bookingDetails);
        } catch (ResourceNotFoundException e) {
            logger.warn("Booking not found with ID {}: {}", bookingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting booking details for ID {}: {}", bookingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", true, "message", "伺服器內部錯誤，無法獲取預訂詳情"));
        }
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        try {
            logger.info("Received request to cancel booking ID: {}", bookingId);
            adminService.cancelBookingByAdmin(bookingId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Booking not found for cancellation with ID {}: {}", bookingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", true, "message", e.getMessage()));
        } catch (IllegalBookingStateException e) {
            logger.warn("Illegal state for cancelling booking ID {}: {}", bookingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error cancelling booking ID {}: {}", bookingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", true, "message", "取消預訂時發生錯誤: " + e.getMessage()));
        }
    }

    @GetMapping("/attractions")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRIP_MANAGER')")
    public ResponseEntity<Page<AttractionAdminViewDTO>> getAllAttractions(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        logger.info("Received request to get all attractions with pageable: {}", pageable);
        Page<AttractionAdminViewDTO> attractions = adminService.getAllAttractions(pageable);
        return ResponseEntity.ok(attractions);
    }

    @GetMapping("/attractions/{attractionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRIP_MANAGER')")
    public ResponseEntity<?> getAttractionById(@PathVariable Long attractionId) {
        try {
            logger.info("Received request to get attraction by ID: {}", attractionId);
            AttractionAdminViewDTO attraction = adminService.getAttractionAdminViewById(attractionId);
            return ResponseEntity.ok(attraction);
        } catch (ResourceNotFoundException e) {
            logger.warn("Attraction not found with ID {}: {}", attractionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    @PostMapping("/attractions")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRIP_MANAGER')")
    public ResponseEntity<?> createAttraction(@Valid @RequestBody CreateAttractionRequestDTO createRequest) {
         try {
            logger.info("Received request to create attraction: {}", createRequest.getName());
            AttractionAdminViewDTO newAttraction = adminService.createAttraction(createRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(newAttraction);
        } catch (BadRequestException e) {
             logger.warn("Bad request while creating attraction {}: {}", createRequest.getName(), e.getMessage());
             return ResponseEntity.badRequest().body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
             logger.error("Error creating attraction {}: {}", createRequest.getName(), e.getMessage(), e);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", true, "message", "建立行程時發生錯誤: " + e.getMessage()));
        }
    }

    @PutMapping("/attractions/{attractionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRIP_MANAGER')")
    public ResponseEntity<?> updateAttraction(
            @PathVariable Long attractionId,
            @Valid @RequestBody UpdateAttractionRequestDTO updateRequest) {
        try {
            logger.info("Received request to update attraction ID: {}", attractionId);
            AttractionAdminViewDTO updatedAttraction = adminService.updateAttraction(attractionId, updateRequest);
            return ResponseEntity.ok(updatedAttraction);
        } catch (ResourceNotFoundException e) {
             logger.warn("Attraction not found for update with ID {}: {}", attractionId, e.getMessage());
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", true, "message", e.getMessage()));
        } catch (BadRequestException e) {
             logger.warn("Bad request while updating attraction ID {}: {}", attractionId, e.getMessage());
             return ResponseEntity.badRequest().body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
             logger.error("Error updating attraction ID {}: {}", attractionId, e.getMessage(), e);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", true, "message", "更新行程時發生錯誤: " + e.getMessage()));
        }
    }

     @PatchMapping("/attractions/{attractionId}/status")
     @PreAuthorize("hasAnyRole('ADMIN', 'TRIP_MANAGER')")
    public ResponseEntity<?> updateAttractionStatus(
            @PathVariable Long attractionId,
            @RequestBody Map<String, Boolean> payload) {
         Boolean isActive = payload.get("isActive");
         if (isActive == null) {
             logger.warn("Missing 'isActive' field in payload for updating status of attraction ID: {}", attractionId);
             return ResponseEntity.badRequest().body(Map.of("error", true, "message", "請求中缺少 'isActive' 欄位"));
         }
         try {
             logger.info("Received request to update status for attraction ID: {} to isActive: {}", attractionId, isActive);
             AttractionAdminViewDTO updatedAttraction = adminService.updateAttractionStatus(attractionId, isActive);
             return ResponseEntity.ok(updatedAttraction);
         } catch (ResourceNotFoundException e) {
             logger.warn("Attraction not found for status update with ID {}: {}", attractionId, e.getMessage());
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", true, "message", e.getMessage()));
         } catch (Exception e) {
             logger.error("Error updating status for attraction ID {}: {}", attractionId, e.getMessage(), e);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", true, "message", "更新行程狀態時發生錯誤: " + e.getMessage()));
         }
    }

    @DeleteMapping("/attractions/{attractionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRIP_MANAGER')")
    public ResponseEntity<?> deleteAttraction(@PathVariable Long attractionId) {
        try {
            logger.info("Received request to delete attraction ID: {}", attractionId);
            adminService.deleteAttraction(attractionId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Attraction not found for deletion with ID {}: {}", attractionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", true, "message", e.getMessage()));
        } catch (BadRequestException e) {
             logger.warn("Bad request while deleting attraction ID {}: {}", attractionId, e.getMessage());
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", true, "message", e.getMessage()));
        }
        catch (Exception e) {
             logger.error("Error deleting attraction ID {}: {}", attractionId, e.getMessage(), e);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", true, "message", "刪除行程時發生錯誤: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        logger.info("Received request to get admin stats.");
        AdminStatsDTO stats = adminService.getAdminStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/trends/registrations")
    public ResponseEntity<List<TrendDataPointDTO>> getRegistrationTrend() {
        logger.info("Received request to get user registration trends.");
        List<TrendDataPointDTO> trendData = adminService.getUserRegistrationTrends();
        return ResponseEntity.ok(trendData);
    }

    @GetMapping("/trends/bookings")
    public ResponseEntity<List<TrendDataPointDTO>> getBookingTrend() {
        logger.info("Received request to get booking trends.");
        List<TrendDataPointDTO> trendData = adminService.getBookingTrends();
        return ResponseEntity.ok(trendData);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex, WebRequest request) {
        logger.error("Unhandled exception occurred: Path: {}, Error: {}", request.getDescription(false), ex.getMessage(), ex);
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", "Internal Server Error",
                "message", "伺服器發生未預期的錯誤: " + ex.getMessage(),
                "path", request.getDescription(false).substring(4)
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
