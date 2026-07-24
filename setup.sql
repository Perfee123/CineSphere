-- CineSphere Database Setup Script
-- Run this entire script in MySQL Workbench to set up or reset the CineSphere database.

DROP DATABASE IF EXISTS cinesphere;
CREATE DATABASE cinesphere;
USE cinesphere;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'TICKET_STAFF') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Movies Table
CREATE TABLE IF NOT EXISTS movies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    poster_path VARCHAR(500),
    banner_path VARCHAR(500),
    rating DECIMAL(3,1) DEFAULT 0.0,
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

-- 8. Stored Procedure: Auto-Generate Seats
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
            INSERT INTO seats (hall_id, row_label, seat_number, seat_type)
            VALUES (p_hall_id, row_char, c, 'REGULAR');
            SET c = c + 1;
        END WHILE;
        SET r = r + 1;
    END WHILE;
END //
DELIMITER ;

-- 9. Seed Data: Users
INSERT INTO users (username, password, full_name, role) VALUES
('admin', '123', 'System Administrator', 'ADMIN'),
('ticket', '123', 'Counter Staff', 'TICKET_STAFF');

-- 10. Seed Data: Halls
INSERT INTO halls (name, total_seats, seat_rows, seat_columns) VALUES
('Hall A', 80, 8, 10),
('Hall B', 60, 6, 10),
('Hall C', 100, 10, 10);

-- 11. Generate Seats for All Halls
CALL generate_hall_seats(1, 8, 10);
CALL generate_hall_seats(2, 6, 10);
CALL generate_hall_seats(3, 10, 10);

-- 12. Seed Data: Movies
INSERT INTO movies (title, description, duration_minutes, genre, adult_price, kids_price, rating, release_date, showing_from, showing_until, tmdb_id, poster_path, banner_path, status) VALUES
('Inside Out 2', 'Teenager Rileys mind headquarters is undergoing a sudden demolition to make room for something entirely unexpected: new Emotions!', 96, 'Animation', 400.00, 250.00, 7.7, '2024-06-11', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1022789, null, null, 'ACTIVE'),
('Deadpool & Wolverine', 'A listless Wade Wilson toils away in civilian life with his days as the morally flexible mercenary, Deadpool, behind him.', 127, 'Action', 500.00, 300.00, 8.0, '2024-07-24', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 533535, null, null, 'ACTIVE'),
('Despicable Me 4', 'Gru and Lucy and their girls welcome a new member to the Gru family, Gru Jr., who is intent on tormenting his dad.', 94, 'Animation', 350.00, 200.00, 7.4, '2024-06-20', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 519182, null, null, 'ACTIVE'),
('Twisters', 'As storm season intensifies, the paths of former storm chaser Kate Cooper and reckless social-media superstar Tyler Owens collide.', 122, 'Action', 450.00, 250.00, 7.1, '2024-07-10', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 718821, null, null, 'ACTIVE'),
('Longlegs', 'In pursuit of a serial killer, an FBI agent uncovers a series of occult clues that she must solve to end his terrifying killing spree.', 101, 'Horror', 400.00, 0.00, 7.4, '2024-07-10', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1226578, null, null, 'ACTIVE'),
('The Dark Knight', 'When the menace known as the Joker wreaks havoc on Gotham, Batman must accept one of the greatest psychological tests of his ability to fight injustice.', 152, 'Action', 350.00, 200.00, 9.0, '2008-07-18', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 60 DAY), null, '/qJ2tW6WMUDux911r6m7haRef0WH.jpg', '/dqK9Hag1054tghRQSqLSfrkvQnA.jpg', 'ACTIVE'),
('Inception', 'A thief who steals corporate secrets through dream-sharing technology is given the task of planting an idea into the mind of a CEO.', 148, 'Sci-Fi', 400.00, 250.00, 8.8, '2010-07-16', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 60 DAY), null, '/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg', '/8ZTVqvKDQ8emSGUEMjsS4yHAwrp.jpg', 'ACTIVE'),
('Interstellar', 'A team of explorers travel through a wormhole in space in an attempt to ensure humanitys survival.', 169, 'Sci-Fi', 450.00, 250.00, 8.6, '2014-11-05', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 60 DAY), null, null, null, 'ACTIVE'),
('Dunkirk', 'Allied soldiers from Belgium, the British Commonwealth and Empire, and France are surrounded by the German Army and evacuated during a fierce battle.', 106, 'War', 350.00, 200.00, 7.8, '2017-07-19', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 60 DAY), null, null, null, 'ACTIVE'),
('Tenet', 'Armed with only one word, Tenet, and fighting for the survival of the entire world, a Protagonist journeys through a twilight world of international espionage.', 150, 'Action', 400.00, 200.00, 7.3, '2020-08-22', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 60 DAY), null, null, null, 'ACTIVE');

-- 13. Seed Data: Shows
INSERT INTO shows (movie_id, hall_id, show_date, show_time, status) VALUES
(1, 1, CURDATE(), '10:00:00', 'SCHEDULED'), (1, 1, CURDATE(), '14:00:00', 'SCHEDULED'),
(2, 2, CURDATE(), '13:00:00', 'SCHEDULED'), (2, 2, CURDATE(), '19:00:00', 'SCHEDULED'),
(3, 3, CURDATE(), '11:00:00', 'SCHEDULED'), (3, 3, CURDATE(), '15:00:00', 'SCHEDULED'),
(4, 1, CURDATE(), '18:00:00', 'SCHEDULED'),
(5, 2, CURDATE(), '22:00:00', 'SCHEDULED'),
(6, 3, CURDATE(), '20:00:00', 'SCHEDULED'),
(7, 1, CURDATE(), '22:00:00', 'SCHEDULED');

-- 14. Seed Data: Bookings (Mock Data for Ticket Desk)
INSERT INTO bookings (show_id, booked_by, adult_count, kids_count, total_amount, status) VALUES
(1, 2, 2, 0, 800.00, 'CONFIRMED'),
(2, 2, 0, 3, 750.00, 'CHECKED_IN'),
(3, 2, 1, 1, 800.00, 'CANCELLED'),
(4, 2, 2, 2, 1600.00, 'CONFIRMED');

-- 15. Seed Data: Booking Seats
INSERT INTO booking_seats (booking_id, seat_id, ticket_type) VALUES
(1, 1, 'ADULT'), (1, 2, 'ADULT'),
(2, 3, 'KID'), (2, 4, 'KID'), (2, 5, 'KID'),
(3, 81, 'ADULT'), (3, 82, 'KID'),
(4, 141, 'ADULT'), (4, 142, 'ADULT'), (4, 143, 'KID'), (4, 144, 'KID');

-- 16. Procedure to fully book a show
DELIMITER //
CREATE PROCEDURE fully_book_show(IN target_show_id INT)
BEGIN
    DECLARE h_id INT;
    DECLARE new_b_id INT;
    
    SELECT hall_id INTO h_id FROM shows WHERE id = target_show_id;
    
    INSERT INTO bookings (show_id, booked_by, adult_count, kids_count, total_amount, status)
    VALUES (target_show_id, 2, 60, 0, 20000.00, 'CONFIRMED');
    
    SET new_b_id = LAST_INSERT_ID();
    
    INSERT INTO booking_seats (booking_id, seat_id, ticket_type)
    SELECT new_b_id, id, 'ADULT' FROM seats WHERE hall_id = h_id;
END //
DELIMITER ;

CALL fully_book_show(6); -- Fully books "Despicable Me 4" in Hall 3

