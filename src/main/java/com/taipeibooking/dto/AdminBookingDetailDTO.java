package com.taipeibooking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AdminBookingDetailDTO {
    private Long bookingId;
    private String userName;
    private String userEmail;
    private String attractionName;
    private String attractionAddress;
    private String attractionImage;
    private LocalDate date;
    private String time;
    private BigDecimal price;
    private String status;

    public AdminBookingDetailDTO() {
    }

    public AdminBookingDetailDTO(Long bookingId, String userName, String userEmail, String attractionName, String attractionAddress, String attractionImage, LocalDate date, String time, BigDecimal price, String status) {
        this.bookingId = bookingId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.attractionName = attractionName;
        this.attractionAddress = attractionAddress;
        this.attractionImage = attractionImage;
        this.date = date;
        this.time = time;
        this.price = price;
        this.status = status;
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getAttractionName() { return attractionName; }
    public void setAttractionName(String attractionName) { this.attractionName = attractionName; }
    public String getAttractionAddress() { return attractionAddress; }
    public void setAttractionAddress(String attractionAddress) { this.attractionAddress = attractionAddress; }
    public String getAttractionImage() { return attractionImage; }
    public void setAttractionImage(String attractionImage) { this.attractionImage = attractionImage; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
