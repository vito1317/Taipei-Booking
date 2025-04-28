package com.taipeibooking.dto;

import java.math.BigDecimal;

public class AdminStatsDTO {
    private long totalUsers;
    private long totalBookings;
    private BigDecimal totalRevenue;


    public AdminStatsDTO(long totalUsers, long totalBookings, BigDecimal totalRevenue) {
         this.totalUsers = totalUsers;
         this.totalBookings = totalBookings;
         this.totalRevenue = totalRevenue;
     }


     public AdminStatsDTO() {}


    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }


    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
}