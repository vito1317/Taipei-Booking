package com.taipeibooking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "attraction_id", nullable = false)
    private Long attractionId;

    @Column(name = "trip_name", nullable = false)
    private String attractionName;

    @Column(name = "attraction_address")
    private String attractionAddress;

    @Column(name = "attraction_image", length = 512)
    private String attractionImage;

    @Temporal(TemporalType.DATE)
    @Column(name = "booking_date", nullable = false)
    private Date date;

    @Column(nullable = false)
    private String time;

    @Column(nullable = false)
    private Integer price;

    @NotBlank
    @Size(min = 10, max = 10)
    @Column(name = "customer_id_number", nullable = false, length = 20)
    private String customerIdNumber;

    @NotBlank
    @Column(name = "customer_phone", nullable = false, length = 20)
    private String contactPhone;


    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createdAt;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public String getCustomerIdNumber() {
        return customerIdNumber;
    }

    public void setCustomerIdNumber(String customerIdNumber) {
        this.customerIdNumber = customerIdNumber;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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

    @Override
    public String toString() {
        return "Booking{" +
               "id=" + id +
               ", userId=" + (user != null ? user.getId() : null) +
               ", customerName='" + customerName + '\'' +
               ", attractionId=" + attractionId +
               ", attractionName='" + attractionName + '\'' +
               ", date=" + date +
               ", time='" + time + '\'' +
               ", price=" + price +

               ", status='" + status + '\'' +
               ", customerIdNumber='[PROTECTED]'" +
               ", contactPhone='" + contactPhone + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}