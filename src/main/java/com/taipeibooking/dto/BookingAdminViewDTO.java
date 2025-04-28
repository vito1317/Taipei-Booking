package com.taipeibooking.dto;

import java.time.LocalDateTime;
import java.util.Date;


public class BookingAdminViewDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long attractionId;
    private String attractionName;
    private String attractionAddress;
    private String attractionImage;
    private Date date;
    private String time;
    private Integer price;
    private String contactPhone;
    private String customerIdNumber;
    private LocalDateTime createdAt;


    public BookingAdminViewDTO() {
    }

    public BookingAdminViewDTO(Long id, Long userId, String userName, Long attractionId, String attractionName,
                               String attractionAddress, String attractionImage, Date date, String time, Integer price,
                               String contactPhone, String customerIdNumber, LocalDateTime createdAt ) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.attractionId = attractionId;
        this.attractionName = attractionName;
        this.attractionAddress = attractionAddress;
        this.attractionImage = attractionImage;
        this.date = date;
        this.time = time;
        this.price = price;
        this.contactPhone = contactPhone;
        this.customerIdNumber = customerIdNumber;
        this.createdAt = createdAt;

    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getAttractionId() {
        return attractionId;
    }

    public void setAttractionId(Long attractionId) {
        this.attractionId = attractionId;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public String getAttractionAddress() {
        return attractionAddress;
    }

    public void setAttractionAddress(String attractionAddress) {
        this.attractionAddress = attractionAddress;
    }

    public String getAttractionImage() {
        return attractionImage;
    }

    public void setAttractionImage(String attractionImage) {
        this.attractionImage = attractionImage;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getCustomerIdNumber() {
        return customerIdNumber;
    }

    public void setCustomerIdNumber(String customerIdNumber) {
        this.customerIdNumber = customerIdNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


}