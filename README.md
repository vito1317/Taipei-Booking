# Taipei Booking

This is a Taipei attraction booking system developed using Java Spring Boot. It allows users to browse various attractions in Taipei City, register/log in, and book trips.

## Table of Contents

- [Taipei Booking](#taipei-booking)
  - [Table of Contents](#table-of-contents)
  - [Features](#features)
  - [Tech Stack](#tech-stack)
  - [Data Models](#data-models)
  - [Database Schema \& Sample Data](#database-schema--sample-data)
  - [Prerequisites](#prerequisites)
  - [Getting Started](#getting-started)
  - [API Endpoints](#api-endpoints)
  - [Contributing](#contributing)
  - [License](#license)

## Features

* **Attraction Browsing:** View a list of attractions in Taipei City, including images, names, categories, and MRT station information. Supports searching by keyword and pagination.
* **User Authentication:** Secure user registration and login using JWT (JSON Web Tokens).
* **Trip Booking:** Logged-in users can create new bookings by selecting an attraction, date, time, and price.
* **Booking Management:** Users can view their upcoming (unpaid/unconfirmed) booking details.
* **Admin Panel:** An administrative interface for managing attractions, users, bookings, and viewing statistics/trends.

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
    * `role` (String): User role (e.g., `USER`, `ADMIN`, `TRIP_MANAGER`). Defaults to `USER`.
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
--     role VARCHAR(20) DEFAULT 'USER' NOT NULL,
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

```

## Prerequisites

Before you begin, ensure you have met the following requirements:

  * You have installed Java Development Kit (JDK) version 17 or higher.
  * You have installed Maven.
  * You have installed and configured your chosen database (e.g., MySQL, PostgreSQL).
  * You have a tool like `git` installed to clone the repository.

## Getting Started

Follow these steps to get the project running locally:

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/vito1317/taipei-booking.git # Replace with your actual repo URL
    cd taipei-booking
    ```

2.  **Create the Database:**

      * **Important:** Before configuring the application, you need to create an empty database in your database server (e.g., MySQL, PostgreSQL).
      * The database should be named `taipei_db` (to match the default configuration below).
      * Example command for MySQL:
        ```sql
        CREATE DATABASE taipei_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
        ```
      * You do **not** need to create the tables manually. Spring Boot with JPA will automatically create or update the tables based on your entity classes (`User.java`, `Attraction.java`, `Booking.java`) when the application starts, thanks to the `spring.jpa.hibernate.ddl-auto=update` setting in `application.properties`.

3.  **Configure the Application:**

      * Open the `src/main/resources/application.properties` file.
      * Update the following properties with your database connection details:
        ```properties
        spring.datasource.url=jdbc:mysql://localhost:3306/taipei_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true # Ensure 'taipei_db' matches the database you created
        spring.datasource.username=your_db_username
        spring.datasource.password=your_db_password
        spring.jpa.hibernate.ddl-auto=update # This allows Spring Boot to manage the schema
        ```

4.  **Configure JWT Secret:**

      * Set the JWT secret key in `application.properties`:
        ```properties
        jwt.secret=YourVeryStrongAndSecretKeyHere # Replace with a strong, secure key
        jwt.expiration=86400000 # Token expiration time in milliseconds (e.g., 24 hours)
        ```
      * **Security Note:** For production, it's highly recommended to use environment variables or a configuration server for sensitive data like database passwords and JWT secrets, rather than hardcoding them in `application.properties`.

5.  **Build the project:**

    ```bash
    mvn clean install
    ```

6.  **Run the application:**

    ```bash
    mvn spring-boot:run
    ```

    Alternatively, you can run the packaged JAR file:

    ```bash
    java -jar target/taipei-booking-0.0.1-SNAPSHOT.jar # Adjust JAR filename if necessary
    ```

7.  **Access the application:**
    Open your web browser and navigate to `http://localhost:8080` (or the port configured in `application.properties`).

      * You can optionally run the SQL `INSERT` statements from the [Database Schema & Sample Data](#database-schema--sample-data) section in your database client to populate the `attractions` table with initial data.

## API Endpoints

Here are the main API endpoints provided by the application:

**Authentication (`/api/user`)**

  * `POST /api/user/register`: Register a new user.
      * Body: `RegisterRequest` (name, email, password)
  * `POST /api/user/login`: Log in a user.
      * Body: `LoginRequest` (email, password)
      * Returns: JWT token and user role.
  * `GET /api/user/auth`: Get the currently authenticated user's information.
      * Requires: Valid JWT in Authorization header.
      * Returns: User details (id, name, email, role) or null if not authenticated.

**Attractions (`/api`)**

  * `GET /api/attractions`: Get a list of active attractions for the frontend.
      * Returns: List of `AttractionBasicDTO`.

**Bookings (`/api`)**

  * `POST /api/booking`: Create a new booking.
      * Requires: Valid JWT.
      * Body: `BookingRequest` (attractionId, date, time, price)
      * Returns: `{ "ok": true, "bookingId": ... }`
  * `GET /api/booking`: Get the current user's bookings.
      * Requires: Valid JWT.
      * Returns: List of `Booking` objects for the user.
  * `DELETE /api/booking/{bookingId}`: Delete a specific booking for the current user.
      * Requires: Valid JWT.
      * Allowed only for bookings in certain states (e.g., UNPAID).
      * Returns: `{ "ok": true }` on success.
  * `POST /api/booking/{bookingId}/pay`: Mark a booking as paid (simulated payment).
      * Requires: Valid JWT.
      * Allowed only for bookings in the UNPAID state.
      * Returns: `{ "ok": true, "message": "...", "bookingId": ..., "newStatus": "PAID" }`

**Admin - Users (`/api/admin/users`)** (Requires ADMIN role)

  * `POST /users`: Create a new user (by admin).
      * Body: `AdminCreateUserRequestDTO`
      * Returns: `UserAdminViewDTO` of the created user.
  * `GET /users`: Get a paginated list of all users.
      * Supports pagination parameters (e.g., `?page=0&size=10&sort=id,asc`).
      * Returns: `Page<UserAdminViewDTO>`.
  * `GET /users/{userId}`: Get details of a specific user.
      * Returns: `UserAdminViewDTO`.
  * `PUT /users/{userId}`: Update a specific user's details (by admin).
      * Body: `AdminUpdateUserRequestDTO`
      * Returns: `UserAdminViewDTO` of the updated user.
  * `DELETE /users/{userId}`: Delete a specific user.
      * Returns: HTTP 204 No Content on success.

**Admin - Bookings (`/api/admin/bookings`)** (Requires ADMIN role)

  * `GET /bookings`: Get a paginated list of all bookings.
      * Supports pagination parameters (e.g., `?page=0&size=20&sort=createdAt,desc`).
      * Returns: `Page<Booking>`.
  * `GET /bookings/{bookingId}`: Get detailed information about a specific booking.
      * Returns: `AdminBookingDetailDTO`.
  * `DELETE /bookings/{bookingId}`: Cancel a specific booking (by admin).
      * Returns: HTTP 204 No Content on success.

**Admin - Attractions (`/api/admin/attractions`)** (Requires ADMIN or TRIP\_MANAGER role)

  * `POST /attractions`: Create a new attraction.
      * Body: `CreateAttractionRequestDTO`
      * Returns: `AttractionAdminViewDTO` of the created attraction.
  * `GET /attractions`: Get a paginated list of all attractions.
      * Supports pagination parameters (e.g., `?page=0&size=10&sort=id,asc`).
      * Returns: `Page<AttractionAdminViewDTO>`.
  * `GET /attractions/{attractionId}`: Get details of a specific attraction.
      * Returns: `AttractionAdminViewDTO`.
  * `PUT /attractions/{attractionId}`: Update details of a specific attraction.
      * Body: `UpdateAttractionRequestDTO`
      * Returns: `AttractionAdminViewDTO` of the updated attraction.
  * `PATCH /attractions/{attractionId}/status`: Update the active status of an attraction.
      * Body: `{ "isActive": boolean }`
      * Returns: `AttractionAdminViewDTO` of the updated attraction.
  * `DELETE /attractions/{attractionId}`: Delete a specific attraction.
      * Returns: HTTP 204 No Content on success.

**Admin - Statistics & Trends (`/api/admin`)** (Requires ADMIN role)

  * `GET /stats`: Get dashboard statistics (e.g., total users, bookings).
      * Returns: `AdminStatsDTO`.
  * `GET /trends/registrations`: Get data points for user registration trends.
      * Returns: `List<TrendDataPointDTO>`.
  * `GET /trends/bookings`: Get data points for booking trends.
      * Returns: `List<TrendDataPointDTO>`.

## Contributing

Contributions are welcome\! If you'd like to contribute, please follow these steps:

1.  Fork the repository.
2.  Create a new branch (`git checkout -b feature/your-feature-name`).
3.  Make your changes.
4.  Commit your changes (`git commit -m 'Add some feature'`).
5.  Push to the branch (`git push origin feature/your-feature-name`).
6.  Open a Pull Request.

Please ensure your code adheres to the existing style and includes tests where appropriate.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details .
