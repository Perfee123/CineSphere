package controllers.scheduler;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.Hall;
import models.HallDAO;
import controllers.MainLayoutController;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class HallManagementController implements Initializable {

    @FXML private VBox mainContent;
    @FXML private FlowPane hallsContainer;
    @FXML private VBox loadingOverlay;

    private HallDAO hallDAO = new HallDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadData();
    }

    private void loadData() {
        hallsContainer.getChildren().clear();
        
        List<Hall> halls = hallDAO.getAllHalls();
        if (halls.isEmpty()) {
            Label noHalls = new Label("No halls found. Click '+ Add New Hall' to create one.");
            noHalls.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d; -fx-padding: 30;");
            hallsContainer.getChildren().add(noHalls);
            return;
        }

        for (Hall hall : halls) {
            hallsContainer.getChildren().add(createHallCard(hall));
        }
    }

    private VBox createHallCard(Hall hall) {
        VBox card = new VBox(15);
        card.setPrefWidth(320); // slightly more compact
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-border-color: #e2e8f0; -fx-border-radius: 12px; -fx-border-width: 1px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 0); -fx-cursor: hand;");
        
        // Hover effect for the card
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12px; -fx-border-color: #cbd5e1; -fx-border-radius: 12px; -fx-border-width: 1px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 2); -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-border-color: #e2e8f0; -fx-border-radius: 12px; -fx-border-width: 1px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 0); -fx-cursor: hand;"));
        
        // Double-click to edit
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                handleEditHall(hall);
            }
        });
        
        // Header: Name and Badges
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLbl = new Label(hall.getName());
        nameLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        
        Label statusBadge = new Label(hall.getStatus().equals("ACTIVE") ? "Available" : "Maintenance");
        if (hall.getStatus().equals("ACTIVE")) {
            statusBadge.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 4 8; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            statusBadge.setStyle("-fx-background-color: #fef08a; -fx-text-fill: #854d0e; -fx-padding: 4 8; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold;");
        }
        
        header.getChildren().addAll(nameLbl, hSpacer, statusBadge);
        
        // Subtitle tags (Type and Kids)
        HBox tagBox = new HBox(8);
        tagBox.setAlignment(Pos.CENTER_LEFT);
        
        Label typeBadge = new Label(hall.getType());
        typeBadge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-padding: 4 8; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");
        tagBox.getChildren().add(typeBadge);
        
        if (hall.isKidsHall()) {
            Label kidsBadge = new Label("★ Kids");
            kidsBadge.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-padding: 4 8; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");
            tagBox.getChildren().add(kidsBadge);
        }
        
        // Inner Stats Box
        HBox statsBox = new HBox();
        statsBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8px; -fx-border-color: #f1f5f9; -fx-border-radius: 8px; -fx-padding: 12;");
        
        VBox seatStat = new VBox(2);
        seatStat.setAlignment(Pos.CENTER);
        Label seatVal = new Label(String.valueOf(hall.getTotalSeats()));
        seatVal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label seatLbl = new Label("Total Seats");
        seatLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        seatStat.getChildren().addAll(seatVal, seatLbl);
        
        Region sSpacer = new Region();
        HBox.setHgrow(sSpacer, Priority.ALWAYS);
        
        VBox dimStat = new VBox(2);
        dimStat.setAlignment(Pos.CENTER);
        Label dimVal = new Label(hall.getSeatRows() + " × " + hall.getSeatColumns());
        dimVal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label dimLbl = new Label("Grid Layout");
        dimLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        dimStat.getChildren().addAll(dimVal, dimLbl);
        
        statsBox.getChildren().addAll(seatStat, sSpacer, dimStat);
        
        // Action Buttons
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(5, 0, 0, 0));
        
        Button toggleBtn = new Button(hall.getStatus().equals("ACTIVE") ? "Lock" : "Unlock");
        toggleBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(toggleBtn, Priority.ALWAYS);
        toggleBtn.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-text-fill: #475569; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8;");
        toggleBtn.setOnAction(e -> {
            e.consume(); // Prevent double click event from firing on card
            handleToggleStatus(hall);
        });
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(deleteBtn, Priority.ALWAYS);
        deleteBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-border-color: #fca5a5; -fx-border-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8;");
        deleteBtn.setOnAction(e -> {
            e.consume();
            handleDelete(hall);
        });
        
        bottomBox.getChildren().addAll(toggleBtn, deleteBtn);
        
        // Hint text
        Label editHint = new Label("Double-click card to edit hall details");
        editHint.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-style: italic;");
        editHint.setMaxWidth(Double.MAX_VALUE);
        editHint.setAlignment(Pos.CENTER);
        
        card.getChildren().addAll(header, tagBox, statsBox, bottomBox, editHint);
        return card;
    }

    private void handleEditHall(Hall hall) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/scheduler/EditHall.fxml"));
            Parent root = loader.load();
            
            EditHallController controller = loader.getController();
            controller.setHallData(hall);
            
            StackPane contentArea = (StackPane) mainContent.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleToggleStatus(Hall hall) {
        if (hall.getStatus().equals("ACTIVE")) {
            hall.setStatus("MAINTENANCE");
        } else {
            hall.setStatus("ACTIVE");
        }
        if (hallDAO.updateHall(hall)) {
            loadData();
        }
    }

    private void handleDelete(Hall hall) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Hall: " + hall.getName());
        alert.setContentText("Are you sure you want to delete this hall? This cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (hallDAO.deleteHall(hall.getId())) {
                loadData();
            } else {
                Alert err = new Alert(Alert.AlertType.ERROR, "Failed to delete hall. It may have existing shows.");
                err.showAndWait();
            }
        }
    }

    @FXML
    public void handleAddNewHall(ActionEvent event) {
        MainLayoutController.getInstance().loadPageDirectly("/views/scheduler/AddHall.fxml");
    }

    private void showLoader() {
        loadingOverlay.setVisible(true);
        loadingOverlay.setManaged(true);
        mainContent.setDisable(true);
    }

    private void hideLoader() {
        loadingOverlay.setVisible(false);
        loadingOverlay.setManaged(false);
        mainContent.setDisable(false);
    }
}
