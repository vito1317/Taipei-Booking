# Taipei Booking

This is a Taipei attraction booking system developed using Java Spring Boot. It allows users to browse various attractions in Taipei City, register/log in, and book trips.

## Table of Contents

* [Features](#features)
* [Tech Stack](#tech-stack)
* [Data Models](#data-models)
* [Database Schema & Sample Data](#database-schema--sample-data)
* [Prerequisites](#prerequisites)
* [Getting Started](#getting-started)
* [API Endpoints](#api-endpoints)
* [Contributing](#contributing)
* [License](#license)

## Features

* **Attraction Browsing:** View a list of attractions in Taipei City, including images, names, categories, and MRT station information. Supports searching by keyword and pagination.
* **User Authentication:** Secure user registration and login using JWT (JSON Web Tokens).
* **Trip Booking:** Logged-in users can create new bookings by selecting an attraction, date, time, and price.
* **Booking Management:** Users can view their upcoming (unpaid/unconfirmed) booking details.
* **Admin Panel (Optional):** An administrative interface for managing attractions, users, bookings, and viewing statistics/trends.

## Tech Stack

* **Backend:**
    * Java 17+ (or specify your version)
    * Spring Boot (specify version from `pom.xml` if desired)
    * Spring Security (for authentication/authorization with JWT)
    * Spring Data JPA (for database interaction)
    * Maven (for dependency management and build)
* **Frontend:**
    * HTML5
    * CSS3
    * Vanilla JavaScript
* **Database:**
    * **(Please specify your database, e.g., MySQL 8.0, PostgreSQL 14, H2)** - *Crucial for others to set up.*
* **Other Libraries:**
    * `jjwt` (for JWT handling)
    * (Add any other significant libraries from your `pom.xml`)

## Data Models

This project uses the following core data models (JPA Entities):

* **`User` (`User.java`)**: Represents a user account.
    * `id` (Long): Primary key.
    * `username` (String): User's email address (used for login, unique).
    * `password` (String): Hashed password (ignored in JSON responses).
    * `name` (String): User's display name.
    * `role` (String): User role (e.g., `ROLE_USER`, `ROLE_ADMIN`, `ROLE_TRIP_MANAGER`). Defaults to `ROLE_USER`.
    * `createdAt` (LocalDateTime): Timestamp of account creation.
    * `bookings` (List<Booking>): Association with the user's bookings (lazy-loaded, ignored in JSON).

* **`Attraction` (`Attraction.java`)**: Represents a tourist attraction.
    * `id` (Long): Primary key.
    * `name` (String): Name of the attraction.
    * `description` (String): Detailed description (TEXT).
    * `address` (String): Address of the attraction.
    * `imageUrl` (String): URL of the attraction's image.
    * `mrt` (String): Nearby MRT station.
    * `category` (String): Category of the attraction.
    * `isActive` (Boolean): Whether the attraction is currently active/bookable. Defaults to `true`.
    * `createdAt` (LocalDateTime): Timestamp of creation.
    * `updatedAt` (LocalDateTime): Timestamp of the last update.

* **`Booking` (`Booking.java`)**: Represents a booking made by a user for an attraction.
    * `id` (Long): Primary key.
    * `user` (User): Association with the user who made the booking (lazy-loaded, ignored in JSON).
    * `customerName` (String): Name of the customer making the booking (might differ from user's name).
    * `attractionId` (Long): Foreign key referencing the booked `Attraction`.
    * `attractionName` (String): Name of the booked attraction (denormalized).
    * `attractionAddress` (String): Address of the booked attraction (denormalized).
    * `attractionImage` (String): Image URL of the booked attraction (denormalized).
    * `date` (Date): Date of the booking.
    * `time` (String): Time slot of the booking (e.g., "morning", "afternoon").
    * `price` (Integer): Price of the booking.
    * `customerIdNumber` (String): Customer's ID number (masked in `toString`).
    * `contactPhone` (String): Customer's contact phone number.
    * `status` (String): Status of the booking (e.g., "UNPAID", "PAID", "CANCELLED").
    * `createdAt` (LocalDateTime): Timestamp of booking creation.

## Database Schema & Sample Data

Below is the schema for the `attractions` table and some sample data (assuming MySQL). You'll also need tables for `users` and `bookings` based on the Data Models section.

```sql
-- Schema for the attractions table
CREATE TABLE attractions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address VARCHAR(255),
    image_url VARCHAR(512),
    mrt VARCHAR(100),
    category VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_attractions_name (name),
    INDEX idx_attractions_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample data for the attractions table
INSERT INTO attractions (name, image_url, is_active) VALUES
('台北 101', 'image/Taipei101.jpg', TRUE),
('九份', 'image/9.jpg', TRUE),
('西門町', 'image/西門町.jpg', TRUE),
('士林夜市', 'image/士林夜市.jpg', TRUE),
('大安森林公園', 'image/大安森林公園.jpg', TRUE),
('中正紀念堂', 'image/中正紀念堂.jpg', TRUE),
('兒童新樂園', 'image/兒童新樂園.jpg', TRUE),
('松山文創園區', 'image/松山文創園區.jpg', TRUE),
('林安泰古厝', 'image/林安泰古厝.jpg', TRUE),
('故宮', 'image/故宮.jpg', TRUE),
('美麗華', 'image/美麗華.jpg', TRUE),
('國立國父紀念館', 'image/國立國父紀念館.jpg', TRUE),
('華山', 'image/華山.jpg', TRUE),
('陽明山國家公園', 'image/陽明山國家公園.jpg', TRUE),
('臺北市立動物園', 'image/臺北市立動物園.jpg', TRUE),
('臺北植物園', 'image/臺北植物園.jpg', TRUE),
('臺灣博物館', 'image/臺灣博物館.jpg', TRUE),
('艋舺龍山寺', 'image/艋舺龍山寺.jpg', TRUE),
('貓空', 'image/貓空.jpg', TRUE),
('饒河夜市', 'image/饒河夜市.jpg', TRUE);

-- Note: You will also need CREATE TABLE statements for 'users' and 'bookings'.
-- The structure should align with the User.java and Booking.java entities.
-- Example for users (adjust based on User.java):
-- CREATE TABLE users (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     username VARCHAR(100) NOT NULL UNIQUE,
--     password VARCHAR(255) NOT NULL,
--     name VARCHAR(50) NOT NULL,
--     role VARCHAR(20) DEFAULT 'ROLE_USER' NOT NULL,
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Example for bookings (adjust based on Booking.java):
-- CREATE TABLE bookings (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     user_id BIGINT NOT NULL,
--     customer_name VARCHAR(255) NOT NULL,
--     attraction_id BIGINT NOT NULL,
--     attraction_name VARCHAR(255) NOT NULL,
--     attraction_address VARCHAR(255),
--     attraction_image VARCHAR(512),
--     booking_date DATE NOT NULL,
--     time VARCHAR(255) NOT NULL,
--     price INT NOT NULL,
--     customer_id_number VARCHAR(20) NOT NULL,
--     contact_phone VARCHAR(20) NOT NULL,
--     status VARCHAR(20) NOT NULL,
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     FOREIGN KEY (user_id) REFERENCES users(id),
--     FOREIGN KEY (attraction_id) REFERENCES attractions(id)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
