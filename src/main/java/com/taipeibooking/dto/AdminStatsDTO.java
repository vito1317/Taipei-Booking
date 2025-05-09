package com.taipeibooking.dto;

import java.util.List;
import java.util.Map;

public class AdminStatsDTO {
    private long totalGeneralUsers;
    private long totalBookings;
    private long totalAttractions;
    private List<TrendDataPointDTO> bookingTrends;
    private List<TrendDataPointDTO> userRegistrationTrends;
    private Map<String, Long> ageDistribution;
    private Map<String, Long> genderDistribution;

    public AdminStatsDTO() {
    }

    public AdminStatsDTO(long totalGeneralUsers, long totalBookings, long totalAttractions,
                         List<TrendDataPointDTO> bookingTrends, List<TrendDataPointDTO> userRegistrationTrends,
                         Map<String, Long> ageDistribution, Map<String, Long> genderDistribution) {
        this.totalGeneralUsers = totalGeneralUsers;
        this.totalBookings = totalBookings;
        this.totalAttractions = totalAttractions;
        this.bookingTrends = bookingTrends;
        this.userRegistrationTrends = userRegistrationTrends;
        this.ageDistribution = ageDistribution;
        this.genderDistribution = genderDistribution;
    }

    public long getTotalGeneralUsers() {
        return totalGeneralUsers;
    }

    public void setTotalGeneralUsers(long totalGeneralUsers) {
        this.totalGeneralUsers = totalGeneralUsers;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public long getTotalAttractions() {
        return totalAttractions;
    }

    public void setTotalAttractions(long totalAttractions) {
        this.totalAttractions = totalAttractions;
    }

    public List<TrendDataPointDTO> getBookingTrends() {
        return bookingTrends;
    }

    public void setBookingTrends(List<TrendDataPointDTO> bookingTrends) {
        this.bookingTrends = bookingTrends;
    }

    public List<TrendDataPointDTO> getUserRegistrationTrends() {
        return userRegistrationTrends;
    }

    public void setUserRegistrationTrends(List<TrendDataPointDTO> userRegistrationTrends) {
        this.userRegistrationTrends = userRegistrationTrends;
    }

    public Map<String, Long> getAgeDistribution() {
        return ageDistribution;
    }

    public void setAgeDistribution(Map<String, Long> ageDistribution) {
        this.ageDistribution = ageDistribution;
    }

    public Map<String, Long> getGenderDistribution() {
        return genderDistribution;
    }

    public void setGenderDistribution(Map<String, Long> genderDistribution) {
        this.genderDistribution = genderDistribution;
    }
}
