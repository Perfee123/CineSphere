package controllers.snackbar;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.Snack;
import models.SnackDAO;
import models.SnackSale;
import models.SnackSaleDAO;
import models.SnackSaleItem;
import models.BookingDAO;
import models.BookingPOSDetails;
import models.PromoCode;
import models.PromoCodeDAO;
import utils.SnackReceiptGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SnackPOSController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private FlowPane cardsContainer;

    @FXML private TableView<SnackSaleItem> cartTable;
    @FXML private TableColumn<SnackSaleItem, String> cartColName;
    @FXML private TableColumn<SnackSaleItem, Integer> cartColQty;
    @FXML private TableColumn<SnackSaleItem, BigDecimal> cartColPrice;
    @FXML private TableColumn<SnackSaleItem, BigDecimal> cartColTotal;
    @FXML private TableColumn<SnackSaleItem, Void> cartColAction;

    @FXML private ComboBox<String> customerTypeCombo;
    @FXML private VBox bookingLookupBox;
    @FXML private TextField bookingIdField;
    @FXML private VBox bookingDetailsBox;
    @FXML private Label bdMovieLabel;
    @FXML private Label bdHallLabel;
    @FXML private Label bdSeatsLabel;
    @FXML private Label bdTimeLabel;

    @FXML private Label subtotalLabel;
    @FXML private TextField discountCodeField;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;

    private SnackDAO snackDAO = new SnackDAO();
    private SnackSaleDAO saleDAO = new SnackSaleDAO();
    private BookingDAO bookingDAO = new BookingDAO();
    private PromoCodeDAO promoCodeDAO = new PromoCodeDAO();
    
    private List<Snack> allSnacks;
    private ObservableList<SnackSaleItem> cartData = FXCollections.observableArrayList();
    
    private BigDecimal currentDiscountPercentage = BigDecimal.ZERO;
    private Integer currentBookingId = null;

    @FXML
    public void initialize() {
        try {
            setupCartTable();
            
            categoryFilter.setItems(FXCollections.observableArrayList(
                "All Categories", "Popcorn", "Beverage", "Candy", "Combo", "Other"
            ));
            categoryFilter.setValue("All Categories");

            customerTypeCombo.setItems(FXCollections.observableArrayList(
                "Walk-In Customer", "Ticket Holder"
            ));
            customerTypeCombo.setValue("Walk-In Customer");
            customerTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if ("Ticket Holder".equals(newVal)) {
                    bookingLookupBox.setVisible(true);
                    bookingLookupBox.setManaged(true);
                } else {
                    bookingLookupBox.setVisible(false);
                    bookingLookupBox.setManaged(false);
                    resetBookingDetails();
                }
            });

            loadInventory();

            searchField.textProperty().addListener((observable, oldValue, newValue) -> renderCards());
            categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> renderCards());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetBookingDetails() {
        bookingIdField.clear();
        bookingDetailsBox.setVisible(false);
        bookingDetailsBox.setManaged(false);
        bdMovieLabel.setText("Movie: N/A");
        bdHallLabel.setText("Hall: N/A");
        bdSeatsLabel.setText("Seats: N/A");
        bdTimeLabel.setText("Time: N/A");
        
        currentDiscountPercentage = BigDecimal.ZERO;
        currentBookingId = null;
        updateTotals();
    }

    @FXML
    public void handleBookingLookup() {
        String input = bookingIdField.getText();
        if (input == null || input.trim().isEmpty()) {
            showAlert("Error", "Please enter a Booking ID.");
            return;
        }
        
        input = input.trim().replace("BK-", "");
        try {
            int bId = Integer.parseInt(input);
            BookingPOSDetails details = bookingDAO.getBookingDetailsForPOS(bId);
            
            if (details != null) {
                currentBookingId = bId;
                bdMovieLabel.setText("Movie: " + details.getMovieName());
                bdHallLabel.setText("Hall: " + details.getHallName());
                bdSeatsLabel.setText("Seats: " + details.getSeatNumbers());
                bdTimeLabel.setText("Time: " + details.getShowTime());
                
                bookingDetailsBox.setVisible(true);
                bookingDetailsBox.setManaged(true);
                
                if (details.getSnackDiscount().compareTo(BigDecimal.ZERO) > 0) {
                    currentDiscountPercentage = details.getSnackDiscount();
                    showAlert("Auto-Discount Applied", "A scheduled snack discount of " + currentDiscountPercentage + "% has been applied automatically!");
                } else {
                    currentDiscountPercentage = BigDecimal.ZERO;
                }
                updateTotals();
            } else {
                showAlert("Error", "Booking not found or it has been cancelled.");
                resetBookingDetails();
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid Booking ID format.");
        }
    }

    private void setupCartTable() {
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        cartColName.setCellValueFactory(new PropertyValueFactory<>("snackName"));
        cartColQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartColPrice.setCellValueFactory(new PropertyValueFactory<>("priceAtSale"));
        
        cartColTotal.setCellValueFactory(cellData -> {
            if (cellData.getValue() != null) {
                return new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getLineTotal());
            }
            return new javafx.beans.property.SimpleObjectProperty<>(BigDecimal.ZERO);
        });

        // Add Action column for +, -, x buttons
        cartColAction.setCellFactory(param -> new TableCell<SnackSaleItem, Void>() {
            private final Button btnAdd = new Button("+");
            private final Button btnSub = new Button("-");
            private final Button btnRemove = new Button("x");
            private final HBox pane = new HBox(5, btnAdd, btnSub, btnRemove);

            {
                btnAdd.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 2px 6px; -fx-cursor: hand;");
                btnSub.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-padding: 2px 7px; -fx-cursor: hand;");
                btnRemove.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 2px 7px; -fx-cursor: hand;");
                pane.setAlignment(Pos.CENTER);

                btnAdd.setOnAction(event -> {
                    SnackSaleItem item = getTableView().getItems().get(getIndex());
                    if (isStockAvailable(item.getSnackId(), 1)) {
                        item.setQuantity(item.getQuantity() + 1);
                        cartTable.refresh();
                        updateTotals();
                    } else {
                        showAlert("Error", "Not enough stock available.");
                    }
                });

                btnSub.setOnAction(event -> {
                    SnackSaleItem item = getTableView().getItems().get(getIndex());
                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                        cartTable.refresh();
                        updateTotals();
                    }
                });

                btnRemove.setOnAction(event -> {
                    SnackSaleItem item = getTableView().getItems().get(getIndex());
                    cartData.remove(item);
                    updateTotals();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        cartTable.setItems(cartData);
    }

    private boolean isStockAvailable(int snackId, int additionalQtyRequested) {
        Snack found = null;
        for (Snack s : allSnacks) {
            if (s.getId() == snackId) {
                found = s; break;
            }
        }
        if (found == null) return false;
        
        int inCartQty = 0;
        for (SnackSaleItem item : cartData) {
            if (item.getSnackId() == snackId) {
                inCartQty += item.getQuantity();
            }
        }
        return found.getQuantity() >= (inCartQty + additionalQtyRequested);
    }

    private void loadInventory() {
        allSnacks = snackDAO.getActiveSnacks();
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
        card.setPrefWidth(180);
        card.getStyleClass().add("movie-grid-card");

        Region imageRegion = new Region();
        imageRegion.setPrefSize(180, 110);
        imageRegion.setMinSize(180, 110);
        imageRegion.setMaxSize(180, 110);
        
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
        detailsBox.setPadding(new Insets(12));

        Label nameLabel = new Label(snack.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web("#111111"));
        nameLabel.setWrapText(true);

        Label categoryLabel = new Label(snack.getCategory());
        categoryLabel.setFont(Font.font("System", 12));
        categoryLabel.setTextFill(Color.web("#888888"));

        Label priceLabel = new Label(String.format("$%.2f", snack.getPrice()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        priceLabel.setTextFill(Color.web("#0066ff"));

        HBox stockBox = new HBox(5);
        Label qtyLabel = new Label("Stock: " + snack.getQuantity());
        qtyLabel.setFont(Font.font("System", 12));
        
        if (snack.getQuantity() < 10) {
            qtyLabel.setTextFill(Color.web("#d9534f")); // Red for low stock
            qtyLabel.setStyle("-fx-font-weight: bold;");
        } else {
            qtyLabel.setTextFill(Color.web("#5cb85c")); // Green for good stock
        }
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        stockBox.getChildren().addAll(qtyLabel, spacer);

        Button addBtn = new Button("Add to Cart");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-cursor: hand; -fx-font-weight: bold;");
        
        if (snack.getQuantity() <= 0) {
            addBtn.setDisable(true);
            addBtn.setText("Out of Stock");
            addBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #888888; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        }
        
        addBtn.setOnAction(e -> handleAddFromCard(snack));

        detailsBox.getChildren().addAll(nameLabel, categoryLabel, priceLabel, stockBox, addBtn);
        card.getChildren().addAll(imageRegion, detailsBox);
        return card;
    }

    private void handleAddFromCard(Snack selected) {
        if (!isStockAvailable(selected.getId(), 1)) {
            showAlert("Stock Error", "Not enough stock available for " + selected.getName() + ".");
            return;
        }

        boolean found = false;
        for (SnackSaleItem item : cartData) {
            if (item.getSnackId() == selected.getId()) {
                item.setQuantity(item.getQuantity() + 1);
                found = true;
                break;
            }
        }

        if (!found) {
            SnackSaleItem newItem = new SnackSaleItem();
            newItem.setSnackId(selected.getId());
            newItem.setSnackName(selected.getName());
            newItem.setPriceAtSale(selected.getPrice());
            newItem.setQuantity(1);
            newItem.setDiscountApplied(BigDecimal.ZERO);
            cartData.add(newItem);
        }

        cartTable.refresh();
        updateTotals();
    }

    @FXML
    public void handleApplyDiscount() {
        String code = discountCodeField.getText();
        if (code == null || code.trim().isEmpty()) {
            currentDiscountPercentage = BigDecimal.ZERO;
            showAlert("Error", "Please enter a discount code.");
            updateTotals();
            return;
        }

        PromoCode promo = promoCodeDAO.getPromoCode(code.trim());
        if (promo != null) {
            currentDiscountPercentage = promo.getDiscountPercentage();
            showAlert("Success", promo.getDiscountPercentage() + "% Discount Applied!");
        } else {
            currentDiscountPercentage = BigDecimal.ZERO;
            showAlert("Error", "Invalid discount code.");
        }
        updateTotals();
    }

    private void updateTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (SnackSaleItem item : cartData) {
            subtotal = subtotal.add(item.getPriceAtSale().multiply(new BigDecimal(item.getQuantity())));
        }

        BigDecimal discountAmt = subtotal.multiply(currentDiscountPercentage).divide(new BigDecimal("100"));
        BigDecimal total = subtotal.subtract(discountAmt);

        subtotalLabel.setText(String.format("$%.2f", subtotal));
        discountLabel.setText(String.format("-$%.2f", discountAmt));
        totalLabel.setText(String.format("$%.2f", total));
    }

    @FXML
    public void handleCheckout() {
        if (cartData.isEmpty()) {
            showAlert("Error", "Cart is empty. Please add items before checking out.");
            return;
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        StringBuilder summary = new StringBuilder();
        summary.append("Order Summary:\n\n");
        
        for (SnackSaleItem item : cartData) {
            subtotal = subtotal.add(item.getPriceAtSale().multiply(new BigDecimal(item.getQuantity())));
            summary.append(item.getSnackName()).append(" (x").append(item.getQuantity()).append(") - $")
                   .append(item.getPriceAtSale().multiply(new BigDecimal(item.getQuantity()))).append("\n");
        }
        
        BigDecimal discountAmt = subtotal.multiply(currentDiscountPercentage).divide(new BigDecimal("100"));
        BigDecimal finalTotal = subtotal.subtract(discountAmt);

        summary.append("\nSubtotal: $").append(subtotal);
        summary.append("\nDiscount (").append(currentDiscountPercentage).append("%): -$").append(discountAmt);
        summary.append("\n------------------------");
        summary.append("\nGrand Total: $").append(finalTotal);

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Sale");
        confirmAlert.setHeaderText("Please review the order before finalizing:");
        confirmAlert.setContentText(summary.toString());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            
            SnackSale sale = new SnackSale();
            sale.setTotalAmount(finalTotal);
            sale.setBookingId(currentBookingId);
            // Defaulting user_id to 1 (Admin) for now as there's no global SessionManager
            sale.setUserId(1);

            List<SnackSaleItem> itemsToSave = new ArrayList<>();
            for (SnackSaleItem cItem : cartData) {
                cItem.setDiscountApplied(currentDiscountPercentage);
                itemsToSave.add(cItem);
            }

            try {
                boolean success = saleDAO.createSale(sale, itemsToSave);
                if (success) {
                    showAlert("Success", "Sale completed successfully! Generating Receipt...");
                    SnackReceiptGenerator.generateAndOpenReceipt(sale, itemsToSave);
                    
                    cartData.clear();
                    currentDiscountPercentage = BigDecimal.ZERO;
                    currentBookingId = null;
                    discountCodeField.clear();
                    customerTypeCombo.setValue("Walk-In Customer");
                    updateTotals();
                    loadInventory(); 
                } else {
                    showAlert("Error", "Database transaction failed. Please check the connection and try again.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "An unexpected error occurred during checkout: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert.AlertType type = title.equals("Success") || title.equals("Auto-Discount Applied") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR;
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
