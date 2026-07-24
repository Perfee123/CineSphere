import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.File;
import java.net.URL;

public class TestFXML {
    public static void main(String[] args) {
        Platform.startup(() -> {
            try {
                URL url = new File("src/views/snackbar/SnackPOS.fxml").toURI().toURL();
                System.out.println("URL: " + url);
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();
                System.out.println("SUCCESS");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.exit();
            }
        });
    }
}
