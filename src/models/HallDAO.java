package models;

import utils.DBUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HallDAO {

    public HallDAO() {
        initializeSchema();
    }

    private void initializeSchema() {
        try (Connection conn = DBUtils.getConnection();
             Statement stmt = conn.createStatement()) {
            try {
                stmt.execute("ALTER TABLE halls ADD COLUMN type VARCHAR(50) DEFAULT 'Digital 2D'");
            } catch (SQLException e) {
                // Ignore if column already exists
            }
            try {
                stmt.execute("ALTER TABLE halls ADD COLUMN is_kids_hall BOOLEAN DEFAULT FALSE");
            } catch (SQLException e) {
                // Ignore if column already exists
            }
            try {
                stmt.execute("ALTER TABLE seats ADD COLUMN status ENUM('AVAILABLE', 'MAINTENANCE') DEFAULT 'AVAILABLE'");
            } catch (SQLException e) {
                // Ignore if column already exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Hall> getAllHalls() {
        List<Hall> halls = new ArrayList<>();
        String query = "SELECT * FROM halls";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                halls.add(mapResultSetToHall(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return halls;
    }

    public Hall getHallById(int id) {
        String query = "SELECT * FROM halls WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToHall(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addHall(Hall hall) {
        String query = "INSERT INTO halls (name, type, total_seats, seat_rows, seat_columns, status, is_kids_hall) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBUtils.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, hall.getName());
                stmt.setString(2, hall.getType() != null ? hall.getType() : "Digital 2D");
                stmt.setInt(3, hall.getTotalSeats());
                stmt.setInt(4, hall.getSeatRows());
                stmt.setInt(5, hall.getSeatColumns());
                stmt.setString(6, hall.getStatus() != null ? hall.getStatus() : "ACTIVE");
                stmt.setBoolean(7, hall.isKidsHall());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            hall.setId(generatedKeys.getInt(1));

                            // Auto-generate seats for this new hall using the stored procedure
                            generateSeatsForHall(conn, hall.getId(), hall.getSeatRows(), hall.getSeatColumns());

                            conn.commit();
                            return true;
                        }
                    }
                }
            }

            conn.rollback();
            return false;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean updateHall(Hall hall) {
        String query = "UPDATE halls SET name = ?, type = ?, total_seats = ?, seat_rows = ?, seat_columns = ?, status = ?, is_kids_hall = ? WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, hall.getName());
            stmt.setString(2, hall.getType());
            stmt.setInt(3, hall.getTotalSeats());
            stmt.setInt(4, hall.getSeatRows());
            stmt.setInt(5, hall.getSeatColumns());
            stmt.setString(6, hall.getStatus());
            stmt.setBoolean(7, hall.isKidsHall());
            stmt.setInt(8, hall.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteHall(int id) {
        String query = "DELETE FROM halls WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void generateSeatsForHall(Connection conn, int hallId, int rows, int cols) throws SQLException {
        String query = "CALL generate_hall_seats(?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, hallId);
            stmt.setInt(2, rows);
            stmt.setInt(3, cols);
            stmt.execute();
        }
    }

    private Hall mapResultSetToHall(ResultSet rs) throws SQLException {
        boolean hasType = true;
        boolean hasKids = true;
        try { rs.findColumn("type"); } catch (SQLException e) { hasType = false; }
        try { rs.findColumn("is_kids_hall"); } catch (SQLException e) { hasKids = false; }
        
        return new Hall(
            rs.getInt("id"),
            rs.getString("name"),
            hasType ? rs.getString("type") : "Digital 2D",
            rs.getInt("total_seats"),
            rs.getInt("seat_rows"),
            rs.getInt("seat_columns"),
            rs.getString("status"),
            hasKids ? rs.getBoolean("is_kids_hall") : false,
            rs.getTimestamp("created_at")
        );
    }

    public List<String> getMaintenanceSeats(int hallId) {
        List<String> maintenanceSeats = new ArrayList<>();
        String sql = "SELECT row_label, seat_number FROM seats WHERE hall_id = ? AND status = 'MAINTENANCE'";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, hallId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    maintenanceSeats.add(rs.getString("row_label") + rs.getInt("seat_number"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maintenanceSeats;
    }

    public boolean updateMaintenanceSeats(int hallId, List<String> maintenanceSeats) {
        Connection conn = null;
        try {
            conn = DBUtils.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Reset all seats for this hall to AVAILABLE
            String resetSql = "UPDATE seats SET status = 'AVAILABLE' WHERE hall_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(resetSql)) {
                stmt.setInt(1, hallId);
                stmt.executeUpdate();
            }
            
            // 2. Set specified seats to MAINTENANCE
            if (maintenanceSeats != null && !maintenanceSeats.isEmpty()) {
                String updateSql = "UPDATE seats SET status = 'MAINTENANCE' WHERE hall_id = ? AND row_label = ? AND seat_number = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    for (String seatId : maintenanceSeats) {
                        String rowLabel = seatId.substring(0, 1);
                        int seatNum = Integer.parseInt(seatId.substring(1));
                        
                        stmt.setInt(1, hallId);
                        stmt.setString(2, rowLabel);
                        stmt.setInt(3, seatNum);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
}
