import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import java.net.URL;
import java.io.File;

public class Test {
    public static void main(String[] args) {
        Platform.startup(() -> {
            try {
                File file = new File("src/views/snackbar/SnackBills.fxml");
                System.out.println("File exists: " + file.exists());
                URL location = file.toURI().toURL();
                System.out.println("Loading URL: " + location);
                FXMLLoader loader = new FXMLLoader(location);
                loader.load();
                System.out.println("FXML Loaded Successfully!");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.exit();
            }
        });
    }
}
