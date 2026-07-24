import java.sql.Connection;
import java.sql.Statement;
import utils.DBUtils;

public class AddCashierToSales {
    public static void main(String[] args) {
        try (Connection conn = DBUtils.getConnection();
             Statement stmt = conn.createStatement()) {
            System.out.println("Adding user_id to snack_sales...");
            stmt.execute("ALTER TABLE snack_sales ADD COLUMN user_id INT DEFAULT NULL AFTER booking_id");
            stmt.execute("ALTER TABLE snack_sales ADD FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL");
            System.out.println("Done!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
