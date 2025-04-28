package com.taipeibooking.service;

import com.taipeibooking.dto.BookingRequest;
import com.taipeibooking.exception.IllegalBookingStateException;
import com.taipeibooking.exception.ResourceNotFoundException;
import com.taipeibooking.model.Booking;
import com.taipeibooking.model.User;
import com.taipeibooking.repository.BookingRepository;
import com.taipeibooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class BookingService {


    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;


    @Autowired
    public BookingService(BookingRepository bookingRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }


    private User getCurrentAuthenticatedUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
             throw new UsernameNotFoundException("使用者未登入或認證無效");
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("找不到使用者: " + username));
    }



    @Transactional
    public Booking createBooking(BookingRequest bookingRequest) {
        User currentUser = getCurrentAuthenticatedUser();

        Booking booking = new Booking();
        booking.setUser(currentUser);

        if (bookingRequest.getContactName() != null && !bookingRequest.getContactName().isEmpty()) {
             booking.setCustomerName(bookingRequest.getContactName());
        } else {
             booking.setCustomerName(currentUser.getName());
        }

        booking.setAttractionId(bookingRequest.getAttractionId());
        booking.setDate(bookingRequest.getDate());
        booking.setTime(bookingRequest.getTime());
        booking.setPrice(bookingRequest.getPrice());
        booking.setAttractionName(bookingRequest.getAttractionName());

        booking.setAttractionImage(bookingRequest.getAttractionImage());
        booking.setCustomerIdNumber(bookingRequest.getCustomerIdNumber());
        booking.setContactPhone(bookingRequest.getContactPhone());


        booking.setStatus("PENDING");


        return bookingRepository.save(booking);
    }


    @Transactional
    public Booking markBookingAsPaid(Long bookingId) {
        User currentUser = getCurrentAuthenticatedUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到預訂 ID: " + bookingId));


        if (!booking.getUser().getId().equals(currentUser.getId())) {
            System.err.println("權限錯誤：使用者 " + currentUser.getUsername() + " 嘗試將不屬於自己的預訂 (ID: " + bookingId + ") 標記為已付款。");
            throw new AccessDeniedException("無權限修改此預訂狀態");
        }



        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
             System.err.println("狀態錯誤：嘗試將狀態為 " + booking.getStatus() + " 的預訂 (ID: " + bookingId + ") 標記為已付款。");
            throw new IllegalBookingStateException("只有狀態為 PENDING 的預訂才能標記為已付款");
        }


        booking.setStatus("PAID");
        return bookingRepository.save(booking);
    }


    public List<Booking> getBookingsForCurrentUser() {
        User currentUser = getCurrentAuthenticatedUser();

        return bookingRepository.findByUserId(currentUser.getId());
    }


    @Transactional
    public boolean deleteBooking(Long bookingId) {
        User currentUser = getCurrentAuthenticatedUser();

        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);

        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();


            if (booking.getUser().getId().equals(currentUser.getId())) {
                bookingRepository.deleteById(bookingId);
                return true;
            } else {

                System.err.println("警告：使用者 " + currentUser.getUsername() + " 嘗試刪除非自己的預定 (ID: " + bookingId + ")");
                return false;
            }
        }
        return false;
    }
}