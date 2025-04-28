package com.taipeibooking.dto;

public class TrendDataPointDTO {
    private String label; 
    private Long value; 

    public TrendDataPointDTO(String label, Long value) {
        this.label = label;
        this.value = value;
    }
     public TrendDataPointDTO() {}

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Long getValue() { return value; }
    public void setValue(Long value) { this.value = value; }
}
