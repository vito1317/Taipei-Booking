package com.taipeibooking.service;

import com.taipeibooking.dto.BookingRequest;
import com.taipeibooking.dto.BookingResponseDTO;
import com.taipeibooking.exception.BadRequestException;
import com.taipeibooking.exception.IllegalBookingStateException;
import com.taipeibooking.exception.ResourceNotFoundException;
import com.taipeibooking.model.Attraction;
import com.taipeibooking.model.Booking;
import com.taipeibooking.model.User;
import com.taipeibooking.repository.AttractionRepository;
import com.taipeibooking.repository.BookingRepository;
import com.taipeibooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final AttractionRepository attractionRepository;
    private final AuthService authService;

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          AttractionRepository attractionRepository,
                          AuthService authService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.attractionRepository = attractionRepository;
        this.authService = authService;
    }

    private User getCurrentAuthenticatedUser() {
        User user = authService.getCurrentUser();
        if (user == null) {
            throw new UsernameNotFoundException("使用者未登入或認證無效");
        }
        return user;
    }

    @Transactional
    public BookingResponseDTO createBooking(BookingRequest bookingRequest) {
        User currentUser = getCurrentAuthenticatedUser();
        logger.info("User {} attempting to create booking for attraction ID: {}", currentUser.getEmail(), bookingRequest.getAttractionId());

        Attraction attraction = attractionRepository.findById(bookingRequest.getAttractionId())
            .orElseThrow(() -> {
                 logger.warn("Attraction not found for ID: {}", bookingRequest.getAttractionId());
                 return new ResourceNotFoundException("找不到景點 ID: " + bookingRequest.getAttractionId());
            });

        if (!attraction.isActive()) {
             logger.warn("Attempt to book inactive attraction ID: {}", bookingRequest.getAttractionId());
            throw new BadRequestException("此景點目前無法預訂: " + attraction.getName());
        }

        logger.debug("Checking for existing pending booking for user {}, attraction {}, date {}",
                     currentUser.getId(), attraction.getId(), bookingRequest.getDate());
        Optional<Booking> existingPendingBooking = bookingRepository
            .findByUserAndAttractionAndDateAndStatus(currentUser, attraction, bookingRequest.getDate(), "PENDING");

        if(existingPendingBooking.isPresent()){
             logger.warn("User {} already has a pending booking for attraction {} on date {}",
                         currentUser.getEmail(), attraction.getId(), bookingRequest.getDate());
             throw new BadRequestException("您已對此景點和日期有待處理的預訂。");
        }
        logger.debug("No existing pending booking found. Proceeding to create new booking.");

        Booking booking = new Booking();
        booking.setUser(currentUser);
        booking.setAttraction(attraction);
        booking.setAttractionName(attraction.getName());
        booking.setAttractionImage(attraction.getImageUrl());
        booking.setDate(bookingRequest.getDate());
        booking.setTime(bookingRequest.getTime());
        booking.setPrice(bookingRequest.getPrice());
        booking.setCustomerName(bookingRequest.getContactName());
        booking.setContactPhone(bookingRequest.getContactPhone());
        booking.setCustomerIdNumber(bookingRequest.getCustomerIdNumber());
        booking.setStatus("PENDING");

        Booking savedBooking = bookingRepository.save(booking);
        logger.info("Booking created successfully with ID: {} for user {}", savedBooking.getId(), currentUser.getEmail());
        return convertToBookingResponseDTO(savedBooking);
    }

    public Optional<BookingResponseDTO> getCurrentPendingBookingForUser() {
        User currentUser = getCurrentAuthenticatedUser();
        return bookingRepository.findByUserOrderByDateDesc(currentUser).stream()
                .filter(b -> "PENDING".equalsIgnoreCase(b.getStatus()))
                .map(this::convertToBookingResponseDTO)
                .findFirst();
    }


    public List<BookingResponseDTO> getBookingsForCurrentUser() {
        User currentUser = getCurrentAuthenticatedUser();
        List<Booking> bookings = bookingRepository.findByUserOrderByDateDesc(currentUser);
        return bookings.stream().map(this::convertToBookingResponseDTO).collect(Collectors.toList());
    }

    public BookingResponseDTO getBookingByIdForCurrentUser(Long bookingId) {
        User currentUser = getCurrentAuthenticatedUser();
        Booking booking = bookingRepository.findByIdAndUser(bookingId, currentUser)
            .orElseThrow(() -> new ResourceNotFoundException("找不到預訂 ID: " + bookingId + " 或您無權查看"));
        return convertToBookingResponseDTO(booking);
    }

    @Transactional
    public void deleteBooking(Long bookingId) {
        User currentUser = getCurrentAuthenticatedUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到預訂 ID: " + bookingId));

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("您無權刪除此預訂");
        }

        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalBookingStateException("只有在 '待處理' 狀態的預訂才能被刪除。目前狀態: " + booking.getStatus());
        }
        bookingRepository.delete(booking);
         logger.info("Booking ID: {} deleted successfully by user {}", bookingId, currentUser.getEmail());
    }

    @Transactional
    public BookingResponseDTO processPaymentAndUpdateStatus(Long bookingId, String paymentNonce) {
        User currentUser = getCurrentAuthenticatedUser();
        logger.info("User {} attempting to process payment for booking ID: {}", currentUser.getEmail(), bookingId);

        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> {
                logger.warn("Payment failed: Booking not found for ID: {}", bookingId);
                return new ResourceNotFoundException("找不到預訂 ID: " + bookingId);
            });

        if (!booking.getUser().getId().equals(currentUser.getId())) {
             logger.warn("Payment failed: User {} does not own booking ID: {}", currentUser.getEmail(), bookingId);
            throw new AccessDeniedException("您無權支付此預訂");
        }

        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
             logger.warn("Payment failed: Booking ID {} is not in PENDING status (current: {})", bookingId, booking.getStatus());
            throw new IllegalBookingStateException("只有 '待處理' 狀態的預訂才能付款。目前狀態: " + booking.getStatus());
        }

        boolean paymentSuccessful = simulatePaymentGateway(paymentNonce, booking.getPrice());

        if (paymentSuccessful) {
            booking.setStatus("PAID");
            Booking updatedBooking = bookingRepository.save(booking);
            logger.info("Booking ID: {} successfully marked as PAID for user {}", bookingId, currentUser.getEmail());
            return convertToBookingResponseDTO(updatedBooking);
        } else {
            logger.error("Simulated payment failed for booking ID: {}", bookingId);
            throw new BadRequestException("付款處理失敗，請稍後再試或聯繫客服。");
        }
    }

    private boolean simulatePaymentGateway(String nonce, BigDecimal amount) {
        logger.info("Simulating payment processing for amount {} with nonce/token starting with: {}",
                    amount, (nonce != null && nonce.length() > 5 ? nonce.substring(0, 5) + "..." : nonce));
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return !"fail".equalsIgnoreCase(nonce);
    }


    private BookingResponseDTO convertToBookingResponseDTO(Booking booking) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setDate(booking.getDate());
        dto.setTime(booking.getTime());
        dto.setPrice(booking.getPrice());
        dto.setStatus(booking.getStatus());

        if (booking.getAttraction() != null) {
            BookingResponseDTO.AttractionDetails attractionDetails = new BookingResponseDTO.AttractionDetails();
            attractionDetails.setId(booking.getAttraction().getId());
            attractionDetails.setName(booking.getAttraction().getName());
            attractionDetails.setAddress(booking.getAttraction().getAddress());
            attractionDetails.setImage(booking.getAttraction().getImageUrl());
            dto.setAttraction(attractionDetails);
        } else {
            BookingResponseDTO.AttractionDetails attractionDetails = new BookingResponseDTO.AttractionDetails();
            attractionDetails.setId(booking.getAttractionIdOriginal());
            attractionDetails.setName(booking.getAttractionName());
            attractionDetails.setImage(booking.getAttractionImage());
            dto.setAttraction(attractionDetails);
        }
        return dto;
    }
}
