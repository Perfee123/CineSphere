package controllers.scheduler;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import models.Discount;
import models.DiscountDAO;
import models.Movie;
import models.MovieDAO;
import models.ShowDAO;
import models.ShowTableItem;
import models.PromoCode;
import models.PromoCodeDAO;
import models.Snack;
import models.SnackDAO;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DiscountManagementController implements Initializable {

    // --- Direct Discounts Tab ---
    @FXML private TableView<Discount> discountsTable;
    @FXML private TableColumn<Discount, Integer> idColumn;
    @FXML private TableColumn<Discount, String> targetTypeColumn;
    @FXML private TableColumn<Discount, String> targetNameColumn;
    @FXML private TableColumn<Discount, BigDecimal> discountPercentageColumn;
    @FXML private TableColumn<Discount, String> statusColumn;
    @FXML private TableColumn<Discount, Void> discountActionColumn;

    // --- Promo Codes Tab ---
    @FXML private TableView<PromoCode> promoTable;
    @FXML private TableColumn<PromoCode, Integer> promoIdColumn;
    @FXML private TableColumn<PromoCode, String> promoCodeColumn;
    @FXML private TableColumn<PromoCode, BigDecimal> promoDiscountColumn;
    @FXML private TableColumn<PromoCode, String> promoStatusColumn;
    @FXML private TableColumn<PromoCode, Void> promoActionColumn;

    private DiscountDAO discountDAO = new DiscountDAO();
    private MovieDAO movieDAO = new MovieDAO();
    private ShowDAO showDAO = new ShowDAO();
    private PromoCodeDAO promoCodeDAO = new PromoCodeDAO();
    private SnackDAO snackDAO = new SnackDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDiscountsTable();
        setupPromoTable();
        
        loadDiscountData();
        loadPromoData();
    }

    // --- DIRECT DISCOUNTS LOGIC ---
    private void setupDiscountsTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        targetTypeColumn.setCellValueFactory(new PropertyValueFactory<>("targetType"));
        targetNameColumn.setCellValueFactory(new PropertyValueFactory<>("targetName"));
        discountPercentageColumn.setCellValueFactory(new PropertyValueFactory<>("discountPercentage"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        discountActionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
                deleteBtn.setOnAction(event -> {
                    Discount d = getTableView().getItems().get(getIndex());
                    handleDeleteDiscount(d);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(deleteBtn);
                    box.setStyle("-fx-alignment: center;");
                    setGraphic(box);
                }
            }
        });
    }

    private void loadDiscountData() {
        List<Discount> discounts = discountDAO.getAllDiscounts();
        
        for (Discount d : discounts) {
            if ("MOVIE".equals(d.getTargetType())) {
                Movie m = movieDAO.getMovieById(d.getTargetId());
                if (m != null) d.setTargetName(m.getTitle());
            } else if ("SHOW".equals(d.getTargetType())) {
                d.setTargetName("Show ID: " + d.getTargetId());
            } else if ("SNACK".equals(d.getTargetType())) {
                Snack snack = snackDAO.getAllSnacks().stream()
                    .filter(s -> s.getId() == d.getTargetId())
                    .findFirst()
                    .orElse(null);
                if (snack != null) {
                    d.setTargetName(snack.getName());
                } else {
                    d.setTargetName("Snack ID: " + d.getTargetId());
                }
            }
        }
        
        ObservableList<Discount> observableList = FXCollections.observableArrayList(discounts);
        discountsTable.setItems(observableList);
    }
    
    @FXML
    public void openAddDiscountDialog(ActionEvent event) {
        Dialog<Discount> dialog = new Dialog<>();
        dialog.setTitle("Add Direct Discount");
        dialog.setHeaderText("Create a new discount for a Movie or Show");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("SHOW", "MOVIE", "SNACK"));
        typeCombo.setPromptText("Target Type");
        
        ComboBox<Object> itemCombo = new ComboBox<>();
        itemCombo.setPromptText("Select Target");
        itemCombo.setPrefWidth(200);
        
        typeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            itemCombo.getItems().clear();
            if ("MOVIE".equals(newVal)) {
                itemCombo.setItems(FXCollections.observableArrayList(movieDAO.getAllMovies()));
            } else if ("SHOW".equals(newVal)) {
                itemCombo.setItems(FXCollections.observableArrayList(showDAO.getUpcomingShows()));
            } else if ("SNACK".equals(newVal)) {
                itemCombo.setItems(FXCollections.observableArrayList(snackDAO.getAllSnacks()));
            }
        });

        TextField percentageField = new TextField();
        percentageField.setPromptText("e.g. 15.5");

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
        statusCombo.setValue("ACTIVE");

        grid.add(new Label("Target Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Target Item:"), 0, 1);
        grid.add(itemCombo, 1, 1);
        grid.add(new Label("Discount %:"), 0, 2);
        grid.add(percentageField, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(statusCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String type = typeCombo.getValue();
                    Object item = itemCombo.getValue();
                    String pctStr = percentageField.getText();
                    String status = statusCombo.getValue();
                    
                    if (type == null || item == null || pctStr.isEmpty() || status == null) return null;
                    
                    BigDecimal pct = new BigDecimal(pctStr);
                    int targetId = 0;
                    if (item instanceof Movie) {
                        targetId = Integer.parseInt(((Movie)item).getId().replace("M", ""));
                    } else if (item instanceof ShowTableItem) {
                        targetId = Integer.parseInt(((ShowTableItem)item).getShowId().replace("SH-", ""));
                    } else if (item instanceof Snack) {
                        targetId = ((Snack)item).getId();
                    }

                    Discount d = new Discount(0, type, targetId, pct, status, null, null);
                    return d;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Discount> result = dialog.showAndWait();
        result.ifPresent(discount -> {
            discountDAO.addDiscount(discount);
            loadDiscountData();
        });
    }

    private void handleDeleteDiscount(Discount discount) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Discount");
        alert.setHeaderText("Delete Discount ID: " + discount.getId() + "?");
        alert.setContentText("Are you sure you want to permanently delete this discount?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (discountDAO.deleteDiscount(discount.getId())) {
                loadDiscountData();
            }
        }
    }

    // --- PROMO CODES LOGIC ---
    private void setupPromoTable() {
        promoIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        promoCodeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        promoDiscountColumn.setCellValueFactory(new PropertyValueFactory<>("discountPercentage"));
        promoStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        promoActionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
                deleteBtn.setOnAction(event -> {
                    PromoCode p = getTableView().getItems().get(getIndex());
                    handleDeletePromo(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(deleteBtn);
                    box.setStyle("-fx-alignment: center;");
                    setGraphic(box);
                }
            }
        });
    }

    private void loadPromoData() {
        List<PromoCode> promos = promoCodeDAO.getAllPromoCodes();
        ObservableList<PromoCode> observableList = FXCollections.observableArrayList(promos);
        promoTable.setItems(observableList);
    }
    
    @FXML
    public void openAddPromoDialog(ActionEvent event) {
        Dialog<PromoCode> dialog = new Dialog<>();
        dialog.setTitle("Add Promo Code");
        dialog.setHeaderText("Create a new global Promo Code");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField codeField = new TextField();
        codeField.setPromptText("e.g. SUMMER20");
        
        TextField percentageField = new TextField();
        percentageField.setPromptText("e.g. 20.0");

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
        statusCombo.setValue("ACTIVE");

        grid.add(new Label("Promo Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Discount %:"), 0, 1);
        grid.add(percentageField, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(statusCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String code = codeField.getText().trim().toUpperCase();
                    String pctStr = percentageField.getText();
                    String status = statusCombo.getValue();
                    
                    if (code.isEmpty() || pctStr.isEmpty() || status == null) return null;
                    
                    BigDecimal pct = new BigDecimal(pctStr);
                    return new PromoCode(0, code, pct, status, null);
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        Optional<PromoCode> result = dialog.showAndWait();
        result.ifPresent(promo -> {
            promoCodeDAO.addPromoCode(promo);
            loadPromoData();
        });
    }

    private void handleDeletePromo(PromoCode promo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Promo Code");
        alert.setHeaderText("Delete Promo: " + promo.getCode() + "?");
        alert.setContentText("Are you sure you want to permanently delete this promo code?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (promoCodeDAO.deletePromoCode(promo.getId())) {
                loadPromoData();
            }
        }
    }
}
