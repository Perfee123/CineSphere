package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import models.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainLayoutController {

    @FXML private Label roleLabel;
    @FXML private VBox navMenu;
    @FXML private Label nameLabel;
    @FXML private Label bottomRoleLabel;
    @FXML private StackPane contentArea;

    private User currentUser;
    private List<Button> navButtons = new ArrayList<>();

    private static MainLayoutController instance;

    public void initData(User user) {
        instance = this;
        this.currentUser = user;
        
        nameLabel.setText(user.getFullName());
        
        if ("ADMIN".equals(user.getRole())) {
            roleLabel.setText("SYSTEM ADMIN");
            bottomRoleLabel.setText("Administrator");
            buildAdminSidebar();
        } else if ("SCHEDULER".equals(user.getRole())) {
            roleLabel.setText("MOVIE SCHEDULER");
            bottomRoleLabel.setText("Scheduler");
            buildSchedulerSidebar();
        } else if ("SNACK_STAFF".equals(user.getRole())) {
            roleLabel.setText("SNACK BAR STAFF");
            bottomRoleLabel.setText("Snack Desk");
            buildSnackSidebar();
        } else {
            roleLabel.setText("TICKET STAFF");
            bottomRoleLabel.setText("Ticket Desk");
            buildTicketSidebar();
        }
        
        // Select the first item by default if available
        if (!navButtons.isEmpty()) {
            navButtons.get(0).fire();
        }
    }

    private void buildAdminSidebar() {
        addNavButton("Dashboard Overview", "/views/admin/AdminOverview.fxml");
        addNavButton("Movie Management", "/views/admin/MovieManagement.fxml");
        addNavButton("Staff Management", "/views/admin/StaffManagement.fxml");
        addNavButton("Sales & Reports", "/views/admin/SalesReports.fxml");
    }

    private void buildTicketSidebar() {
        addNavButton("Dashboard", "/views/ticket/TicketOverview.fxml");
        addNavButton("Now Showing", "/views/ticket/NowShowing.fxml");
        addNavButton("Booking Ticket", "/views/ticket/BookingTicket.fxml");
        addNavButton("Ticket Desk", "/views/ticket/BookingHistory.fxml");
    }

    private void buildSchedulerSidebar() {
        addNavButton("Overview", "/views/scheduler/SchedulerOverview.fxml");
        addNavButton("Hall Management", "/views/scheduler/HallManagement.fxml");
        addNavButton("Pending Shows", "/views/scheduler/PendingShows.fxml");
        addNavButton("Currently Showing", "/views/scheduler/ShowScheduling.fxml");
        addNavButton("Discounts", "/views/scheduler/DiscountManagement.fxml");
    }

    private void buildSnackSidebar() {
        addNavButton("Overview", "/views/snackbar/SnackOverview.fxml");
        addNavButton("Snack Management", "/views/snackbar/SnackManagement.fxml");
        addNavButton("Point of Sale", "/views/snackbar/SnackPOS.fxml");
        addNavButton("Bills & Reports", "/views/snackbar/SnackBills.fxml");
    }

    private void addNavButton(String title, String fxmlPath) {
        Button btn = new Button(title);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("nav-button");

        btn.setOnAction(e -> {
            setActiveButton(btn);
            loadPage(fxmlPath);
        });

        navButtons.add(btn);
        navMenu.getChildren().add(btn);
    }

    private void setActiveButton(Button activeBtn) {
        for (Button btn : navButtons) {
            if (btn == activeBtn) {
                if (!btn.getStyleClass().contains("nav-button-active")) {
                    btn.getStyleClass().add("nav-button-active");
                }
                btn.getStyleClass().remove("nav-button");
            } else {
                if (!btn.getStyleClass().contains("nav-button")) {
                    btn.getStyleClass().add("nav-button");
                }
                btn.getStyleClass().remove("nav-button-active");
            }
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Failed to load page: " + fxmlPath);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(errorLabel);
        }
    }

    @FXML
    public void handleSignOut(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public static MainLayoutController getInstance() {
        return instance;
    }

    public void navigateByTitle(String title) {
        for (Button btn : navButtons) {
            if (btn.getText().equals(title)) {
                btn.fire();
                return;
            }
        }
    }

    public void loadPageDirectly(String fxmlPath) {
        // Deselect nav buttons visually since this is a sub-page
        for (Button btn : navButtons) {
            if (!btn.getStyleClass().contains("nav-button")) {
                btn.getStyleClass().add("nav-button");
            }
            btn.getStyleClass().remove("nav-button-active");
        }
        loadPage(fxmlPath);
    }
}
