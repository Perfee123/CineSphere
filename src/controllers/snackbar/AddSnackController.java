package controllers.snackbar;

import controllers.MainLayoutController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Snack;
import models.SnackDAO;

import java.math.BigDecimal;

public class AddSnackController {

    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextArea descArea;
    @FXML private TextField priceField;
    @FXML private TextField costPriceField;
    @FXML private TextField quantityField;
    @FXML private TextField minStockField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label imagePathLabel;

    private Snack currentSnack;
    private SnackDAO snackDAO = new SnackDAO();
    private java.io.File selectedImageFile = null;
    
    public static Snack snackToEdit = null;

    @FXML
    public void initialize() {
        categoryCombo.setItems(FXCollections.observableArrayList(
            "Popcorn", "Beverage", "Candy", "Combo", "Other"
        ));
        statusCombo.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
        
        initData(snackToEdit);
    }

    private void initData(Snack snack) {
        this.currentSnack = snack;

        if (snack != null) {
            titleLabel.setText("Edit Snack");
            nameField.setText(snack.getName());
            categoryCombo.setValue(snack.getCategory());
            descArea.setText(snack.getDescription());
            priceField.setText(snack.getPrice().toString());
            costPriceField.setText(snack.getCostPrice() != null ? snack.getCostPrice().toString() : "0.00");
            quantityField.setText(String.valueOf(snack.getQuantity()));
            minStockField.setText(String.valueOf(snack.getMinStock()));
            statusCombo.setValue(snack.getStatus());
            if (snack.getImagePath() != null) {
                imagePathLabel.setText("Current Image: " + snack.getImagePath());
            }
        } else {
            titleLabel.setText("Add New Snack");
            statusCombo.setValue("ACTIVE");
            quantityField.setText("0");
            minStockField.setText("0");
            costPriceField.setText("0.00");
            imagePathLabel.setText("No file selected");
        }
    }

    @FXML
    private void handleChooseImage() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Choose Snack Image");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        java.io.File file = fileChooser.showOpenDialog(titleLabel.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            imagePathLabel.setText(file.getName());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        if (currentSnack == null) {
            currentSnack = new Snack();
        }

        currentSnack.setName(nameField.getText());
        currentSnack.setCategory(categoryCombo.getValue());
        currentSnack.setDescription(descArea.getText());
        currentSnack.setPrice(new BigDecimal(priceField.getText()));
        currentSnack.setCostPrice(new BigDecimal(costPriceField.getText()));
        currentSnack.setQuantity(Integer.parseInt(quantityField.getText()));
        currentSnack.setMinStock(Integer.parseInt(minStockField.getText()));
        currentSnack.setStatus(statusCombo.getValue());

        if (selectedImageFile != null) {
            try {
                String relativePath = utils.ImageUtils.copyImage(selectedImageFile);
                currentSnack.setImagePath(relativePath);
            } catch (java.io.IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to save the image file.");
                return;
            }
        }

        boolean success;
        if (currentSnack.getId() == 0) {
            success = snackDAO.addSnack(currentSnack);
        } else {
            success = snackDAO.updateSnack(currentSnack);
        }

        if (success) {
            navigateBack();
        } else {
            showAlert("Error", "Failed to save snack details.");
        }
    }

    @FXML
    private void handleCancel() {
        navigateBack();
    }
    
    @FXML
    private void handleBack() {
        navigateBack();
    }

    private void navigateBack() {
        MainLayoutController.getInstance().loadPageDirectly("/views/snackbar/SnackManagement.fxml");
    }

    private boolean validateInput() {
        String name = nameField.getText();
        String priceStr = priceField.getText();
        String costPriceStr = costPriceField.getText();
        String qtyStr = quantityField.getText();
        String minStockStr = minStockField.getText();

        if (name == null || name.trim().isEmpty()) {
            showAlert("Validation Error", "Name is required.");
            return false;
        }

        if (categoryCombo.getValue() == null) {
            showAlert("Validation Error", "Category is required.");
            return false;
        }

        try {
            BigDecimal p = new BigDecimal(priceStr);
            if (p.compareTo(BigDecimal.ZERO) < 0) {
                showAlert("Validation Error", "Price cannot be negative.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Price must be a valid number.");
            return false;
        }

        try {
            BigDecimal cp = new BigDecimal(costPriceStr);
            if (cp.compareTo(BigDecimal.ZERO) < 0) {
                showAlert("Validation Error", "Cost Price cannot be negative.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Cost Price must be a valid number.");
            return false;
        }

        try {
            int q = Integer.parseInt(qtyStr);
            if (q < 0) {
                showAlert("Validation Error", "Quantity cannot be negative.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Quantity must be an integer.");
            return false;
        }

        try {
            int ms = Integer.parseInt(minStockStr);
            if (ms < 0) {
                showAlert("Validation Error", "Min Stock cannot be negative.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Min Stock must be an integer.");
            return false;
        }

        return true;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
