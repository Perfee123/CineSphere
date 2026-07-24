package controllers.admin;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import models.UserDAO;

public class StaffDialogController {

    @FXML private Label titleLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Button saveButton;
 
    private User currentUser;
    private UserDAO userDAO;
    private boolean saved = false;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("ADMIN", "TICKET_STAFF", "SCHEDULER", "SNACK_STAFF"));
        statusComboBox.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
    }

    public void initData(User user, UserDAO dao) {
        this.userDAO = dao;
        this.currentUser = user;

        if (user != null) {
            titleLabel.setText("Edit Staff");
            fullNameField.setText(user.getFullName());
            usernameField.setText(user.getUsername());
            passwordField.setText(user.getPassword());
            roleComboBox.setValue(user.getRole());
            statusComboBox.setValue(user.getStatus());
            saveButton.setText("Save");
        } else {
            titleLabel.setText("Add New Staff");
            roleComboBox.setValue("TICKET_STAFF");
            statusComboBox.setValue("ACTIVE");
            saveButton.setText("Add");
        }
    }

    @FXML
    public void onSave(ActionEvent event) {
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();
        String status = statusComboBox.getValue();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || role == null || status == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill in all fields.");
            alert.showAndWait();
            return;
        }

        if (currentUser == null) {
            // Add new
            User newUser = new User(0, username, password, fullName, role, status, null, null);
            if (userDAO.addUser(newUser)) {
                saved = true;
                closeDialog(event);
            } else {
                showError("Failed to add user. Username might already exist.");
            }
        } else {
            // Update existing
            currentUser.setFullName(fullName);
            currentUser.setUsername(username);
            currentUser.setPassword(password);
            currentUser.setRole(role);
            currentUser.setStatus(status);
            
            if (userDAO.updateUser(currentUser)) {
                saved = true;
                closeDialog(event);
            } else {
                showError("Failed to update user.");
            }
        }
    }

    @FXML
    public void onCancel(ActionEvent event) {
        closeDialog(event);
    }

    private void closeDialog(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    public boolean isSaved() {
        return saved;
    }
}
