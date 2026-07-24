import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import java.net.URL;
import java.io.File;

public class TestFXML {
    public static void main(String[] args) {
        // Initialize JavaFX Toolkit
        new JFXPanel();
        
        Platform.runLater(() -> {
            try {
                URL url = new File("src/views/snackbar/SnackPOS.fxml").toURI().toURL();
                System.out.println("Loading URL: " + url);
                FXMLLoader.load(url);
                System.out.println("SnackPOS.fxml loaded successfully!");
            } catch (Exception e) {
                System.err.println("Error loading SnackPOS.fxml:");
                e.printStackTrace();
            }
            
            try {
                URL url = new File("src/views/snackbar/SnackBills.fxml").toURI().toURL();
                System.out.println("Loading URL: " + url);
                FXMLLoader.load(url);
                System.out.println("SnackBills.fxml loaded successfully!");
            } catch (Exception e) {
                System.err.println("Error loading SnackBills.fxml:");
                e.printStackTrace();
            }
            
            System.exit(0);
        });
    }
}
