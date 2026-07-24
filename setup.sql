-- CineSphere Database Setup Script
-- Run this entire script in MySQL Workbench to set up or reset the CineSphere database.

CREATE DATABASE IF NOT EXISTS cinesphere;
USE cinesphere;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'TICKET_STAFF', 'SCHEDULER', 'SNACK_STAFF') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Movies Table
CREATE TABLE IF NOT EXISTS movies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    tagline VARCHAR(255),
    poster_path VARCHAR(500),
    banner_path VARCHAR(500),
    rating DECIMAL(3,1) DEFAULT 0.0,
    popularity DOUBLE DEFAULT 0.0,
    release_date DATE,
    duration_minutes INT NOT NULL DEFAULT 120,
    genre VARCHAR(100),
    adult_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    kids_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    showing_from DATE,
    showing_until DATE,
    tmdb_id INT UNIQUE,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. Halls Table
CREATE TABLE IF NOT EXISTS halls (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    total_seats INT NOT NULL,
    seat_rows INT NOT NULL,
    seat_columns INT NOT NULL,
    status ENUM('ACTIVE', 'MAINTENANCE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Seats Table
CREATE TABLE IF NOT EXISTS seats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    hall_id INT NOT NULL,
    row_label CHAR(1) NOT NULL,
    seat_number INT NOT NULL,
    seat_type ENUM('REGULAR', 'PREMIUM', 'VIP') NOT NULL DEFAULT 'REGULAR',
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE,
    UNIQUE KEY unique_seat (hall_id, row_label, seat_number)
);

-- 5. Shows Table
CREATE TABLE IF NOT EXISTS shows (
    id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT NOT NULL,
    hall_id INT NOT NULL,
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    status ENUM('SCHEDULED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
);

-- 6. Bookings Table
CREATE TABLE IF NOT EXISTS bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    show_id INT NOT NULL,
    booked_by INT NOT NULL,
    adult_count INT NOT NULL DEFAULT 0,
    kids_count INT NOT NULL DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('CONFIRMED', 'CANCELLED', 'CHECKED_IN') NOT NULL DEFAULT 'CONFIRMED',
    qr_code TEXT,
    FOREIGN KEY (show_id) REFERENCES shows(id),
    FOREIGN KEY (booked_by) REFERENCES users(id)
);

-- 7. Booking Seats Table
CREATE TABLE IF NOT EXISTS booking_seats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    seat_id INT NOT NULL,
    ticket_type ENUM('ADULT', 'KID') NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seats(id),
    UNIQUE KEY unique_booking_seat (booking_id, seat_id)
);

-- 7.5 Discounts Table
CREATE TABLE IF NOT EXISTS discounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    target_type ENUM('SHOW', 'SNACK', 'MOVIE') NOT NULL,
    target_id INT NOT NULL,
    discount_percentage DECIMAL(5,2) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 7.6 Promo Codes Table
CREATE TABLE IF NOT EXISTS promo_codes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_percentage DECIMAL(5,2) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Snacks Tables
CREATE TABLE IF NOT EXISTS snacks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    cost_price DECIMAL(10,2) DEFAULT 0.00,
    quantity INT DEFAULT 0,
    min_stock INT DEFAULT 10,
    category VARCHAR(50),
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    image_path VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    snack_id INT NOT NULL,
    old_qty INT NOT NULL,
    new_qty INT NOT NULL,
    reason ENUM('SALE', 'RESTOCK', 'MANUAL_ADJUSTMENT') NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (snack_id) REFERENCES snacks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS combos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    image_path VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS combo_items (
    combo_id INT NOT NULL,
    snack_id INT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (combo_id, snack_id),
    FOREIGN KEY (combo_id) REFERENCES combos(id) ON DELETE CASCADE,
    FOREIGN KEY (snack_id) REFERENCES snacks(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS snack_sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT DEFAULT NULL, 
    total_amount DECIMAL(10,2) NOT NULL,
    sale_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS snack_sale_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    snack_sale_id INT NOT NULL,
    snack_id INT DEFAULT NULL,
    combo_id INT DEFAULT NULL,
    quantity INT NOT NULL,
    price_at_sale DECIMAL(10,2) NOT NULL,
    discount_applied DECIMAL(5,2) DEFAULT 0.00,
    FOREIGN KEY (snack_sale_id) REFERENCES snack_sales(id) ON DELETE CASCADE,
    FOREIGN KEY (snack_id) REFERENCES snacks(id) ON DELETE SET NULL,
    FOREIGN KEY (combo_id) REFERENCES combos(id) ON DELETE SET NULL
);

-- 9. Stored Procedure: Auto-Generate Seats
DROP PROCEDURE IF EXISTS generate_hall_seats;
DELIMITER //
CREATE PROCEDURE generate_hall_seats(IN p_hall_id INT, IN p_rows INT, IN p_cols INT)
BEGIN
    DECLARE r INT DEFAULT 0;
    DECLARE c INT DEFAULT 0;
    DECLARE row_char CHAR(1);

    WHILE r < p_rows DO
        SET row_char = CHAR(65 + r);
        SET c = 1;
        WHILE c <= p_cols DO
            INSERT IGNORE INTO seats (hall_id, row_label, seat_number, seat_type)
            VALUES (p_hall_id, row_char, c, 'REGULAR');
            SET c = c + 1;
        END WHILE;
        SET r = r + 1;
    END WHILE;
END //
DELIMITER ;

-- 10. Seed Data: Users
INSERT IGNORE INTO users (username, password, full_name, role) VALUES
('admin', '123', 'System Administrator', 'ADMIN'),
('ticket', '123', 'Counter Staff', 'TICKET_STAFF'),
('scheduler', '123', 'Movie Scheduler', 'SCHEDULER'),
('snack', '123', 'Snack Bar Staff', 'SNACK_STAFF');

-- 11. Seed Data: Halls
INSERT IGNORE INTO halls (name, total_seats, seat_rows, seat_columns) VALUES
('Hall A', 80, 8, 10),
('Hall B', 60, 6, 10),
('Hall C', 100, 10, 10);

-- 12. Generate Seats for All Halls
CALL generate_hall_seats(1, 8, 10);
CALL generate_hall_seats(2, 6, 10);
CALL generate_hall_seats(3, 10, 10);