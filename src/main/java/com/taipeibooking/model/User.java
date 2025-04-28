package com.taipeibooking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String role = "ROLE_USER";

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Booking> bookings = new ArrayList<>();


    public User() {
    }


    public User(String name, String username, String password, String role) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.setRole(role);
    }


     public User(String name, String username, String password) {
         this.name = name;
         this.username = username;
         this.password = password;

     }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if ("ROLE_USER".equals(role) || "ROLE_ADMIN".equals(role) || "ROLE_TRIP_MANAGER".equals(role)) {
             this.role = role;
        } else {
            this.role = "ROLE_USER";
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }



    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setUser(this);
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setUser(null);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
             createdAt = LocalDateTime.now();
        }

         if (role == null || role.isEmpty() || !List.of("ROLE_USER", "ROLE_ADMIN", "ROLE_TRIP_MANAGER").contains(role)) {
             role = "ROLE_USER";
         }
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", username='" + username + '\'' +
               ", role='" + role + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}
