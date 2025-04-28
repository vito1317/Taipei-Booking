package com.taipeibooking.controller;

import com.taipeibooking.dto.AttractionBasicDTO;
import com.taipeibooking.dto.BookingRequest;
import com.taipeibooking.exception.IllegalBookingStateException;
import com.taipeibooking.exception.ResourceNotFoundException;
import com.taipeibooking.model.Booking;
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


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class BookingController {

    private final BookingService bookingService;
    private final AttractionService attractionService;

    @Autowired
    public BookingController(BookingService bookingService, AttractionService attractionService) {
        this.bookingService = bookingService;
        this.attractionService = attractionService;
    }


    @GetMapping("/attractions")
    public ResponseEntity<?> getActiveAttractions() {
        try {
            List<AttractionBasicDTO> attractions = attractionService.getActiveAttractionsForFrontend();

            return ResponseEntity.ok(Map.of("data", attractions));
        } catch (Exception e) {
            System.err.println("獲取行程列表失敗: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", true, "message", "無法獲取行程列表"));
        }
    }



    @PostMapping("/booking")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
       try {
            Booking savedBooking = bookingService.createBooking(bookingRequest);
            return ResponseEntity.ok().body(Map.of(
                "ok", true,
                "bookingId", savedBooking.getId()
            ));
        } catch (UsernameNotFoundException e) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
             System.err.println("預定建立失敗: " + e.getMessage());
             e.printStackTrace();
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", true, "message", "伺服器內部錯誤，無法建立預定"));
        }
    }

    @PostMapping("/booking/{bookingId}/pay")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markBookingAsPaid(@PathVariable Long bookingId) {
        try {
            Booking updatedBooking = bookingService.markBookingAsPaid(bookingId);
            return ResponseEntity.ok(Map.of(
                "ok", true,
                "message", "付款成功，訂單狀態已更新為 PAID",
                "bookingId", updatedBooking.getId(),
                "newStatus", updatedBooking.getStatus()
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(Map.of("error", true, "message", e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body(Map.of("error", true, "message", e.getMessage()));
        } catch (IllegalBookingStateException e) {
             return ResponseEntity.status(HttpStatus.CONFLICT)
                                  .body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("標記預訂為已付款時失敗 (ID: " + bookingId + "): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", true, "message", "伺服器內部錯誤，無法更新訂單狀態"));
        }
    }


    @GetMapping("/booking")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUserBookings() {
        try {
            List<Booking> bookings = bookingService.getBookingsForCurrentUser();

            return ResponseEntity.ok(Map.of("data", bookings));
        } catch (UsernameNotFoundException e) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
             System.err.println("查詢預定失敗: " + e.getMessage());
             e.printStackTrace();
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", true, "message", "伺服器內部錯誤，無法查詢預定"));
        }
    }

    @DeleteMapping("/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteBooking(@PathVariable Long bookingId) {
        try {
            boolean deleted = bookingService.deleteBooking(bookingId);
            if (deleted) {
                 return ResponseEntity.ok().body(Map.of("ok", true));
            } else {

                 return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                      .body(Map.of("error", true, "message", "無法刪除預定，預定不存在或您無權操作"));
            }
        } catch (UsernameNotFoundException e) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", true, "message", e.getMessage()));
         } catch (ResourceNotFoundException e) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", true, "message", e.getMessage()));
        } catch (IllegalBookingStateException e) {
             return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", true, "message", e.getMessage()));
        } catch (Exception e) {
             System.err.println("刪除預定失敗: " + e.getMessage());
             e.printStackTrace();
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", true, "message", "伺服器內部錯誤，無法刪除預定"));
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

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", true);
        responseBody.put("message", "輸入資料驗證失敗");
        responseBody.put("errors", errors);
        return responseBody;
    }

     @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
     @ExceptionHandler(Exception.class)
     public Map<String, Object> handleGenericException(Exception ex) {
         System.err.println("發生未預期錯誤: " + ex.getMessage());
         ex.printStackTrace();
         Map<String, Object> responseBody = new HashMap<>();
         responseBody.put("error", true);

         responseBody.put("message", "伺服器發生未預期的錯誤，請稍後再試或聯繫管理員。");
         return responseBody;
     }
}
