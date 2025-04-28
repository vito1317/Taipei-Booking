package com.taipeibooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;

public class BookingRequest {

    @NotNull(message = "景點 ID 不得為空")
    private Long attractionId;

    @NotNull(message = "預訂日期不得為空")
    private Date date;

    @NotBlank(message = "預訂時間不得為空")
    private String time;

    @NotNull(message = "價格不得為空")
    private Integer price;


    private String attractionName;
    private String attractionAddress;
    private String attractionImage;


    @NotBlank(message = "聯絡姓名不得為空")
    private String contactName;

    @NotBlank(message = "聯絡電話不得為空")
    private String contactPhone;


    @NotBlank(message = "身分證字號不得為空")
    @Size(min = 10, max = 10, message = "身分證字號格式應為 10 碼")
    private String customerIdNumber;




    public Long getAttractionId() {
        return attractionId;
    }

    public void setAttractionId(Long attractionId) {
        this.attractionId = attractionId;
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