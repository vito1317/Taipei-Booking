package com.taipeibooking.service;

import com.taipeibooking.dto.*;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Page<UserAdminViewDTO> getAllUsers(Pageable pageable) {
        logger.debug("Fetching all users with pageable: {}", pageable);
        return userRepository.findAll(pageable).map(this::convertToUserAdminViewDTO);
    }

    public UserAdminViewDTO getUserById(Long userId) {
        logger.debug("Fetching user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("找不到使用者 ID: " + userId);
                });
        return convertToUserAdminViewDTO(user);
    }

    @Transactional
    public UserAdminViewDTO createUser(AdminCreateUserRequestDTO requestDTO) {
        logger.info("Attempting to create user with email: {}", requestDTO.getEmail());

        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            logger.warn("Email {} already exists. Cannot create user.", requestDTO.getEmail());
            throw new BadRequestException("Email " + requestDTO.getEmail() + " 已存在");
        }

        if (requestDTO == null) {
            logger.error("AdminCreateUserRequestDTO is null. Cannot create user.");
            throw new BadRequestException("請求資料不能為空");
        }

        User user = new User();
        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());
        user.setUsername(requestDTO.getEmail());

        if (requestDTO.getPassword() == null || requestDTO.getPassword().trim().isEmpty()) {
            logger.error("Password is null or empty for email: {}. Cannot create user.", requestDTO.getEmail());
            throw new BadRequestException("密碼不能為空");
        }
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        logger.debug("Password encoded for user: {}", requestDTO.getEmail());

        user.setRole(requestDTO.getRole() != null && !requestDTO.getRole().trim().isEmpty()
                ? requestDTO.getRole().toUpperCase()
                : "USER");
        user.setAge(requestDTO.getAge());
        user.setGender(requestDTO.getGender());

        try {
            User savedUser = userRepository.save(user);
            logger.info("User {} created successfully with ID: {}", savedUser.getEmail(), savedUser.getId());
            return convertToUserAdminViewDTO(savedUser);
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while creating user with email {}: {}", requestDTO.getEmail(), e.getMessage(), e);
            throw new BadRequestException("創建用戶失敗，資料庫約束衝突，可能是 Email 或用戶名已存在。詳細錯誤: " + e.getMostSpecificCause().getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating user with email {}: {}", requestDTO.getEmail(), e.getMessage(), e);
            throw new RuntimeException("創建用戶時發生未預期的內部錯誤，請查看日誌。");
        }
    }

    @Transactional
    public UserAdminViewDTO updateUser(Long userId, AdminUpdateUserRequestDTO requestDTO) {
        logger.info("Attempting to update user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {} for update.", userId);
                    return new ResourceNotFoundException("找不到使用者 ID: " + userId);
                });

        if (requestDTO == null) {
            logger.error("AdminUpdateUserRequestDTO is null for user ID: {}. Cannot update user.", userId);
            throw new BadRequestException("請求資料不能為空");
        }

        user.setName(requestDTO.getName());

        if (requestDTO.getEmail() != null && !requestDTO.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(requestDTO.getEmail())) {
                logger.warn("New email {} already exists. Cannot update user ID: {}", requestDTO.getEmail(), userId);
                throw new BadRequestException("Email " + requestDTO.getEmail() + " 已被其他使用者使用");
            }
            user.setEmail(requestDTO.getEmail());
            user.setUsername(requestDTO.getEmail());
        }

        if (requestDTO.getRole() != null && !requestDTO.getRole().trim().isEmpty()) {
            user.setRole(requestDTO.getRole().toUpperCase());
        }
        user.setAge(requestDTO.getAge());
        user.setGender(requestDTO.getGender());

        if (requestDTO.getPassword() != null && !requestDTO.getPassword().trim().isEmpty()) {
            if (requestDTO.getPassword().length() < 4) {
                 logger.warn("Password for user ID {} is too short.", userId);
                 throw new BadRequestException("新密碼長度至少需要4個字元");
            }
            user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
            logger.debug("Password updated for user ID: {}", userId);
        }

        try {
            User updatedUser = userRepository.save(user);
            logger.info("User with ID: {} updated successfully.", updatedUser.getId());
            return convertToUserAdminViewDTO(updatedUser);
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while updating user ID {}: {}", userId, e.getMessage(), e);
            throw new BadRequestException("更新用戶失敗，資料庫約束衝突。詳細錯誤: " + e.getMostSpecificCause().getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating user ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("更新用戶時發生未預期的內部錯誤，請查看日誌。");
        }
    }

    @Transactional
    public void deleteUser(Long userId) {
        logger.info("Attempting to delete user with ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            logger.warn("User not found with ID: {} for deletion.", userId);
            throw new ResourceNotFoundException("找不到使用者 ID: " + userId);
        }
        logger.debug("Deleting bookings for user ID: {}", userId);
        bookingRepository.deleteByUser_Id(userId);
        userRepository.deleteById(userId);
        logger.info("User with ID: {} and their associated bookings deleted successfully.", userId);
    }

    public Page<AttractionAdminViewDTO> getAllAttractions(Pageable pageable) {
        logger.debug("Fetching all attractions with pageable: {}", pageable);
        return attractionRepository.findAll(pageable).map(this::convertToAttractionAdminViewDTO);
    }

    public AttractionAdminViewDTO getAttractionAdminViewById(Long attractionId) {
        logger.debug("Fetching attraction for admin view by ID: {}", attractionId);
        Attraction attraction = attractionRepository.findById(attractionId)
            .orElseThrow(() -> {
                logger.warn("Attraction not found with ID: {}", attractionId);
                return new ResourceNotFoundException("找不到景點 ID: " + attractionId);
            });
        return convertToAttractionAdminViewDTO(attraction);
    }

    @Transactional
    public AttractionAdminViewDTO createAttraction(CreateAttractionRequestDTO requestDTO) {
        logger.info("Attempting to create attraction with name: {}", requestDTO.getName());
        if (requestDTO == null) {
            logger.error("CreateAttractionRequestDTO is null. Cannot create attraction.");
            throw new BadRequestException("請求資料不能為空");
        }
        Attraction attraction = new Attraction();
        mapDtoToAttraction(requestDTO, attraction);
        attraction.setActive(true);

        try {
            Attraction savedAttraction = attractionRepository.save(attraction);
            logger.info("Attraction {} created successfully with ID: {}", savedAttraction.getName(), savedAttraction.getId());
            return convertToAttractionAdminViewDTO(savedAttraction);
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while creating attraction {}: {}", requestDTO.getName(), e.getMessage(), e);
            throw new BadRequestException("創建景點失敗，資料庫約束衝突。詳細錯誤: " + e.getMostSpecificCause().getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating attraction {}: {}", requestDTO.getName(), e.getMessage(), e);
            throw new RuntimeException("創建景點時發生未預期的內部錯誤，請查看日誌。");
        }
    }

    @Transactional
    public AttractionAdminViewDTO updateAttraction(Long attractionId, UpdateAttractionRequestDTO requestDTO) {
        logger.info("Attempting to update attraction with ID: {}", attractionId);
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> {
                    logger.warn("Attraction not found with ID: {} for update.", attractionId);
                    return new ResourceNotFoundException("找不到景點 ID: " + attractionId);
                });
        if (requestDTO == null) {
            logger.error("UpdateAttractionRequestDTO is null for attraction ID: {}. Cannot update.", attractionId);
            throw new BadRequestException("請求資料不能為空");
        }
        mapDtoToAttraction(requestDTO, attraction);

        try {
            Attraction updatedAttraction = attractionRepository.save(attraction);
            logger.info("Attraction with ID: {} updated successfully.", updatedAttraction.getId());
            return convertToAttractionAdminViewDTO(updatedAttraction);
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while updating attraction ID {}: {}", attractionId, e.getMessage(), e);
            throw new BadRequestException("更新景點失敗，資料庫約束衝突。詳細錯誤: " + e.getMostSpecificCause().getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating attraction ID {}: {}", attractionId, e.getMessage(), e);
            throw new RuntimeException("更新景點時發生未預期的內部錯誤，請查看日誌。");
        }
    }

    @Transactional
    public AttractionAdminViewDTO updateAttractionStatus(Long attractionId, boolean isActive) {
        logger.info("Attempting to update status for attraction ID: {} to isActive: {}", attractionId, isActive);
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> {
                    logger.warn("Attraction not found with ID: {} for status update.", attractionId);
                    return new ResourceNotFoundException("找不到景點 ID: " + attractionId);
                });
        attraction.setActive(isActive);
        Attraction updatedAttraction = attractionRepository.save(attraction);
        logger.info("Status for attraction ID: {} updated successfully to isActive: {}", attractionId, isActive);
        return convertToAttractionAdminViewDTO(updatedAttraction);
    }

    @Transactional
    public void deleteAttraction(Long attractionId) {
        logger.info("Attempting to delete attraction with ID: {}", attractionId);
        Attraction attraction = attractionRepository.findById(attractionId)
            .orElseThrow(() -> {
                logger.warn("Attraction not found with ID: {} for deletion.", attractionId);
                return new ResourceNotFoundException("找不到景點 ID: " + attractionId);
            });

        if (bookingRepository.existsByAttraction_IdAndStatusIn(attractionId, List.of("PENDING", "PAID", "CONFIRMED"))) {
            logger.warn("Cannot delete attraction ID: {} because it has active bookings. Set to inactive instead.", attractionId);
            throw new BadRequestException("無法刪除景點，因為它有關聯的有效預訂。請先將景點設為非活躍狀態，或處理相關預訂。");
        }
        attractionRepository.delete(attraction);
        logger.info("Attraction with ID: {} deleted successfully.", attractionId);
    }

     public Page<BookingAdminViewDTO> getAllBookings(Pageable pageable) {
         logger.debug("Fetching all bookings for admin view with pageable: {}", pageable);
         return bookingRepository.findAll(pageable).map(this::convertToBookingAdminViewDTO);
     }

     public AdminBookingDetailDTO getBookingDetails(Long bookingId) {
         logger.debug("Fetching booking details for admin view for booking ID: {}", bookingId);
         Booking booking = bookingRepository.findById(bookingId)
             .orElseThrow(() -> {
                 logger.warn("Booking not found with ID: {}", bookingId);
                 return new ResourceNotFoundException("找不到預訂 ID: " + bookingId);
             });
         return convertToAdminBookingDetailDTO(booking);
     }

     @Transactional
     public void cancelBookingByAdmin(Long bookingId) {
         logger.info("Admin attempting to cancel booking with ID: {}", bookingId);
         Booking booking = bookingRepository.findById(bookingId)
             .orElseThrow(() -> {
                 logger.warn("Booking not found with ID: {} for cancellation by admin.", bookingId);
                 return new ResourceNotFoundException("找不到預訂 ID: " + bookingId);
             });
         if ("CANCELLED".equalsIgnoreCase(booking.getStatus()) || "COMPLETED".equalsIgnoreCase(booking.getStatus())) {
             logger.warn("Booking ID: {} is already {} or {}, cannot cancel.", bookingId, booking.getStatus(), "COMPLETED");
             throw new IllegalBookingStateException("預訂狀態為 " + booking.getStatus() + "，無法取消。");
         }
         booking.setStatus("CANCELLED");
         bookingRepository.save(booking);
         logger.info("Booking ID: {} cancelled successfully by admin.", bookingId);
     }

    public AdminStatsDTO getAdminStats() {
        logger.debug("Calculating admin statistics.");
        List<User> generalUsers = userRepository.findByRole("USER");
        long totalGeneralUsers = generalUsers.size();
        long totalBookings = bookingRepository.count();
        long totalAttractions = attractionRepository.count();

        Map<String, Long> ageDistribution = generalUsers.stream()
            .filter(u -> u.getAge() != null)
            .collect(Collectors.groupingBy(u -> mapAgeToGroup(u.getAge()), Collectors.counting()));
        logger.debug("Age distribution calculated: {}", ageDistribution);

        Map<String, Long> genderDistribution = generalUsers.stream()
            .filter(u -> u.getGender() != null && !u.getGender().trim().isEmpty())
            .collect(Collectors.groupingBy(User::getGender, Collectors.counting()));
        logger.debug("Gender distribution calculated: {}", genderDistribution);

        List<TrendDataPointDTO> bookingTrends = getBookingTrends();
        List<TrendDataPointDTO> userRegistrationTrends = getUserRegistrationTrends();

        AdminStatsDTO stats = new AdminStatsDTO(totalGeneralUsers, totalBookings, totalAttractions, bookingTrends, userRegistrationTrends, ageDistribution, genderDistribution);
        logger.info("Admin statistics generated: TotalUsers={}, TotalBookings={}, TotalAttractions={}", totalGeneralUsers, totalBookings, totalAttractions);
        return stats;
    }

    public List<TrendDataPointDTO> getBookingTrends() {
        logger.debug("Generating booking trends for the last 30 days.");
        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(29).withHour(0).withMinute(0).withSecond(0).withNano(0);

            List<Object[]> results = bookingRepository.findBookingTrendsByDateRange(startDate, endDate);
             if (results == null) {
                 logger.warn("Booking trends query returned null.");
                 return Collections.emptyList();
             }
            logger.debug("Raw booking trends data from repository: {} entries", results.size());

            return results.stream()
                .map(result -> {
                    LocalDateTime dateTime = (LocalDateTime) result[0];
                    Long count = (Long) result[1];
                    return new TrendDataPointDTO(dateTime.toLocalDate().toString(), count);
                })
                .sorted((d1, d2) -> LocalDate.parse(d1.getLabel()).compareTo(LocalDate.parse(d2.getLabel())))
                .collect(Collectors.toList());
        } catch (Exception e) {
             logger.error("Failed to generate booking trends: {}", e.getMessage(), e);
             return Collections.singletonList(new TrendDataPointDTO("Error", 0L));
        }
    }

    public List<TrendDataPointDTO> getUserRegistrationTrends() {
         logger.debug("Generating user registration trends for the last 30 days.");
         try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(29).withHour(0).withMinute(0).withSecond(0).withNano(0);

             Map<LocalDate, Long> dailyUserCounts = userRepository.findAll().stream()
                 .filter(u -> u.getCreatedAt() != null &&
                               u.getCreatedAt().isAfter(startDate.minusNanos(1)) &&
                               u.getCreatedAt().isBefore(endDate.plusNanos(1)))
                 .collect(Collectors.groupingBy(u -> u.getCreatedAt().toLocalDate(), Collectors.counting()));
            logger.debug("Raw user registration trends data: {} entries", dailyUserCounts.size());

             return dailyUserCounts.entrySet().stream()
                 .map(entry -> new TrendDataPointDTO(entry.getKey().toString(), entry.getValue()))
                 .sorted((d1, d2) -> LocalDate.parse(d1.getLabel()).compareTo(LocalDate.parse(d2.getLabel())))
                 .collect(Collectors.toList());
         } catch (Exception e) {
             logger.error("Failed to generate user registration trends: {}", e.getMessage(), e);
             return List.of(new TrendDataPointDTO("User Registrations (Error)", 0L));
         }
    }

    private UserAdminViewDTO convertToUserAdminViewDTO(User user) {
        if (user == null) return null;
        return new UserAdminViewDTO(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getAge(), user.getGender());
    }

    private AttractionAdminViewDTO convertToAttractionAdminViewDTO(Attraction attraction) {
        if (attraction == null) return null;
        return new AttractionAdminViewDTO(
            attraction.getId(),
            attraction.getName(),
            attraction.getCategory(),
            attraction.getMrt(),
            attraction.getDistrict(),
            attraction.getDescription(),
            attraction.isActive()
        );
    }

    private void mapDtoToAttraction(CreateAttractionRequestDTO dto, Attraction attraction) {
        attraction.setName(dto.getName());
        attraction.setDescription(dto.getDescription());
        attraction.setAddress(dto.getAddress());
        attraction.setLat(dto.getLat());
        attraction.setLng(dto.getLng());
        attraction.setTransport(dto.getTransport());
        attraction.setMrt(dto.getMrt());
        attraction.setCategory(dto.getCategory());
        attraction.setDistrict(dto.getDistrict());
        attraction.setImageUrl(dto.getImageUrl());
    }

    private void mapDtoToAttraction(UpdateAttractionRequestDTO dto, Attraction attraction) {
        attraction.setName(dto.getName());
        attraction.setDescription(dto.getDescription());
        attraction.setAddress(dto.getAddress());
        attraction.setLat(dto.getLat());
        attraction.setLng(dto.getLng());
        attraction.setTransport(dto.getTransport());
        attraction.setMrt(dto.getMrt());
        attraction.setCategory(dto.getCategory());
        attraction.setDistrict(dto.getDistrict());
        attraction.setImageUrl(dto.getImageUrl());
    }

    private BookingAdminViewDTO convertToBookingAdminViewDTO(Booking booking) {
        if (booking == null) return null;
         return new BookingAdminViewDTO(
            booking.getId(),
            booking.getUser() != null ? booking.getUser().getName() : "N/A (使用者資料遺失)",
            booking.getAttraction() != null ? booking.getAttraction().getName() : (booking.getAttractionName() != null ? booking.getAttractionName() : "N/A (景點資料遺失)"),
            booking.getDate(),
            booking.getTime(),
            booking.getPrice(),
            booking.getStatus(),
            booking.getCreatedAt()
        );
    }

    private AdminBookingDetailDTO convertToAdminBookingDetailDTO(Booking booking) {
        if (booking == null) return null;
        User user = booking.getUser();
        Attraction attraction = booking.getAttraction();

        String attractionName = "N/A";
        String attractionAddress = "N/A";
        String imageUrl = null;

        if (attraction != null) {
            attractionName = attraction.getName();
            attractionAddress = attraction.getAddress();
            imageUrl = attraction.getImageUrl();
        } else if (booking.getAttractionIdOriginal() != null) {
            logger.warn("Booking ID {} references a potentially deleted attraction (Original ID: {}). Using stored name/image.",
                        booking.getId(), booking.getAttractionIdOriginal());
            attractionName = booking.getAttractionName() != null ? booking.getAttractionName() : "景點資料已移除 (ID: " + booking.getAttractionIdOriginal() + ")";
        } else {
             attractionName = booking.getAttractionName() != null ? booking.getAttractionName() : "景點資料遺失";
        }
        imageUrl = booking.getAttractionImage() != null ? booking.getAttractionImage() : imageUrl;


        return new AdminBookingDetailDTO(
            booking.getId(),
            user != null ? user.getName() : "使用者資料遺失",
            user != null ? user.getEmail() : "使用者 Email 遺失",
            attractionName,
            attractionAddress,
            imageUrl,
            booking.getDate(),
            booking.getTime(),
            booking.getPrice(),
            booking.getStatus()
        );
    }

    private String mapAgeToGroup(Integer age) {
        if (age == null) return "未知";
        if (age < 18) return "<18";
        if (age >= 18 && age <= 25) return "18-25";
        if (age >= 26 && age <= 35) return "26-35";
        if (age >= 36 && age <= 45) return "36-45";
        if (age >= 46 && age <= 55) return "46-55";
        if (age > 55) return ">55";
        return "未知";
    }
}
