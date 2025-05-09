package com.taipeibooking.dto;

public class AttractionAdminViewDTO {
    private Long id;
    private String name;
    private String category;
    private String mrt;
    private String district;
    private String description;
    private boolean isActive;

    public AttractionAdminViewDTO() {
    }

    public AttractionAdminViewDTO(Long id, String name, String category, String mrt, String district, String description, boolean isActive) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.mrt = mrt;
        this.district = district;
        this.description = description;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMrt() {
        return mrt;
    }

    public void setMrt(String mrt) {
        this.mrt = mrt;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
