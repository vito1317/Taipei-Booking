package com.taipeibooking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingResponseDTO {
    private Long id;
    private AttractionDetails attraction;
    private LocalDate date;
    private String time;
    private BigDecimal price;
    private String status;

    public static class AttractionDetails {
        private Long id;
        private String name;
        private String address;
        private String image;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AttractionDetails getAttraction() { return attraction; }
    public void setAttraction(AttractionDetails attraction) { this.attraction = attraction; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
