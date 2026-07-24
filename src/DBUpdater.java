import java.sql.Connection;
import java.sql.Statement;
import utils.DBUtils;

public class DBUpdater {
    public static void main(String[] args) {
        try (Connection conn = DBUtils.getConnection();
             Statement stmt = conn.createStatement()) {
             
            System.out.println("Updating snacks table...");
            try {
                stmt.execute("ALTER TABLE snacks ADD COLUMN cost_price DECIMAL(10,2) DEFAULT 0.00 AFTER price");
            } catch (Exception e) { System.out.println(e.getMessage()); }

            try {
                stmt.execute("ALTER TABLE snacks ADD COLUMN min_stock INT DEFAULT 10 AFTER quantity");
            } catch (Exception e) { System.out.println(e.getMessage()); }

            System.out.println("Updating shows table...");
            try {
                stmt.execute("ALTER TABLE shows ADD COLUMN snack_discount_id INT AFTER show_time");
            } catch (Exception e) { System.out.println(e.getMessage()); }

            System.out.println("Updating snack_sales table...");
            try {
                stmt.execute("ALTER TABLE snack_sales ADD COLUMN user_id INT AFTER booking_id");
            } catch (Exception e) { System.out.println(e.getMessage()); }
            try {
                stmt.execute("ALTER TABLE snack_sales ADD FOREIGN KEY (user_id) REFERENCES users(id)");
            } catch (Exception e) { System.out.println(e.getMessage()); }

            System.out.println("Creating inventory_logs table...");
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory_logs (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "snack_id INT NOT NULL, " +
                    "old_qty INT NOT NULL, " +
                    "new_qty INT NOT NULL, " +
                    "reason ENUM('SALE', 'RESTOCK', 'MANUAL_ADJUSTMENT') NOT NULL, " +
                    "user_id INT NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (snack_id) REFERENCES snacks(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")");

            System.out.println("Creating combos table...");
            stmt.execute("CREATE TABLE IF NOT EXISTS combos (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "description VARCHAR(255), " +
                    "price DECIMAL(10,2) NOT NULL, " +
                    "status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE', " +
                    "image_path VARCHAR(255) DEFAULT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");

            System.out.println("Creating combo_items table...");
            stmt.execute("CREATE TABLE IF NOT EXISTS combo_items (" +
                    "combo_id INT NOT NULL, " +
                    "snack_id INT NOT NULL, " +
                    "quantity INT NOT NULL, " +
                    "PRIMARY KEY (combo_id, snack_id), " +
                    "FOREIGN KEY (combo_id) REFERENCES combos(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (snack_id) REFERENCES snacks(id) ON DELETE CASCADE" +
                    ")");

            System.out.println("Updating snack_sale_items table for combos...");
            try {
                stmt.execute("ALTER TABLE snack_sale_items MODIFY snack_id INT DEFAULT NULL");
            } catch (Exception e) { System.out.println(e.getMessage()); }
            try {
                stmt.execute("ALTER TABLE snack_sale_items ADD COLUMN combo_id INT DEFAULT NULL AFTER snack_id");
            } catch (Exception e) { System.out.println(e.getMessage()); }
            try {
                stmt.execute("ALTER TABLE snack_sale_items ADD FOREIGN KEY (combo_id) REFERENCES combos(id) ON DELETE SET NULL");
            } catch (Exception e) { System.out.println(e.getMessage()); }
            try {
                // snack_id was previously NOT NULL, but setup.sql now says ON DELETE SET NULL, we need to alter the constraint but let's just do it
                stmt.execute("ALTER TABLE snack_sale_items DROP FOREIGN KEY snack_sale_items_ibfk_2");
                stmt.execute("ALTER TABLE snack_sale_items ADD FOREIGN KEY (snack_id) REFERENCES snacks(id) ON DELETE SET NULL");
            } catch (Exception e) { System.out.println(e.getMessage()); }

            System.out.println("Database updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
