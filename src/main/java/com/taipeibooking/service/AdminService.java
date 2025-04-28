package com.taipeibooking.service;

import com.taipeibooking.dto.*;
import com.taipeibooking.exception.BadRequestException;
import com.taipeibooking.exception.ResourceNotFoundException;
import com.taipeibooking.model.Attraction;
import com.taipeibooking.model.Booking;
import com.taipeibooking.model.User;
import com.taipeibooking.repository.AttractionRepository;
import com.taipeibooking.repository.BookingRepository;
import com.taipeibooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AdminStatsDTO getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalBookings = bookingRepository.count();
        BigDecimal totalRevenue = bookingRepository.findAll().stream()
                .filter(booking -> "PAID".equalsIgnoreCase(booking.getStatus()))
                .map(Booking::getPrice)
                .filter(price -> price != null)
                .map(price -> BigDecimal.valueOf(price.longValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        AdminStatsDTO stats = new AdminStatsDTO();
        stats.setTotalUsers(totalUsers);
        stats.setTotalBookings(totalBookings);
        stats.setTotalRevenue(totalRevenue);
        return stats;
    }

    public List<TrendDataPointDTO> getBookingTrend() {
        List<Booking> bookings = bookingRepository.findAll();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        Map<String, Long> trends = bookings.stream()
                 .filter(b -> b.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getCreatedAt().toLocalDate().format(dateFormatter),
                        Collectors.counting()
                ));
        return trends.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new TrendDataPointDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<TrendDataPointDTO> getRegistrationTrend() {
        List<User> users = userRepository.findAll();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Long> trends = users.stream()
                .filter(u -> u.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedAt().format(monthFormatter),
                        Collectors.counting()
                ));
        return trends.entrySet().stream()
                 .sorted(Map.Entry.comparingByKey())
                .map(entry -> new TrendDataPointDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public Page<UserAdminViewDTO> getAllUsersForAdmin(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToUserAdminViewDTO);
    }

     public UserAdminViewDTO getUserByIdForAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到 ID 為 " + userId + " 的用戶"));
        return convertToUserAdminViewDTO(user);
    }

    @Transactional
    public UserAdminViewDTO createUserByAdmin(AdminCreateUserRequestDTO requestDTO) {
        if (userRepository.existsByUsername(requestDTO.getEmail())) {
            throw new BadRequestException("電子郵件已被使用: " + requestDTO.getEmail());
        }
        User user = new User();
        user.setName(requestDTO.getName());
        user.setUsername(requestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setRole(requestDTO.getRole() != null ? requestDTO.getRole() : "ROLE_USER");
        User savedUser = userRepository.save(user);
        return convertToUserAdminViewDTO(savedUser);
    }

    @Transactional
    public UserAdminViewDTO updateUserByAdmin(Long userId, AdminUpdateUserRequestDTO requestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到 ID 為 " + userId + " 的用戶"));
        if (requestDTO.getEmail() != null && !requestDTO.getEmail().equals(user.getUsername())) {
            user.setUsername(requestDTO.getEmail());
        }
        if (requestDTO.getName() != null) {
            user.setName(requestDTO.getName());
        }
        if (requestDTO.getRole() != null) {
            user.setRole(requestDTO.getRole());
        }
        User updatedUser = userRepository.save(user);
        return convertToUserAdminViewDTO(updatedUser);
    }

    @Transactional
    public UserAdminViewDTO updateUserRoleByAdmin(Long userId, UpdateUserRoleRequestDTO requestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到 ID 為 " + userId + " 的用戶"));
        if (requestDTO.getRole() == null) {
            throw new BadRequestException("角色不能為空");
        }
        user.setRole(requestDTO.getRole());
        User updatedUser = userRepository.save(user);
        return convertToUserAdminViewDTO(updatedUser);
    }

    @Transactional
    public void deleteUserByAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到 ID 為 " + userId + " 的用戶"));
        bookingRepository.deleteAll(bookingRepository.findByUserId(userId));
        userRepository.delete(user);
    }

    public Page<Booking> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    public Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到預訂 ID: " + bookingId));
    }

    public AdminBookingDetailDTO getAdminBookingDetailsById(Long bookingId) {
        Booking booking = findBookingById(bookingId);
        return convertToAdminBookingDetailDTO(booking);
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = findBookingById(bookingId);
        bookingRepository.delete(booking);
    }

    @Transactional(readOnly = true)
    public Page<AttractionAdminViewDTO> getAllAttractions(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("id").ascending()
            );
        }
        return attractionRepository.findAll(pageable).map(this::convertToAttractionAdminViewDTO);
    }

    @Transactional(readOnly = true)
    public AttractionAdminViewDTO getAttractionById(Long attractionId) {
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到行程 ID: " + attractionId));
        return convertToAttractionAdminViewDTO(attraction);
    }

    @Transactional
    public AttractionAdminViewDTO createAttraction(CreateAttractionRequestDTO requestDTO) {
        Attraction attraction = new Attraction();
        attraction.setName(requestDTO.getName());
        attraction.setDescription(requestDTO.getDescription());
        attraction.setAddress(requestDTO.getAddress());
        attraction.setImageUrl(requestDTO.getImageUrl());
        attraction.setMrt(requestDTO.getMrt());
        attraction.setCategory(requestDTO.getCategory());
        attraction.setIsActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true);
        Attraction savedAttraction = attractionRepository.save(attraction);
        return convertToAttractionAdminViewDTO(savedAttraction);
    }

    @Transactional
    public AttractionAdminViewDTO updateAttraction(Long attractionId, UpdateAttractionRequestDTO requestDTO) {
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到要更新的行程 ID: " + attractionId));
        if (requestDTO.getName() != null) attraction.setName(requestDTO.getName());
        if (requestDTO.getDescription() != null) attraction.setDescription(requestDTO.getDescription());
        if (requestDTO.getAddress() != null) attraction.setAddress(requestDTO.getAddress());
        if (requestDTO.getImageUrl() != null) attraction.setImageUrl(requestDTO.getImageUrl());
        if (requestDTO.getMrt() != null) attraction.setMrt(requestDTO.getMrt());
        if (requestDTO.getCategory() != null) attraction.setCategory(requestDTO.getCategory());
        if (requestDTO.getIsActive() != null) attraction.setIsActive(requestDTO.getIsActive());
        Attraction updatedAttraction = attractionRepository.save(attraction);
        return convertToAttractionAdminViewDTO(updatedAttraction);
    }

     @Transactional
    public AttractionAdminViewDTO updateAttractionStatus(Long attractionId, boolean isActive) {
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到要更新狀態的行程 ID: " + attractionId));
        attraction.setIsActive(isActive);
        Attraction updatedAttraction = attractionRepository.save(attraction);
        return convertToAttractionAdminViewDTO(updatedAttraction);
    }

    @Transactional
    public void deleteAttraction(Long attractionId) {
        if (!attractionRepository.existsById(attractionId)) {
            throw new ResourceNotFoundException("找不到要刪除的行程 ID: " + attractionId);
        }
        attractionRepository.deleteById(attractionId);
    }

    private UserAdminViewDTO convertToUserAdminViewDTO(User user) {
        if (user == null) return null;
        UserAdminViewDTO dto = new UserAdminViewDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getUsername());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    private AdminBookingDetailDTO convertToAdminBookingDetailDTO(Booking booking) {
        if (booking == null) return null;
        AdminBookingDetailDTO dto = new AdminBookingDetailDTO();
        dto.setId(booking.getId());
        if (booking.getDate() != null) {
            dto.setDate(new java.sql.Date(booking.getDate().getTime()).toLocalDate());
        } else {
            dto.setDate(null);
        }
        dto.setTime(booking.getTime());
        Integer priceInt = booking.getPrice();
        if (priceInt != null) {
            dto.setPrice(BigDecimal.valueOf(priceInt.longValue()));
        } else {
             dto.setPrice(null);
        }
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        User customer = booking.getUser();
        if (customer != null) {
            dto.setCustomerName(customer.getName());
            dto.setCustomerEmail(customer.getUsername());
            dto.setContactPhone(booking.getContactPhone());
            dto.setCustomerIdNumber(booking.getCustomerIdNumber());
        } else {
             dto.setCustomerName(booking.getCustomerName());
             dto.setContactPhone(booking.getContactPhone());
             dto.setCustomerIdNumber(booking.getCustomerIdNumber());
             dto.setCustomerEmail(null);
        }
        dto.setAttractionName(booking.getAttractionName());
        dto.setAttractionAddress(booking.getAttractionAddress());
        dto.setAttractionImage(booking.getAttractionImage());
        return dto;
    }

    private AttractionAdminViewDTO convertToAttractionAdminViewDTO(Attraction attraction) {
        if (attraction == null) return null;
        AttractionAdminViewDTO dto = new AttractionAdminViewDTO();
        dto.setId(attraction.getId());
        dto.setName(attraction.getName());
        dto.setDescription(attraction.getDescription());
        dto.setAddress(attraction.getAddress());
        dto.setImageUrl(attraction.getImageUrl());
        dto.setMrt(attraction.getMrt());
        dto.setCategory(attraction.getCategory());
        dto.setIsActive(attraction.getIsActive());
        dto.setCreatedAt(attraction.getCreatedAt());
        dto.setUpdatedAt(attraction.getUpdatedAt());
        return dto;
    }
}
