package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DBUtils {

    private static Map<String, String> env;

    static {
        env = loadEnv(".env");
    }

    private static Map<String, String> loadEnv(String filename) {
        Map<String, String> envMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int idx = line.indexOf('=');
                if (idx > 0) {
                    String key = line.substring(0, idx).trim();
                    String value = line.substring(idx + 1).trim();
                    envMap.put(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load " + filename + " file.");
        }
        return envMap;
    }

    public static Connection getConnection() throws SQLException {
        String url = env.getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/cinesphere");
        String user = env.getOrDefault("DB_USER", "root");
        String password = env.getOrDefault("DB_PASSWORD", "");
        
        return DriverManager.getConnection(url, user, password);
    }
}
