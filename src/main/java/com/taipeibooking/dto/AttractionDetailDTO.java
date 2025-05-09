package com.taipeibooking.dto;

import java.util.List;

public class AttractionDetailDTO {
    private Long id;
    private String name;
    private String description;
    private String address;
    private Double lat;
    private Double lng;
    private String transport;
    private String mrt;
    private String category;
    private String district;
    private String imageUrl;

    public AttractionDetailDTO() {
    }

    public AttractionDetailDTO(Long id, String name, String description, String address, Double lat, Double lng, String transport, String mrt, String category, String district, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.transport = transport;
        this.mrt = mrt;
        this.category = category;
        this.district = district;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public String getTransport() { return transport; }
    public void setTransport(String transport) { this.transport = transport; }
    public String getMrt() { return mrt; }
    public void setMrt(String mrt) { this.mrt = mrt; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

}
