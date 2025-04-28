package com.taipeibooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateAttractionRequestDTO {
    @NotBlank(message = "景點名稱不可為空")
    @Size(max = 255)
    private String name;
    private String description;
    @Size(max = 255)
    private String address;
    @Size(max = 512)
    private String imageUrl;
    @Size(max = 100)
    private String mrt;
    @Size(max = 100)
    private String category;
    @NotNull(message = "啟用狀態不可為空")
    private Boolean isActive = true;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getMrt() { return mrt; }
    public void setMrt(String mrt) { this.mrt = mrt; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }
}
