package controllers.scheduler;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import models.Hall;
import models.HallDAO;
import controllers.MainLayoutController;

public class AddHallController {

    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField rowsField;
    @FXML private TextField colsField;
    @FXML private CheckBox kidsHallCheck;
    @FXML private Label errorLabel;
    @FXML private GridPane seatGridPreview;

    public boolean saveSuccessful = false;
    private HallDAO hallDAO = new HallDAO();

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList(
                "Digital 2D", "IMAX", "Dolby Atmos", "Scope", "3D"
        ));
        
        // Listeners for rows and columns to generate preview
        rowsField.textProperty().addListener((obs, oldV, newV) -> generatePreview());
        colsField.textProperty().addListener((obs, oldV, newV) -> generatePreview());
    }

    private void generatePreview() {
        seatGridPreview.getChildren().clear();
        errorLabel.setVisible(false); // Reset error initially
        
        try {
            String rText = rowsField.getText().trim();
            String cText = colsField.getText().trim();
            
            if (rText.isEmpty() || cText.isEmpty()) {
                return;
            }
            
            int r = Integer.parseInt(rText);
            int c = Integer.parseInt(cText);
            
            if (r > 10 || c > 15) {
                showError("Max limit exceeded: Rows (10 max), Columns (15 max).");
                return;
            }
            
            if (r > 0 && c > 0) { 
                for (int i = 0; i < r; i++) {
                    for (int j = 0; j < c; j++) {
                        String seatId = (char)('A' + i) + String.valueOf(j + 1);
                        Button seatBtn = new Button(seatId);
                        seatBtn.setPrefSize(45, 45);
                        seatBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #e9ecef; -fx-border-color: #ced4da; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-text-fill: #495057;");
                        
                        Tooltip tooltip = new Tooltip(seatId);
                        seatBtn.setTooltip(tooltip);
                        
                        seatGridPreview.add(seatBtn, j, i);
                    }
                }
            }
        } catch (NumberFormatException e) {
            showError("Rows and columns must be numbers.");
        }
    }

    @FXML
    public void handleSave(ActionEvent event) {
        String name = nameField.getText();
        String type = typeComboBox.getValue();
        String rowsStr = rowsField.getText();
        String colsStr = colsField.getText();
        boolean isKids = kidsHallCheck.isSelected();

        if (name == null || name.trim().isEmpty() || type == null || 
            rowsStr == null || rowsStr.trim().isEmpty() || colsStr == null || colsStr.trim().isEmpty()) {
            showError("All fields are required.");
            return;
        }

        try {
            int rows = Integer.parseInt(rowsStr);
            int cols = Integer.parseInt(colsStr);
            
            if (rows <= 0 || cols <= 0) {
                showError("Rows and Columns must be > 0.");
                return;
            }
            
            if (rows > 10 || cols > 15) {
                showError("Max limit exceeded: Rows (10 max), Columns (15 max).");
                return;
            }

            int totalSeats = rows * cols;
            
            Hall newHall = new Hall(0, name.trim(), type, totalSeats, rows, cols, "ACTIVE", isKids, null);
            if (hallDAO.addHall(newHall)) {
                saveSuccessful = true;
                MainLayoutController.getInstance().loadPageDirectly("/views/scheduler/HallManagement.fxml");
            } else {
                showError("Failed to add hall. Name must be unique.");
            }

        } catch (NumberFormatException e) {
            showError("Rows and columns must be numbers.");
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        MainLayoutController.getInstance().loadPageDirectly("/views/scheduler/HallManagement.fxml");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
