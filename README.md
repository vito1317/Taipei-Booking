# Taipei Booking

This is a Taipei attraction booking system developed using Java Spring Boot. It allows users to browse various attractions in Taipei City, register/log in, and book trips.

## Table of Contents

* [Features](#features)
* [Tech Stack](#tech-stack)
* [Prerequisites](#prerequisites)
* [Getting Started](#getting-started)
* [API Endpoints (Optional)](#api-endpoints-optional)
* [Contributing](#contributing)
* [License](#license)

## Features

* **Attraction Browsing:** View a list of attractions in Taipei City, including images, names, categories, and MRT station information. Supports searching by keyword and pagination.
* **User Authentication:** Secure user registration and login using JWT (JSON Web Tokens).
* **Trip Booking:** Logged-in users can create new bookings by selecting an attraction, date, time, and price.
* **Booking Management:** Users can view their upcoming (unpaid/unconfirmed) booking details.
* **Admin Panel (Optional):** An administrative interface for managing attractions, users, and potentially viewing booking statistics or trends (if implemented).

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
    git clone [https://github.com/your-username/taipei-booking.git](https://github.com/your-username/taipei-booking.git) # Replace with your actual repo URL
    cd taipei-booking
    ```

2.  **Configure the database:**
    * Open the `src/main/resources/application.properties` file.
    * Update the following properties with your database connection details:
        ```properties
        spring.datasource.url=jdbc:mysql://localhost:3306/taipei_booking_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true # Example for MySQL
        spring.datasource.username=your_db_username
        spring.datasource.password=your_db_password
        spring.jpa.hibernate.ddl-auto=update # Or 'validate', 'create', 'create-drop' depending on your needs
        ```
    * **Important:** Create the database (e.g., `taipei_booking_db`) in your database server if it doesn't exist.

3.  **Configure JWT Secret:**
    * Set the JWT secret key in `application.properties`:
        ```properties
        jwt.secret=YourVeryStrongAndSecretKeyHere # Replace with a strong, secure key
        jwt.expiration=86400000 # Token expiration time in milliseconds (e.g., 24 hours)
        ```
    * **Security Note:** For production, it's highly recommended to use environment variables or a configuration server for sensitive data like database passwords and JWT secrets, rather than hardcoding them in `application.properties`.

4.  **Build the project:**
    ```bash
    mvn clean install
    ```

5.  **Run the application:**
    ```bash
    mvn spring-boot:run
    ```
    Alternatively, you can run the packaged JAR file:
    ```bash
    java -jar target/taipei-booking-0.0.1-SNAPSHOT.jar # Adjust JAR filename if necessary
    ```

6.  **Access the application:**
    Open your web browser and navigate to `http://localhost:8080` (or the port configured in `application.properties`).

## API Endpoints (Optional)

Here are some of the main API endpoints provided by the application:

* `GET /api/attractions`: Get a list of attractions (supports pagination and keyword search).
* `GET /api/attractions/{attractionId}`: Get details for a specific attraction.
* `POST /api/user`: Register a new user.
* `PUT /api/user/auth`: Log in a user and receive a JWT token.
* `GET /api/user/auth`: Get the currently logged-in user's information (requires JWT).
* `POST /api/booking`: Create a new booking (requires JWT).
* `GET /api/booking`: Get the current user's upcoming booking (requires JWT).
* `DELETE /api/booking`: Delete the current user's upcoming booking (requires JWT).
* **(Admin Endpoints)**: Document admin endpoints if applicable (e.g., `GET /api/admin/users`, `POST /api/admin/attractions`).

*(Note: This is a sample list based on typical REST conventions and your controller names. Review your `*Controller.java` files for the exact paths and HTTP methods.)*

## Contributing

Contributions are welcome! If you'd like to contribute, please follow these steps:

1.  Fork the repository.
2.  Create a new branch (`git checkout -b feature/your-feature-name`).
3.  Make your changes.
4.  Commit your changes (`git commit -m 'Add some feature'`).
5.  Push to the branch (`git push origin feature/your-feature-name`).
6.  Open a Pull Request.

Please ensure your code adheres to the existing style and includes tests where appropriate.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
