package com.taipeibooking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingRequest {
    @NotNull(message = "景點ID不能為空")
    private Long attractionId;

    @NotNull(message = "日期不能為空")
    @FutureOrPresent(message = "預訂日期必須是今天或未來")
    private LocalDate date;

    @NotBlank(message = "時間不能為空")
    private String time;

    @NotNull(message = "價格不能為空")
    @Positive(message = "價格必須為正數")
    private BigDecimal price;

    @NotBlank(message = "聯絡人姓名不能為空")
    private String contactName;

    @NotBlank(message = "聯絡人電話不能為空")
    private String contactPhone;
    
    private String customerIdNumber;

    public BookingRequest() {
    }

    public BookingRequest(Long attractionId, LocalDate date, String time, BigDecimal price, String contactName, String contactPhone, String customerIdNumber) {
        this.attractionId = attractionId;
        this.date = date;
        this.time = time;
        this.price = price;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.customerIdNumber = customerIdNumber;
    }

    public Long getAttractionId() {
        return attractionId;
    }

    public void setAttractionId(Long attractionId) {
        this.attractionId = attractionId;
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

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
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
}
