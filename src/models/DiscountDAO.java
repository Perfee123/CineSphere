package models;

import utils.DBUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiscountDAO {

    public List<Discount> getAllDiscounts() {
        List<Discount> discounts = new ArrayList<>();
        String query = "SELECT * FROM discounts";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                discounts.add(mapResultSetToDiscount(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return discounts;
    }

    public boolean addDiscount(Discount discount) {
        String query = "INSERT INTO discounts (target_type, target_id, discount_percentage, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, discount.getTargetType());
            stmt.setInt(2, discount.getTargetId());
            stmt.setBigDecimal(3, discount.getDiscountPercentage());
            stmt.setString(4, discount.getStatus() != null ? discount.getStatus() : "ACTIVE");
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        discount.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateDiscount(Discount discount) {
        String query = "UPDATE discounts SET target_type = ?, target_id = ?, discount_percentage = ?, status = ? WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, discount.getTargetType());
            stmt.setInt(2, discount.getTargetId());
            stmt.setBigDecimal(3, discount.getDiscountPercentage());
            stmt.setString(4, discount.getStatus());
            stmt.setInt(5, discount.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteDiscount(int id) {
        String query = "DELETE FROM discounts WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Discount mapResultSetToDiscount(ResultSet rs) throws SQLException {
        return new Discount(
            rs.getInt("id"),
            rs.getString("target_type"),
            rs.getInt("target_id"),
            rs.getBigDecimal("discount_percentage"),
            rs.getString("status"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at")
        );
    }
}
