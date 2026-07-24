package utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ReceiptUtils {

    public static void downloadReceiptAsImage(Node nodeToSnapshot, Window ownerWindow, String defaultFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Receipt");
        fileChooser.setInitialFileName(defaultFileName + ".png");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));

        File file = fileChooser.showSaveDialog(ownerWindow);
        if (file != null) {
            // Take snapshot
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(javafx.scene.paint.Color.WHITE);
            WritableImage snapshot = nodeToSnapshot.snapshot(params, null);

            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
                System.out.println("Receipt saved to: " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
