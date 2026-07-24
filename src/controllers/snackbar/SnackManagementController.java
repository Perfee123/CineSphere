package controllers.snackbar;

import controllers.MainLayoutController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.Snack;
import models.SnackDAO;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SnackManagementController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private FlowPane cardsContainer;

    private SnackDAO snackDAO = new SnackDAO();
    private List<Snack> allSnacks;

    @FXML
    public void initialize() {
        categoryFilter.getItems().addAll("All Categories", "Popcorn", "Beverage", "Candy", "Combo", "Other");
        categoryFilter.setValue("All Categories");

        loadData();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> renderCards());
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> renderCards());
    }

    private void loadData() {
        allSnacks = snackDAO.getAllSnacks();
        renderCards();
    }

    private void renderCards() {
        cardsContainer.getChildren().clear();

        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String selectedCategory = categoryFilter.getValue();

        List<Snack> filtered = allSnacks.stream().filter(snack -> {
            boolean matchesSearch = snack.getName().toLowerCase().contains(searchText);
            boolean matchesCategory = "All Categories".equals(selectedCategory) || 
                                     (snack.getCategory() != null && snack.getCategory().equals(selectedCategory));
            return matchesSearch && matchesCategory;
        }).collect(Collectors.toList());

        for (Snack snack : filtered) {
            cardsContainer.getChildren().add(createSnackCard(snack));
        }
    }

    private VBox createSnackCard(Snack snack) {
        VBox card = new VBox();
        card.setPrefWidth(220);
        card.getStyleClass().add("movie-grid-card");

        Region imageRegion = new Region();
        imageRegion.setPrefSize(220, 140);
        imageRegion.setMinSize(220, 140);
        imageRegion.setMaxSize(220, 140);
        
        if (snack.getImagePath() != null && !snack.getImagePath().isEmpty()) {
            java.io.File file = new java.io.File(snack.getImagePath());
            if (file.exists()) {
                String uri = file.toURI().toString().replace("'", "\\'");
                imageRegion.setStyle("-fx-background-image: url('" + uri + "'); -fx-background-size: cover; -fx-background-position: center; -fx-background-radius: 12 12 0 0; -fx-border-radius: 12 12 0 0;");
            } else {
                imageRegion.setStyle("-fx-background-color: #eeeeee; -fx-background-radius: 12 12 0 0; -fx-border-radius: 12 12 0 0;");
            }
        } else {
            imageRegion.setStyle("-fx-background-color: #eeeeee; -fx-background-radius: 12 12 0 0; -fx-border-radius: 12 12 0 0;");
        }

        VBox detailsBox = new VBox(10);
        detailsBox.setPadding(new Insets(15));

        Label nameLabel = new Label(snack.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLabel.setTextFill(Color.web("#111111"));
        nameLabel.setWrapText(true);

        Label categoryLabel = new Label(snack.getCategory());
        categoryLabel.setFont(Font.font("System", 12));
        categoryLabel.setTextFill(Color.web("#888888"));

        Label priceLabel = new Label(String.format("$%.2f", snack.getPrice()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        priceLabel.setTextFill(Color.web("#0066ff"));

        HBox statusBox = new HBox(5);
        Label qtyLabel = new Label("Qty: " + snack.getQuantity());
        qtyLabel.setFont(Font.font("System", 13));
        
        Label statusLabel = new Label(snack.getStatus());
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        statusLabel.setPadding(new Insets(2, 6, 2, 6));
        statusLabel.setStyle("-fx-background-radius: 10px;");
        
        if ("ACTIVE".equals(snack.getStatus())) {
            if (snack.getQuantity() < snack.getMinStock()) {
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #ffeeba; -fx-text-fill: #856404;");
            } else {
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #d4edda; -fx-text-fill: #155724;");
            }
        } else {
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
        }
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        statusBox.getChildren().addAll(qtyLabel, spacer, statusLabel);

        Button editBtn = new Button("Edit Snack");
        editBtn.setMaxWidth(Double.MAX_VALUE);
        editBtn.setStyle("-fx-background-color: white; -fx-border-color: #0066ff; -fx-text-fill: #0066ff; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-cursor: hand;");
        editBtn.setOnAction(e -> openEditPage(snack));

        detailsBox.getChildren().addAll(nameLabel, categoryLabel, priceLabel, statusBox, editBtn);
        card.getChildren().addAll(imageRegion, detailsBox);
        return card;
    }

    @FXML
    public void onAddSnack() {
        openEditPage(null);
    }

    private void openEditPage(Snack snack) {
        AddSnackController.snackToEdit = snack;
        MainLayoutController.getInstance().loadPageDirectly("/views/snackbar/AddSnack.fxml");
    }
}
