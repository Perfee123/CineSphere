package controllers.scheduler;

import controllers.MainLayoutController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import models.Hall;
import models.HallDAO;

import java.util.ArrayList;
import java.util.List;

public class EditHallController {

    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField rowsField;
    @FXML private TextField colsField;
    @FXML private CheckBox kidsHallCheck;
    @FXML private Label errorLabel;
    @FXML private GridPane seatGridPreview;

    private Hall currentHall;
    private HallDAO hallDAO = new HallDAO();
    private List<String> maintenanceSeats = new ArrayList<>();

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList(
                "Digital 2D", "IMAX", "Dolby Atmos", "Scope", "3D"
        ));
    }

    public void setHallData(Hall hall) {
        this.currentHall = hall;
        nameField.setText(hall.getName());
        typeComboBox.setValue(hall.getType());
        rowsField.setText(String.valueOf(hall.getSeatRows()));
        colsField.setText(String.valueOf(hall.getSeatColumns()));
        kidsHallCheck.setSelected(hall.isKidsHall());

        // Fetch maintenance seats from DB
        maintenanceSeats = hallDAO.getMaintenanceSeats(hall.getId());
        
        generateSeatGrid();
    }

    private void generateSeatGrid() {
        seatGridPreview.getChildren().clear();
        int r = currentHall.getSeatRows();
        int c = currentHall.getSeatColumns();
        
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                String seatId = (char)('A' + i) + String.valueOf(j + 1);
                Button seatBtn = new Button(seatId);
                seatBtn.setPrefSize(45, 45);
                
                if (maintenanceSeats.contains(seatId)) {
                    seatBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #ffc107; -fx-text-fill: #000; -fx-border-color: #e0a800; -fx-border-radius: 4px; -fx-background-radius: 4px;");
                } else {
                    seatBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #ffffff; -fx-border-color: #ced4da; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-text-fill: #495057;");
                }
                
                Tooltip tooltip = new Tooltip(seatId);
                seatBtn.setTooltip(tooltip);
                
                seatBtn.setOnAction(e -> handleSeatToggle(seatBtn, seatId));
                
                seatGridPreview.add(seatBtn, j, i);
            }
        }
    }

    private void handleSeatToggle(Button seatBtn, String seatId) {
        if (maintenanceSeats.contains(seatId)) {
            maintenanceSeats.remove(seatId);
            seatBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #ffffff; -fx-border-color: #ced4da; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-text-fill: #495057;");
        } else {
            maintenanceSeats.add(seatId);
            seatBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #ffc107; -fx-text-fill: #000; -fx-border-color: #e0a800; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        }
    }

    @FXML
    public void handleSave(ActionEvent event) {
        String name = nameField.getText();
        String type = typeComboBox.getValue();
        boolean isKids = kidsHallCheck.isSelected();

        if (name == null || name.trim().isEmpty() || type == null) {
            showError("Name and Type are required.");
            return;
        }

        currentHall.setName(name.trim());
        currentHall.setType(type);
        currentHall.setKidsHall(isKids);

        if (hallDAO.updateHall(currentHall)) {
            hallDAO.updateMaintenanceSeats(currentHall.getId(), maintenanceSeats);
            MainLayoutController.getInstance().loadPageDirectly("/views/scheduler/HallManagement.fxml");
        } else {
            showError("Failed to update hall details.");
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
