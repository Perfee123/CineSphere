package models;

import utils.DBUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PromoCodeDAO {

    public PromoCodeDAO() {
        initializeTable();
    }

    private void initializeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS promo_codes (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "code VARCHAR(50) NOT NULL UNIQUE, " +
                     "discount_percentage DECIMAL(5,2) NOT NULL, " +
                     "status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE', " +
                     "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                     ")";
        try (Connection conn = DBUtils.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PromoCode> getAllPromoCodes() {
        List<PromoCode> codes = new ArrayList<>();
        String sql = "SELECT * FROM promo_codes ORDER BY created_at DESC";
        try (Connection conn = DBUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                codes.add(new PromoCode(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getBigDecimal("discount_percentage"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return codes;
    }

    public boolean addPromoCode(PromoCode promoCode) {
        String sql = "INSERT INTO promo_codes (code, discount_percentage, status) VALUES (?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, promoCode.getCode().toUpperCase().trim());
            stmt.setBigDecimal(2, promoCode.getDiscountPercentage());
            stmt.setString(3, promoCode.getStatus());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePromoCode(PromoCode promoCode) {
        String sql = "UPDATE promo_codes SET code = ?, discount_percentage = ?, status = ? WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, promoCode.getCode().toUpperCase().trim());
            stmt.setBigDecimal(2, promoCode.getDiscountPercentage());
            stmt.setString(3, promoCode.getStatus());
            stmt.setInt(4, promoCode.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deletePromoCode(int id) {
        String sql = "DELETE FROM promo_codes WHERE id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public PromoCode getPromoCode(String code) {
        String sql = "SELECT * FROM promo_codes WHERE code = ? AND status = 'ACTIVE'";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code.toUpperCase().trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new PromoCode(
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getBigDecimal("discount_percentage"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
