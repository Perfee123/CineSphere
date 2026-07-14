package controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

public class ReportTimeframeDialogController {

    @FXML private ComboBox<String> timeframeCombo;
    
    private boolean generated = false;
    private String selectedTimeframe = "All Time";

    public boolean isGenerated() {
        return generated;
    }

    public String getSelectedTimeframe() {
        return selectedTimeframe;
    }

    @FXML
    public void handleCancel() {
        generated = false;
        Stage stage = (Stage) timeframeCombo.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleGenerate() {
        selectedTimeframe = timeframeCombo.getValue();
        if (selectedTimeframe == null) {
            selectedTimeframe = "All Time";
        }
        generated = true;
        Stage stage = (Stage) timeframeCombo.getScene().getWindow();
        stage.close();
    }
}
