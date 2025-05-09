package com.taipeibooking.controller;

import com.taipeibooking.dto.AttractionBasicDTO;
import com.taipeibooking.dto.BookingRequest;
import com.taipeibooking.dto.BookingResponseDTO;
import com.taipeibooking.dto.PaymentRequestDTO;
import com.taipeibooking.exception.BadRequestException;
import com.taipeibooking.exception.IllegalBookingStateException;
import com.taipeibooking.exception.ResourceNotFoundException;
import com.taipeibooking.service.AttractionService;
import com.taipeibooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;
    private final AttractionService attractionService;

    @Autowired
    public BookingController(BookingService bookingService, AttractionService attractionService) {
        this.bookingService = bookingService;
        this.attractionService = attractionService;
    }

    @GetMapping("/booking/attractions")
    public ResponseEntity<?> getActiveAttractionsForBooking() {
        try {
            List<AttractionBasicDTO> attractions = attractionService.getActiveAttractionsForFrontend();
            return ResponseEntity.ok(Map.of("data", attractions));
        } catch (Exception e) {
            logger.error("獲取可用於預訂的行程列表失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", true, "message", "無法獲取行程列表"));
        }
    }

    @PostMapping("/booking")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
       try {
            BookingResponseDTO newBooking = bookingService.createBooking(bookingRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", newBooking));
        } catch (UsernameNotFoundException e) {
             logger.warn("Booking creation failed: {}", e.getMessage());
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", true, "message", e.getMessage()));
        } catch (ResourceNotFoundException e) {
             logger.warn("Booking creation failed: {}", e.getMessage());
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", true, "message", e.getMessage()));
        } catch (BadRequestException e) {
            logger.warn("Booking creation failed due to bad request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
             logger.error("預訂建立失敗", e);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", true, "message", "伺服器內部錯誤，無法建立預訂"));
        }
    }

    @GetMapping("/booking")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUserBooking() {
        try {
            Optional<BookingResponseDTO> pendingBookingOpt = bookingService.getCurrentPendingBookingForUser();
            return ResponseEntity.ok(Map.of("data", pendingBookingOpt.orElse(null)));
        } catch (UsernameNotFoundException e) {
             logger.warn("Get current booking failed: {}", e.getMessage());
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
             logger.error("查詢當前預訂失敗", e);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", true, "message", "伺服器內部錯誤，無法查詢預訂"));
        }
    }

    @GetMapping("/bookings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllCurrentUserBookings() {
        try {
            List<BookingResponseDTO> bookings = bookingService.getBookingsForCurrentUser();
            return ResponseEntity.ok(Map.of("data", bookings));
        } catch (UsernameNotFoundException e) {
             logger.warn("Get all bookings failed: {}", e.getMessage());
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
             logger.error("查詢所有預訂失敗", e);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", true, "message", "伺服器內部錯誤，無法查詢預訂"));
        }
    }

    @PostMapping("/booking/{bookingId}/pay")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> processBookingPayment(
            @PathVariable Long bookingId,
            @RequestBody PaymentRequestDTO paymentRequest) {
        try {
            BookingResponseDTO updatedBooking = bookingService.processPaymentAndUpdateStatus(bookingId, paymentRequest.getPaymentNonce());
            return ResponseEntity.ok(Map.of("data", updatedBooking));
        } catch (UsernameNotFoundException e) {
            logger.warn("Payment failed for booking {}: {}", bookingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", true, "message", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            logger.warn("Payment failed for booking {}: {}", bookingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", true, "message", e.getMessage()));
        } catch (IllegalBookingStateException | BadRequestException e) {
            logger.warn("Payment failed for booking {}: {}", bookingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", true, "message", e.getMessage()));
        } catch (AccessDeniedException e) {
             logger.warn("Payment failed for booking {}: {}", bookingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", true, "message", "您無權支付此預訂"));
        } catch (Exception e) {
            logger.error("處理預訂 {} 付款時發生錯誤", bookingId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", true, "message", "處理付款時發生伺服器錯誤"));
        }
    }


    @DeleteMapping("/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteBookingById(@PathVariable Long bookingId) {
        try {
            bookingService.deleteBooking(bookingId);
            return ResponseEntity.ok().body(Map.of("ok", true));
        } catch (UsernameNotFoundException e) {
             logger.warn("Delete booking {} failed: {}", bookingId, e.getMessage());
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", true, "message", e.getMessage()));
         } catch (ResourceNotFoundException e) {
             logger.warn("Delete booking {} failed: {}", bookingId, e.getMessage());
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", true, "message", e.getMessage()));
        } catch (IllegalBookingStateException e) {
             logger.warn("Delete booking {} failed: {}", bookingId, e.getMessage());
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", true, "message", e.getMessage()));
        } catch (AccessDeniedException e) {
             logger.warn("Delete booking {} failed: {}", bookingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", true, "message", "您無權刪除此預訂"));
        }
        catch (Exception e) {
             logger.error("刪除預訂 {} 失敗", bookingId, e);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", true, "message", "伺服器內部錯誤，無法刪除預訂"));
        }
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        logger.warn("Validation failed: {}", errors);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", true);
        responseBody.put("message", "輸入資料驗證失敗");
        responseBody.put("errors", errors);
        return responseBody;
    }

     @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
     @ExceptionHandler(Exception.class)
     public Map<String, Object> handleGenericException(Exception ex) {
         logger.error("發生未預期錯誤", ex);
         Map<String, Object> responseBody = new HashMap<>();
         responseBody.put("error", true);
         responseBody.put("message", "伺服器發生未預期的錯誤，請稍後再試或聯繫管理員。");
         return responseBody;
     }
}
