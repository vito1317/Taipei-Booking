
# Taipei Booking

This is a Taipei attraction booking system developed using Java Spring Boot. It allows users to browse various attractions in Taipei City, register/log in, and book trips.

## Table of Contents

  - [Taipei Booking](#taipei-booking)
      - [Table of Contents](#table-of-contents)
      - [Features](#features)
      - [Tech Stack](#tech-stack)
      - [Data Models](#data-models)
      - [Database Schema & Sample Data](#database-schema--sample-data)
      - [Prerequisites](#prerequisites)
      - [Getting Started](#getting-started)
      - [API Endpoints](#api-endpoints)
      - [Contributing](#contributing)
      - [License](#license)

## Features

  * **Attraction Browse:** View a list of attractions in Taipei City, including images, names, categories, and MRT station information. Supports searching by keyword and pagination.
  * **User Authentication:** Secure user registration and login using JWT (JSON Web Tokens).
  * **Trip Booking:** Logged-in users can create new bookings by selecting an attraction, date, time, and price.
  * **Booking Management:** Users can view their upcoming and past booking details.
  * **Admin Panel:** An administrative interface for managing attractions, users, bookings, and viewing statistics/trends.

## Tech Stack

  * **Backend:**
      * Java 17+
      * Spring Boot 3.x.x (specify your version from `pom.xml`)
      * Spring Security (for authentication/authorization with JWT)
      * Spring Data JPA (for database interaction)
      * Maven (for dependency management and build)
  * **Frontend:**
      * HTML5
      * CSS3
      * Vanilla JavaScript
  * **Database:**
      * **MySQL 8.0** (or specify your version, the provided SQL dump is for MariaDB which is largely compatible)
  * **Other Libraries:**
      * `io.jsonwebtoken/jjwt` (for JWT handling - specify version)
      * `org.slf4j/slf4j-api` (Logging)
      * `jakarta.validation/jakarta.validation-api` (Validation)

## Data Models

This project uses the following core data models (JPA Entities):

  * **`User` (`User.java`)**: Represents a user account.

      * `id` (Long): Primary key.
      * `name` (String): User's display name.
      * `email` (String): User's email address (used for login, unique).
      * `username` (String): Also set to user's email during registration; `email` is the primary identifier for authentication.
      * `password` (String): Hashed password.
      * `role` (String): User role (e.g., `USER`, `ADMIN`, `TRIP_MANAGER`). Defaults to `USER`.
      * `age` (Integer, optional): User's age.
      * `gender` (String, optional): User's gender.
      * `createdAt` (LocalDateTime): Timestamp of account creation.
      * `updatedAt` (LocalDateTime): Timestamp of the last update.
      * `bookings` (List\<Booking\>): Association with the user's bookings.

  * **`Attraction` (`Attraction.java`)**: Represents a tourist attraction.

      * `id` (Long): Primary key.
      * `name` (String): Name of the attraction.
      * `description` (String): Detailed description (TEXT).
      * `address` (String): Address of the attraction.
      * `lat` (Double): Latitude of the attraction.
      * `lng` (Double): Longitude of the attraction.
      * `transport` (String): Transportation information.
      * `mrt` (String): Nearby MRT station.
      * `category` (String): Category of the attraction.
      * `district` (String): District of the attraction.
      * `isActive` (Boolean): Whether the attraction is currently active/bookable. Defaults to `true`.
      * `imageUrl` (String): URL of the attraction's image (maps to `image_url` in DB).
      * `bookings` (List\<Booking\>): Association with bookings for this attraction.

  * **`Booking` (`Booking.java`)**: Represents a booking made by a user for an attraction.

      * `id` (Long): Primary key.
      * `user` (User): Association with the user who made the booking.
      * `attraction` (Attraction): Association with the booked attraction (can be null if attraction is deleted).
      * `attractionIdOriginal` (Long): Stores the original ID of the attraction, in case the attraction entity gets deleted.
      * `attractionName` (String): Name of the booked attraction (denormalized, maps to `trip_name` in DB).
      * `attractionImage` (String): Image URL of the booked attraction (denormalized).
      * `date` (LocalDate): Date of the booking (maps to `booking_date` in DB).
      * `time` (String): Time slot of the booking (e.g., "morning", "afternoon").
      * `price` (BigDecimal): Price of the booking.
      * `status` (String): Status of the booking (e.g., "PENDING", "PAID", "CANCELLED").
      * `customerName` (String): Name of the customer for the booking.
      * `contactPhone` (String): Customer's contact phone number (maps to `customer_phone` in DB).
      * `customerIdNumber` (String, optional): Customer's ID number.
      * `createdAt` (LocalDateTime): Timestamp of booking creation.
      * `updatedAt` (LocalDateTime): Timestamp of the last update.

## Database Schema & Sample Data

Below is the schema for the `attractions`, `users`, and `bookings` tables (assuming MySQL/MariaDB).

```sql
-- Schema for the attractions table
CREATE TABLE `attractions` (
  `id` bigint(20) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `district` varchar(255) DEFAULT NULL,
  `image_url` text DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `lat` double DEFAULT NULL,
  `lng` double DEFAULT NULL,
  `mrt` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `transport` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample data for the attractions table
INSERT INTO `attractions` (`id`, `address`, `category`, `description`, `district`, `image_url`, `is_active`, `lat`, `lng`, `mrt`, `name`, `transport`) VALUES
(1, '台北市信義區信義路五段7號', '地標建築', '台北101是台灣最著名的地標之一，曾經是世界最高建築。提供觀景台、購物中心及美食餐廳。', '信義區', 'image/101.jpg', b'1', 25.033961, 121.564468, '台北101/世貿站', '台北 101', '搭乘捷運淡水信義線至台北101/世貿站，步行即可抵達。'),
(2, '新北市瑞芳區基山街', '老街山城', '九份是一座充滿懷舊氛圍的山城，以其獨特的階梯、茶館和芋圓聞名。夜晚紅燈籠亮起時景色迷人。', '瑞芳區', 'image/9.jpg', b'1', 25.109458, 121.844902, '瑞芳車站 (需轉乘公車)', '九份', '從台北搭火車至瑞芳車站，再轉乘基隆客運1062、788號公車前往九份老街。'),
(3, '台北市萬華區中華路一段', '商圈夜市', '西門町是台北年輕人的潮流聚集地，擁有眾多商店、電影院、餐廳和小吃攤，充滿活力。', '萬華區', 'image/西門町.jpg', b'1', 25.0423, 121.5081, '西門站', '西門町', '搭乘捷運板南線或松山新店線至西門站即可抵達。'),
(4, '台北市士林區大東路、基河路周邊', '商圈夜市', '士林夜市是台北規模最大的夜市之一，提供各式各樣的台灣小吃、服飾和娛樂活動。', '士林區', 'image/士林夜市.jpg', b'1', 25.0871, 121.5245, '劍潭站', '士林夜市', '搭乘捷運淡水信義線至劍潭站1號出口，步行即可抵達。'),
(5, '台北市大安區新生南路二段1號', '公園綠地', '大安森林公園被譽為「台北市之肺」，是市中心最大的公園，提供廣闊的綠地和多元的休閒設施。', '大安區', 'image/大安森林公園.jpg', b'1', 25.03, 121.535, '大安森林公園站', '大安森林公園', '搭乘捷運淡水信義線至大安森林公園站即可抵達。'),
(6, '台北市中正區中山南路21號', '歷史古蹟', '中正紀念堂是為紀念中華民國前總統蔣中正而興建的建築，主建築氣勢宏偉，廣場寬闊，常有藝文活動。', '中正區', 'image/中正紀念堂.jpg', b'1', 25.0349, 121.5199, '中正紀念堂站', '中正紀念堂', '搭乘捷運淡水信義線或松山新店線至中正紀念堂站即可抵達。'),
(7, '台北市士林區承德路五段55號', '主題樂園', '兒童新樂園是專為兒童設計的遊樂園，提供多樣化的遊樂設施和表演，適合親子同遊。', '士林區', 'image/兒童新樂園.jpg', b'1', 25.0956, 121.5171, '劍潭站或士林站 (需轉乘公車)', '兒童新樂園', '搭乘捷運至劍潭站或士林站，再轉乘接駁公車或市區公車前往。'),
(8, '台北市信義區光復南路133號', '文創園區', '松山文創園區前身為松山菸廠，現已轉型為文創產業的基地，常有展覽、市集和特色商店。', '信義區', 'image/松山文創園區.jpg', b'1', 25.0439, 121.5584, '國父紀念館站', '松山文創園區', '搭乘捷運板南線至國父紀念館站5號出口，步行約10分鐘。'),
(9, '台北市中山區濱江街5號', '歷史古蹟', '林安泰古厝是台北市現存古宅中年代最久、保存最完整的傳統閩南式建築之一。', '中山區', 'image/林安泰古厝.jpg', b'1', 25.0728, 121.5233, '圓山站 (需轉乘公車或步行)', '林安泰古厝', '搭乘捷運淡水信義線至圓山站，轉乘公車或步行約20-25分鐘。'),
(10, '台北市士林區至善路二段221號', '博物館', '國立故宮博物院收藏了豐富的中國古代文物與藝術品，是世界知名的博物館之一。', '士林區', 'image/故宮.jpg', b'1', 25.1022, 121.5485, '士林站 (需轉乘公車)', '故宮', '搭乘捷運淡水信義線至士林站，轉乘公車紅30、255、304等前往故宮博物院。'),
(11, '台北市中山區敬業三路20號', '購物商場', '美麗華百樂園擁有大型摩天輪、購物商場、電影院及餐廳，是休閒娛樂的好去處。', '中山區', 'image/美麗華.jpg', b'1', 25.0836, 121.5574, '劍南路站', '美麗華', '搭乘捷運文湖線至劍南路站即可抵達。'),
(12, '台北市信義區仁愛路四段505號', '紀念館', '國立國父紀念館為紀念孫中山先生而建，建築宏偉，內部有展覽廳及表演場地。', '信義區', 'image/國立國父紀念館.jpg', b'1', 25.0392, 121.5598, '國父紀念館站', '國立國父紀念館', '搭乘捷運板南線至國父紀念館站4號出口。'),
(13, '台北市中正區八德路一段1號', '文創園區', '華山1914文化創意產業園區，前身為酒廠，現為舉辦展覽、表演、市集及文創活動的熱門場所。', '中正區', 'image/華山.jpg', b'1', 25.0449, 121.5298, '忠孝新生站', '華山', '搭乘捷運板南線或中和新蘆線至忠孝新生站1號出口，步行約5分鐘。'),
(14, '台北市北投區竹子湖路1-20號', '國家公園', '陽明山國家公園以火山地形、溫泉及豐富的自然生態聞名，四季皆有不同花卉可欣賞。', '北投區', 'image/陽明山國家公園.jpg', b'1', 25.1598, 121.541, '劍潭站 (需轉乘公車)', '陽明山國家公園', '搭乘捷運至劍潭站，轉乘公車紅5或260至陽明山總站。'),
(15, '台北市文山區新光路二段30號', '動物園', '臺北市立動物園是台灣規模最大的動物園，擁有來自世界各地的多種動物，適合全家大小。', '文山區', 'image/臺北市立動物園.jpg', b'1', 24.9986, 121.5808, '動物園站', '臺北市立動物園', '搭乘捷運文湖線至動物園站即可抵達。'),
(16, '台北市中正區南海路53號', '公園綠地', '臺北植物園內有多樣的植物種類，並設有荷花池、溫室等，是都市中的一片綠洲。', '中正區', 'image/臺北植物園.jpg', b'1', 25.031, 121.513, '小南門站', '臺北植物園', '搭乘捷運松山新店線至小南門站3號出口，步行約5分鐘。'),
(17, '台北市中正區襄陽路2號', '博物館', '國立臺灣博物館是台灣歷史最悠久的博物館，主要展出台灣的自然史與人文發展。', '中正區', 'image/臺灣博物館.jpg', b'1', 25.0445, 121.515, '台大醫院站', '臺灣博物館', '搭乘捷運淡水信義線至台大醫院站4號出口，步行約5分鐘。'),
(18, '台北市萬華區廣州街211號', '宗教聖地', '艋舺龍山寺是台北市著名的古剎，香火鼎盛，建築精美，為國家二級古蹟。', '萬華區', 'image/艋舺龍山寺.jpg', b'1', 25.0368, 121.5, '龍山寺站', '艋舺龍山寺', '搭乘捷運板南線至龍山寺站即可抵達。'),
(19, '台北市文山區指南路三段', '自然景觀', '貓空以茶園風光及纜車聞名，可以品茗、欣賞夜景，是遠離塵囂的好去處。', '文山區', 'image/貓空.jpg', b'1', 24.9688, 121.5899, '動物園站 (轉乘貓空纜車)', '貓空', '搭乘捷運文湖線至動物園站，轉乘貓空纜車至貓空站。'),
(20, '台北市松山區饒河街', '商圈夜市', '饒河街觀光夜市是台北市著名的夜市之一，全長約600公尺，美食眾多，如胡椒餅、藥燉排骨等。', '松山區', 'image/饒河夜市.jpg', b'1', 25.0501, 121.573, '松山站', '饒河夜市', '搭乘捷運松山新店線至松山站1號或5號出口，或搭乘火車至松山車站。');

-- Schema for the users table (align with User.java)
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(255) NOT NULL UNIQUE, -- Set to email value on registration
    `name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(50) DEFAULT 'USER' NOT NULL,
    `age` INT,
    `gender` VARCHAR(20),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Schema for the bookings table (align with Booking.java)
CREATE TABLE `bookings` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `attraction_id` BIGINT, -- Can be null if attraction is deleted
    `attraction_id_original` BIGINT,
    `trip_name` VARCHAR(255) NOT NULL, -- Corresponds to attractionName in Booking.java
    `attraction_image` TEXT,
    `booking_date` DATE NOT NULL, -- Corresponds to date in Booking.java
    `time` VARCHAR(50) NOT NULL,
    `price` DECIMAL(10, 2) NOT NULL,
    `status` VARCHAR(50) NOT NULL,
    `customer_name` VARCHAR(255) NOT NULL,
    `customer_phone` VARCHAR(50) NOT NULL, -- Corresponds to contactPhone in Booking.java
    `customer_id_number` VARCHAR(50),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`attraction_id`) REFERENCES `attractions`(`id`) ON DELETE SET NULL,
    INDEX idx_bookings_user_id (`user_id`),
    INDEX idx_bookings_attraction_id (`attraction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## Prerequisites

Before you begin, ensure you have met the following requirements:

  * You have installed Java Development Kit (JDK) version 17 or higher.
  * You have installed Maven.
  * You have installed and configured your chosen database (e.g., MySQL 8.0 or MariaDB).
  * You have a tool like `git` installed to clone the repository.

## Getting Started

Follow these steps to get the project running locally:

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/vito1317/taipei-booking.git # Replace with your actual repo URL
    cd taipei-booking
    ```

2.  **Create the Database:**

      * **Important:** Before configuring the application, you need to create an empty database in your database server.
      * The database should be named `taipei_db` (to match the default configuration below).
      * Example command for MySQL/MariaDB:
        ```sql
        CREATE DATABASE taipei_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
        ```
      * If `spring.jpa.hibernate.ddl-auto` is set to `update` or `create` in `application.properties`, Spring Boot will attempt to manage the schema. For a clean setup, it's often best to create the database shell first.

3.  **Configure the Application:**

      * Open the `src/main/resources/application.properties` file.
      * Update the following properties with your database connection details:
        ```properties
        spring.datasource.url=jdbc:mysql://localhost:3306/taipei_db?useSSL=false&serverTimezone=Asia/Taipei&allowPublicKeyRetrieval=true
        spring.datasource.username=your_db_username
        spring.datasource.password=your_db_password
        spring.jpa.hibernate.ddl-auto=update
        ```

4.  **Configure JWT Secret:**

      * Set the JWT secret key in `application.properties`:
        ```properties
        jwt.secret=YourVeryStrongAndSecretKeyHereAndLongEnoughForHS512 # Replace with a strong, secure key (at least 256 bits for HS256, 512 bits for HS512)
        jwt.expiration=86400000 # Token expiration time in milliseconds (e.g., 24 hours)
        ```
      * **Security Note:** For production, it's highly recommended to use environment variables or a configuration server for sensitive data.

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
    Open your web browser and navigate to `http://localhost:8080`.

      * Optionally, run the SQL `INSERT` statements from the [Database Schema & Sample Data](https://www.google.com/search?q=%23database-schema--sample-data) section to populate the `attractions` table if not already populated or if `ddl-auto` is not `create`.

## API Endpoints

Here are the main API endpoints provided by the application:

**Authentication (`/api/user`)**

  * `POST /api/user/register`: Register a new user.
      * Body: `RegisterRequest`
        ```json
        {
          "name": "Test User",
          "email": "test@example.com",
          "password": "password123",
          "age": 30,        // Optional
          "gender": "male"  // Optional ("male", "female", "other")
        }
        ```
      * Returns: `{ "ok": true }` on success, or an error message e.g. `{ "error": true, "message": "註冊失敗..." }`.
  * `POST /api/user/login`: Log in a user.
      * Body: `LoginRequest` (email, password)
      * Returns: `{ "token": "jwt_token_here", "role": "USER" }` or error e.g. `{ "error": true, "message": "登入失敗..." }`.
  * `GET /api/user/auth`: Get the currently authenticated user's information.
      * Requires: Valid JWT in Authorization header (`Bearer <token>`).
      * Returns:
        ```json
        {
          "data": {
            "id": 1,
            "name": "Test User",
            "email": "test@example.com",
            "role": "USER", // e.g., USER, ADMIN, TRIP_MANAGER
            "age": 30,
            "gender": "male"
          }
        }
        ```
        or `{ "data": null }` if not authenticated or user not found.

**Attractions (`/api`)**

  * `GET /api/attractions`: Get a list of active attractions.
      * Query Parameters:
          * `page` (int, optional, default: 0): Page number for pagination.
          * `size` (int, optional, default: 12): Number of attractions per page.
          * `keyword` (String, optional): Keyword to search in attraction names.
          * `district` (String, optional): District to filter attractions by.
      * Returns: Paginated list of `AttractionBasicDTO`.
        ```json
        {
          "data": [
            {
              "id": 1,
              "name": "台北 101",
              "mrt": "台北101/世貿站",
              "category": "地標建築",
              "imageUrl": "image/101.jpg",
              "district": "信義區"
            }
            // ... more attractions
          ],
          "nextPage": 1 // or null if it's the last page
        }
        ```
  * `GET /api/attractions/{id}`: Get details of a specific active attraction.
      * Path Variable: `id` (Long): The ID of the attraction.
      * Returns: `AttractionDetailDTO`.
        ```json
        {
          "data": {
            "id": 1,
            "name": "台北 101",
            "description": "台北101是台灣最著名的地標之一...",
            "address": "台北市信義區信義路五段7號",
            "lat": 25.033961,
            "lng": 121.564468,
            "transport": "搭乘捷運淡水信義線至台北101/世貿站，步行即可抵達。",
            "mrt": "台北101/世貿站",
            "category": "地標建築",
            "district": "信義區",
            "imageUrl": "image/101.jpg"
          }
        }
        ```
  * `GET /api/districts`: Get a list of unique, active districts where attractions exist.
      * Returns:
        ```json
        {
          "data": ["信義區", "萬華區", "士林區", "..."]
        }
        ```

**Bookings (`/api`)**

  * `GET /api/booking/attractions`: Get a list of active attractions, simplified for booking dropdowns.
      * Returns: `{ "data": [ /* List of AttractionBasicDTO */ ] }`.
  * `POST /api/booking`: Create a new booking.
      * Requires: Valid JWT.
      * Body: `BookingRequest` (attractionId, date, time, price, contactName, contactPhone, customerIdNumber (optional))
        ```json
        {
          "attractionId": 1,
          "date": "2025-12-24", // YYYY-MM-DD, must be today or future
          "time": "morning",   // "morning" or "afternoon"
          "price": 2000,       // Positive number
          "contactName": "王小明",
          "contactPhone": "0912345678",
          "customerIdNumber": "A123456789" // Optional
        }
        ```
      * Returns: `{ "data": { /* BookingResponseDTO of the created booking with status PENDING */ } }` or error.
  * `GET /api/booking`: Get the current user's single *pending* booking (if any).
      * Requires: Valid JWT.
      * Returns: `{ "data": { /* BookingResponseDTO */ } }` or `{ "data": null }`.
  * `GET /api/bookings`: Get all bookings for the current user (includes pending, paid, cancelled etc.).
      * Requires: Valid JWT.
      * Returns: `{ "data": [ /* List of BookingResponseDTO */ ] }`.
  * `DELETE /api/booking/{bookingId}`: Delete/cancel a specific "PENDING" booking for the current user.
      * Requires: Valid JWT.
      * Path Variable: `bookingId` (Long).
      * Returns: `{ "ok": true }` on success, or error.
  * `POST /api/booking/{bookingId}/pay`: Mark a "PENDING" booking as "PAID".
      * Requires: Valid JWT.
      * Path Variable: `bookingId` (Long).
      * Body: `PaymentRequestDTO`
        ```json
        {
          "paymentNonce": "simulated-payment-nonce-or-actual-token"
        }
        ```
      * Returns: `{ "data": { /* Updated BookingResponseDTO with status PAID */ } }` or error.

**Admin - Users (`/api/admin/users`)** (Requires ADMIN role)

  * `POST /api/admin/users`: Create a new user.
      * Body: `AdminCreateUserRequestDTO` (name, email, password, role (optional), age (optional), gender (optional)).
      * Returns: `UserAdminViewDTO`.
  * `GET /api/admin/users`: Get a paginated list of all users.
      * Query Parameters: e.g., `?page=0&size=10&sort=id,asc`.
      * Returns: `Page<UserAdminViewDTO>`.
  * `GET /api/admin/users/{userId}`: Get details of a specific user.
      * Returns: `UserAdminViewDTO`.
  * `PUT /api/admin/users/{userId}`: Update a user's details.
      * Body: `AdminUpdateUserRequestDTO` (name, email, password (optional, min 4 chars), role (optional), age (optional), gender (optional)).
      * Returns: `UserAdminViewDTO`.
  * `DELETE /api/admin/users/{userId}`: Delete a user and their associated bookings.
      * Returns: HTTP 204 No Content on success.

**Admin - Bookings (`/api/admin/bookings`)** (Requires ADMIN role)

  * `GET /api/admin/bookings`: Get a paginated list of all bookings.
      * Query Parameters: e.g., `?page=0&size=20&sort=createdAt,desc`.
      * Returns: `Page<BookingAdminViewDTO>`.
  * `GET /api/admin/bookings/{bookingId}`: Get detailed information about a specific booking.
      * Returns: `AdminBookingDetailDTO`.
  * `DELETE /api/admin/bookings/{bookingId}`: Cancel a specific booking by admin (changes status to "CANCELLED").
      * Returns: HTTP 204 No Content on success.

**Admin - Attractions (`/api/admin/attractions`)** (Requires ADMIN or TRIP\_MANAGER role)

  * `POST /api/admin/attractions`: Create a new attraction.
      * Body: `CreateAttractionRequestDTO` (name, description, address, lat, lng, transport, mrt, category, district, imageUrl).
      * Returns: `AttractionAdminViewDTO`.
  * `GET /api/admin/attractions`: Get a paginated list of all attractions.
      * Query Parameters: e.g., `?page=0&size=10&sort=id,asc`.
      * Returns: `Page<AttractionAdminViewDTO>`.
  * `GET /api/admin/attractions/{attractionId}`: Get details of a specific attraction for admin view.
      * Returns: `AttractionAdminViewDTO`.
  * `PUT /api/admin/attractions/{attractionId}`: Update an attraction.
      * Body: `UpdateAttractionRequestDTO`.
      * Returns: `AttractionAdminViewDTO`.
  * `PATCH /api/admin/attractions/{attractionId}/status`: Update the active status of an attraction.
      * Body: `{ "isActive": boolean }`.
      * Returns: `AttractionAdminViewDTO`.
  * `DELETE /api/admin/attractions/{attractionId}`: Delete an attraction (fails if active bookings exist).
      * Returns: HTTP 204 No Content on success.

**Admin - Statistics & Trends (`/api/admin`)** (Requires ADMIN role)

  * `GET /api/admin/stats`: Get dashboard statistics.
      * Returns: `AdminStatsDTO` (includes total users, bookings, attractions, age/gender distributions, and trends).
  * `GET /api/admin/trends/registrations`: Get user registration trend data for the last 30 days.
      * Returns: `List<TrendDataPointDTO>` (e.g., `[{"label": "2025-04-09", "value": 5}, ...]`).
  * `GET /api/admin/trends/bookings`: Get booking trend data for the last 30 days.
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
