package com.taipeibooking.dto;

import java.time.LocalDateTime;

public class UserAdminViewDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private Long bookingCount;


    public UserAdminViewDTO(Long id, String name, String email, String role, LocalDateTime createdAt, Long bookingCount) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.bookingCount = bookingCount;
    }
    public UserAdminViewDTO() {}


    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getBookingCount() { return bookingCount; }


    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setBookingCount(Long bookingCount) { this.bookingCount = bookingCount; }
}