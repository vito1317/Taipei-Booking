package com.taipeibooking.dto;

public class AttractionBasicDTO {
    private Long id;
    private String name;
    private String mrt;
    private String category;
    private String imageUrl;
    private String district;

    public AttractionBasicDTO() {
    }

    public AttractionBasicDTO(Long id, String name, String mrt, String category, String imageUrl, String district) {
        this.id = id;
        this.name = name;
        this.mrt = mrt;
        this.category = category;
        this.imageUrl = imageUrl;
        this.district = district;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMrt() { return mrt; }
    public void setMrt(String mrt) { this.mrt = mrt; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
}
