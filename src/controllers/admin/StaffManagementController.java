package controllers.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User;
import models.UserDAO;

import java.io.IOException;
import java.util.List;

public class StaffManagementController {

    @FXML private TextField searchField;
    @FXML private TableView<User> staffTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;

    private UserDAO userDAO = new UserDAO();
    private ObservableList<User> masterData = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (user.getFullName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (user.getUsername().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Optional row styling
        staffTable.setRowFactory(tv -> new TableRow<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("table-row-active", "table-row-inactive");
                if (item == null || empty) {
                    setStyle("");
                } else {
                    if ("ACTIVE".equals(item.getStatus())) {
                        getStyleClass().add("table-row-active");
                    } else {
                        getStyleClass().add("table-row-inactive");
                    }
                }
            }
        });
    }

    private void loadData() {
        List<User> users = userDAO.getAllUsers();
        masterData.setAll(users);
        filteredData = new FilteredList<>(masterData, p -> true);
        staffTable.setItems(filteredData);
    }

    @FXML
    public void onAddStaff() {
        openStaffDialog(null);
    }

    @FXML
    public void onEditStaff() {
        User selected = staffTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a staff member to edit.");
            alert.showAndWait();
            return;
        }
        openStaffDialog(selected);
    }

    private void openStaffDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/StaffDialog.fxml"));
            Parent root = loader.load();
            
            StaffDialogController controller = loader.getController();
            controller.initData(user, userDAO);
            
            Stage stage = new Stage();
            stage.setTitle(user == null ? "Add New Staff Account" : "Edit Staff Account");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            if (controller.isSaved()) {
                loadData(); // refresh table
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
