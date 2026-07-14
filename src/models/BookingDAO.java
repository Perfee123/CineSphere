package models;

import utils.DBUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public List<BookingTableItem> getAllBookings() {
        List<BookingTableItem> bookings = new ArrayList<>();
        
        String sql = "SELECT b.id as booking_id, " +
                     "DATE_FORMAT(b.booking_time, '%Y-%m-%d %H:%i') as date, " +
                     "m.title as movie_title, " +
                     "h.name as hall_name, " +
                     "(b.adult_count + b.kids_count) as total_tickets, " +
                     "b.status, " +
                     "b.total_amount, " +
                     "GROUP_CONCAT(CONCAT(st.row_label, st.seat_number) SEPARATOR ', ') as seats " +
                     "FROM bookings b " +
                     "JOIN shows s ON b.show_id = s.id " +
                     "JOIN movies m ON s.movie_id = m.id " +
                     "JOIN halls h ON s.hall_id = h.id " +
                     "LEFT JOIN booking_seats bs ON b.id = bs.booking_id " +
                     "LEFT JOIN seats st ON bs.seat_id = st.id " +
                     "GROUP BY b.id " +
                     "ORDER BY b.booking_time DESC";
                     
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                String bookingId = "BK-" + rs.getInt("booking_id");
                String date = rs.getString("date");
                String movieTitle = rs.getString("movie_title");
                String hall = rs.getString("hall_name");
                int tickets = rs.getInt("total_tickets");
                String status = rs.getString("status");
                double amount = rs.getDouble("total_amount");
                String seats = rs.getString("seats");
                
                if (seats == null) seats = "-";
                
                // Ensure enum string matches UI expectations
                if ("CHECKED_IN".equals(status)) {
                    status = "CHECKED IN";
                }
                
                bookings.add(new BookingTableItem(bookingId, date, movieTitle, hall, tickets, status, amount, seats));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return bookings;
    }

    public boolean updateStatus(String bookingId, String newStatus) {
        // Map UI string back to DB ENUM if needed
        if ("CHECKED IN".equals(newStatus)) {
            newStatus = "CHECKED_IN";
        }
        
        int bId = Integer.parseInt(bookingId.replace("BK-", ""));
        
        String sql = "UPDATE bookings SET status = ? WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, newStatus);
            stmt.setInt(2, bId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int[] getHallDimensions(String showId) {
        int[] dims = new int[]{8, 10}; // defaults
        int sId = Integer.parseInt(showId.replace("SH-", ""));
        String sql = "SELECT h.seat_rows, h.seat_columns FROM shows s JOIN halls h ON s.hall_id = h.id WHERE s.id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    dims[0] = rs.getInt("seat_rows");
                    dims[1] = rs.getInt("seat_columns");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dims;
    }

    public List<String> getBookedSeats(String showId) {
        List<String> bookedSeats = new ArrayList<>();
        int sId = Integer.parseInt(showId.replace("SH-", ""));
        
        String sql = "SELECT st.row_label, st.seat_number FROM booking_seats bs " +
                     "JOIN bookings b ON bs.booking_id = b.id " +
                     "JOIN seats st ON bs.seat_id = st.id " +
                     "WHERE b.show_id = ? AND b.status != 'CANCELLED'";
                     
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, sId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String seatId = rs.getString("row_label") + rs.getInt("seat_number");
                    bookedSeats.add(seatId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookedSeats;
    }

    public String createBooking(String showId, int userId, int adultCount, int kidsCount, double totalAmount, List<String> selectedSeats) {
        int sId = Integer.parseInt(showId.replace("SH-", ""));
        String newBookingId = null;
        
        Connection conn = null;
        try {
            conn = DBUtils.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // 1. Insert Booking
            String insertBooking = "INSERT INTO bookings (show_id, booked_by, adult_count, kids_count, total_amount, status) VALUES (?, ?, ?, ?, ?, 'CONFIRMED')";
            int generatedId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(insertBooking, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, sId);
                stmt.setInt(2, userId);
                stmt.setInt(3, adultCount);
                stmt.setInt(4, kidsCount);
                stmt.setDouble(5, totalAmount);
                stmt.executeUpdate();
                
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                        newBookingId = "BK-" + generatedId;
                    }
                }
            }
            
            if (generatedId == -1) {
                conn.rollback();
                return null;
            }
            
            // 2. Fetch hall_id for the show
            int hallId = -1;
            String getHall = "SELECT hall_id FROM shows WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(getHall)) {
                stmt.setInt(1, sId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        hallId = rs.getInt("hall_id");
                    }
                }
            }
            
            // 3. Insert Booking Seats
            String insertSeats = "INSERT INTO booking_seats (booking_id, seat_id, ticket_type) VALUES (?, (SELECT id FROM seats WHERE hall_id = ? AND row_label = ? AND seat_number = ? LIMIT 1), ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSeats)) {
                // Distribute tickets (simple logic: assign adults first, then kids)
                int assignedAdults = 0;
                
                for (String seatLabel : selectedSeats) {
                    String rowLabel = seatLabel.substring(0, 1);
                    int seatNum = Integer.parseInt(seatLabel.substring(1));
                    String type = assignedAdults < adultCount ? "ADULT" : "KID";
                    assignedAdults++;
                    
                    stmt.setInt(1, generatedId);
                    stmt.setInt(2, hallId);
                    stmt.setString(3, rowLabel);
                    stmt.setInt(4, seatNum);
                    stmt.setString(5, type);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
        
        return newBookingId;
    }
}
