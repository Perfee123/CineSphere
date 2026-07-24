package models;

import utils.DBUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SnackSaleDAO {

    public boolean createSale(SnackSale sale, List<SnackSaleItem> items) {
        String insertSaleQuery = "INSERT INTO snack_sales (booking_id, user_id, total_amount) VALUES (?, ?, ?)";
        String insertItemQuery = "INSERT INTO snack_sale_items (snack_sale_id, snack_id, quantity, price_at_sale, discount_applied) VALUES (?, ?, ?, ?, ?)";
        String updateStockQuery = "UPDATE snacks SET quantity = quantity - ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DBUtils.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert Sale
            int saleId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(insertSaleQuery, Statement.RETURN_GENERATED_KEYS)) {
                if (sale.getBookingId() != null) {
                    stmt.setInt(1, sale.getBookingId());
                } else {
                    stmt.setNull(1, Types.INTEGER);
                }
                if (sale.getUserId() != null) {
                    stmt.setInt(2, sale.getUserId());
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                stmt.setBigDecimal(3, sale.getTotalAmount());

                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        saleId = rs.getInt(1);
                        sale.setId(saleId);
                    }
                }
            }

            if (saleId == -1) {
                conn.rollback();
                return false;
            }

            // 2. Insert Items and Update Stock
            String guardedStockQuery = "UPDATE snacks SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
            try (PreparedStatement itemStmt = conn.prepareStatement(insertItemQuery);
                 PreparedStatement stockStmt = conn.prepareStatement(guardedStockQuery)) {

                for (SnackSaleItem item : items) {
                    // Insert Item
                    itemStmt.setInt(1, saleId);
                    itemStmt.setInt(2, item.getSnackId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setBigDecimal(4, item.getPriceAtSale());
                    itemStmt.setBigDecimal(5, item.getDiscountApplied());
                    itemStmt.addBatch();

                    // Update Stock with guard
                    stockStmt.setInt(1, item.getQuantity());
                    stockStmt.setInt(2, item.getSnackId());
                    stockStmt.setInt(3, item.getQuantity());
                    stockStmt.addBatch();
                }

                itemStmt.executeBatch();
                int[] stockResults = stockStmt.executeBatch();

                // Check if any stock update failed (insufficient quantity)
                for (int result : stockResults) {
                    if (result == 0) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<SnackSale> getAllSales() {
        List<SnackSale> sales = new ArrayList<>();
        String query = "SELECT s.*, u.username as cashier_name FROM snack_sales s LEFT JOIN users u ON s.user_id = u.id ORDER BY s.sale_time DESC";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
             
             while (rs.next()) {
                 SnackSale sale = new SnackSale(
                     rs.getInt("id"),
                     (Integer) rs.getObject("booking_id"),
                     (Integer) rs.getObject("user_id"),
                     rs.getBigDecimal("total_amount"),
                     rs.getTimestamp("sale_time")
                 );
                 sale.setCashierName(rs.getString("cashier_name"));
                 sales.add(sale);
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    public List<SnackSale> getSalesByDate(java.time.LocalDate date) {
        List<SnackSale> sales = new ArrayList<>();
        String query = "SELECT s.*, u.username as cashier_name FROM snack_sales s LEFT JOIN users u ON s.user_id = u.id WHERE DATE(s.sale_time) = ? ORDER BY s.sale_time DESC";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
             stmt.setDate(1, java.sql.Date.valueOf(date));
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     SnackSale sale = new SnackSale(
                         rs.getInt("id"),
                         (Integer) rs.getObject("booking_id"),
                         (Integer) rs.getObject("user_id"),
                         rs.getBigDecimal("total_amount"),
                         rs.getTimestamp("sale_time")
                     );
                     sale.setCashierName(rs.getString("cashier_name"));
                     sales.add(sale);
                 }
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    public List<SnackSaleItem> getItemsForSale(int saleId) {
        List<SnackSaleItem> items = new ArrayList<>();
        String query = "SELECT si.*, COALESCE(s.name, 'Deleted Item') as snack_name FROM snack_sale_items si LEFT JOIN snacks s ON si.snack_id = s.id WHERE si.snack_sale_id = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

             stmt.setInt(1, saleId);
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     SnackSaleItem item = new SnackSaleItem(
                         rs.getInt("id"),
                         rs.getInt("snack_sale_id"),
                         rs.getInt("snack_id"),
                         rs.getInt("quantity"),
                         rs.getBigDecimal("price_at_sale"),
                         rs.getBigDecimal("discount_applied")
                     );
                     item.setSnackName(rs.getString("snack_name"));
                     items.add(item);
                 }
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public List<SnackSale> getSalesByDateRange(java.time.LocalDate start, java.time.LocalDate end) {
        List<SnackSale> sales = new ArrayList<>();
        String query = "SELECT s.*, u.username as cashier_name FROM snack_sales s LEFT JOIN users u ON s.user_id = u.id WHERE DATE(s.sale_time) >= ? AND DATE(s.sale_time) <= ? ORDER BY s.sale_time DESC";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
             stmt.setDate(1, java.sql.Date.valueOf(start));
             stmt.setDate(2, java.sql.Date.valueOf(end));
             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                     SnackSale sale = new SnackSale(
                         rs.getInt("id"),
                         (Integer) rs.getObject("booking_id"),
                         (Integer) rs.getObject("user_id"),
                         rs.getBigDecimal("total_amount"),
                         rs.getTimestamp("sale_time")
                     );
                     sale.setCashierName(rs.getString("cashier_name"));
                     sales.add(sale);
                 }
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    public String getBestSellingSnack(java.time.LocalDate start, java.time.LocalDate end) {
        String query = "SELECT s.name, SUM(si.quantity) as total_qty " +
                       "FROM snack_sale_items si " +
                       "JOIN snacks s ON si.snack_id = s.id " +
                       "JOIN snack_sales ss ON si.snack_sale_id = ss.id ";
        
        if (start != null && end != null) {
            query += "WHERE DATE(ss.sale_time) >= ? AND DATE(ss.sale_time) <= ? ";
        } else if (start != null) {
            query += "WHERE DATE(ss.sale_time) = ? ";
        }
        
        query += "GROUP BY s.id ORDER BY total_qty DESC LIMIT 1";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
             if (start != null && end != null) {
                 stmt.setDate(1, java.sql.Date.valueOf(start));
                 stmt.setDate(2, java.sql.Date.valueOf(end));
             } else if (start != null) {
                 stmt.setDate(1, java.sql.Date.valueOf(start));
             }

             try (ResultSet rs = stmt.executeQuery()) {
                 if (rs.next()) {
                     return rs.getString("name");
                 }
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }
}
