package com.taipeibooking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingAdminViewDTO {
    private Long id;
    private String userName;
    private String attractionName;
    private LocalDate date;
    private String time;
    private BigDecimal price;
    private String status;
    private LocalDateTime createdAt;

    public BookingAdminViewDTO() {
    }

    public BookingAdminViewDTO(Long id, String userName, String attractionName, LocalDate date, String time, BigDecimal price, String status, LocalDateTime createdAt) {
        this.id = id;
        this.userName = userName;
        this.attractionName = attractionName;
        this.date = date;
        this.time = time;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
