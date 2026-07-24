package controllers.snackbar;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.print.PrinterJob;
import javafx.stage.FileChooser;
import models.SnackSale;
import models.SnackSaleDAO;
import models.SnackSaleItem;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class SnackBillsController {

    @FXML private ComboBox<String> dateFilterCombo;
    @FXML private HBox customDateBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private Label totalSalesCountLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label avgSaleLabel;
    @FXML private Label bestSnackLabel;

    @FXML private TabPane tabPane;

    @FXML private TableView<SnackSale> salesTable;
    @FXML private TableColumn<SnackSale, Integer> colSaleId;
    @FXML private TableColumn<SnackSale, Integer> colBookingId;
    @FXML private TableColumn<SnackSale, String> colCashier;
    @FXML private TableColumn<SnackSale, BigDecimal> colAmount;
    @FXML private TableColumn<SnackSale, String> colTime;

    @FXML private Label selectedSaleLabel;
    @FXML private TableView<SnackSaleItem> itemsTable;
    @FXML private TableColumn<SnackSaleItem, String> colItemName;
    @FXML private TableColumn<SnackSaleItem, Integer> colItemQty;
    @FXML private TableColumn<SnackSaleItem, BigDecimal> colItemPrice;
    @FXML private TableColumn<SnackSaleItem, BigDecimal> colItemDiscount;
    @FXML private TableColumn<SnackSaleItem, BigDecimal> colItemTotal;

    @FXML private Button printReceiptBtn;
    
    private SnackSale currentSelectedSale = null;

    private SnackSaleDAO saleDAO = new SnackSaleDAO();
    private ObservableList<SnackSale> salesData = FXCollections.observableArrayList();
    private ObservableList<SnackSaleItem> itemsData = FXCollections.observableArrayList();

    private LocalDate currentStart = null;
    private LocalDate currentEnd = null;

    @FXML
    public void initialize() {
        try {
            setupTables();
            setupFilters();

            salesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    loadSaleItems(newSelection);
                    tabPane.getSelectionModel().select(1); // Auto-switch to the Sale Items tab
                }
            });

            salesTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && salesTable.getSelectionModel().getSelectedItem() != null) {
                    showSaleDetailsDialog(salesTable.getSelectionModel().getSelectedItem());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error initializing SnackBillsController: " + e.getMessage());
        }
    }

    private void setupFilters() {
        dateFilterCombo.setItems(FXCollections.observableArrayList(
            "Today", "Yesterday", "This Week", "This Month", "Custom Date Range", "All Time"
        ));
        dateFilterCombo.setValue("Today");
        
        dateFilterCombo.setOnAction(e -> {
            String selection = dateFilterCombo.getValue();
            if ("Custom Date Range".equals(selection)) {
                customDateBox.setVisible(true);
                customDateBox.setManaged(true);
            } else {
                customDateBox.setVisible(false);
                customDateBox.setManaged(false);
                applyPresetFilter(selection);
            }
        });
        
        applyPresetFilter("Today"); // Load initial data
    }

    private void applyPresetFilter(String selection) {
        LocalDate today = LocalDate.now();
        switch (selection) {
            case "Today":
                currentStart = today;
                currentEnd = today;
                break;
            case "Yesterday":
                currentStart = today.minusDays(1);
                currentEnd = today.minusDays(1);
                break;
            case "This Week":
                currentStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
                currentEnd = today;
                break;
            case "This Month":
                currentStart = today.withDayOfMonth(1);
                currentEnd = today;
                break;
            case "All Time":
                currentStart = null;
                currentEnd = null;
                break;
        }
        loadSalesData();
    }

    @FXML
    public void handleApplyCustomDate() {
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            currentStart = startDatePicker.getValue();
            currentEnd = endDatePicker.getValue();
            loadSalesData();
        }
    }

    private void setupTables() {
        salesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        itemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colSaleId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colCashier.setCellValueFactory(new PropertyValueFactory<>("cashierName"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("saleTime"));

        colItemName.setCellValueFactory(new PropertyValueFactory<>("snackName"));
        colItemQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colItemPrice.setCellValueFactory(new PropertyValueFactory<>("priceAtSale"));
        colItemDiscount.setCellValueFactory(new PropertyValueFactory<>("discountApplied"));

        colItemTotal.setCellValueFactory(cellData -> {
            if (cellData.getValue() != null) {
                return new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getLineTotal());
            }
            return new javafx.beans.property.SimpleObjectProperty<>(BigDecimal.ZERO);
        });
        
        salesTable.setItems(salesData);
        itemsTable.setItems(itemsData);
    }

    private void loadSalesData() {
        List<SnackSale> sales;
        if (currentStart == null && currentEnd == null) {
            sales = saleDAO.getAllSales();
        } else if (currentStart != null && currentStart.equals(currentEnd)) {
            sales = saleDAO.getSalesByDate(currentStart);
        } else {
            sales = saleDAO.getSalesByDateRange(currentStart, currentEnd);
        }

        salesData.setAll(sales);

        BigDecimal revenue = BigDecimal.ZERO;
        for (SnackSale s : sales) {
            if (s.getTotalAmount() != null) {
                revenue = revenue.add(s.getTotalAmount());
            }
        }

        totalSalesCountLabel.setText(String.valueOf(sales.size()));
        totalRevenueLabel.setText(String.format("$%.2f", revenue));
        
        if (sales.size() > 0) {
            BigDecimal avg = revenue.divide(new BigDecimal(sales.size()), 2, RoundingMode.HALF_UP);
            avgSaleLabel.setText(String.format("$%.2f", avg));
        } else {
            avgSaleLabel.setText("$0.00");
        }
        
        String bestSnack = saleDAO.getBestSellingSnack(currentStart, currentEnd);
        bestSnackLabel.setText(bestSnack != null ? bestSnack : "N/A");

        itemsData.clear();
        selectedSaleLabel.setText("No sale selected");
        printReceiptBtn.setVisible(false);
        currentSelectedSale = null;
    }

    private void loadSaleItems(SnackSale sale) {
        currentSelectedSale = sale;
        selectedSaleLabel.setText("Viewing Items for Sale ID: " + sale.getId());
        List<SnackSaleItem> items = saleDAO.getItemsForSale(sale.getId());
        itemsData.setAll(items);
        printReceiptBtn.setVisible(true);
    }

    private void showSaleDetailsDialog(SnackSale sale) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Sale Details - ID: " + sale.getId());
        
        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeBtn);
        
        List<SnackSaleItem> items = saleDAO.getItemsForSale(sale.getId());
        
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20px; -fx-background-color: white;");
        
        TextArea receiptArea = new TextArea();
        receiptArea.setEditable(false);
        receiptArea.setPrefRowCount(15);
        receiptArea.setPrefColumnCount(40);
        receiptArea.setStyle("-fx-font-family: 'Courier New';");
        
        StringBuilder receiptText = new StringBuilder();
        receiptText.append("============= CineSphere =============\n");
        receiptText.append("            SNACK RECEIPT             \n");
        receiptText.append("======================================\n");
        receiptText.append("Sale ID: ").append(sale.getId()).append("\n");
        receiptText.append("Date & Time: ").append(sale.getSaleTime()).append("\n");
        
        String cashier = sale.getCashierName();
        receiptText.append("Cashier: ").append(cashier != null ? cashier : "Admin").append("\n");
        
        if (sale.getBookingId() != null && sale.getBookingId() > 0) {
            receiptText.append("Booking ID: BK-").append(sale.getBookingId()).append("\n");
        }
        
        receiptText.append("--------------------------------------\n");
        receiptText.append("Purchased Items:\n");
        
        BigDecimal subtotal = BigDecimal.ZERO;
        for (SnackSaleItem item : items) {
            receiptText.append(item.getSnackName()).append("\n");
            receiptText.append("  ").append(item.getQuantity()).append(" x $").append(item.getPriceAtSale());
            receiptText.append("  = $").append(item.getLineTotal()).append("\n");
            subtotal = subtotal.add(item.getPriceAtSale().multiply(new BigDecimal(item.getQuantity())));
        }
        
        BigDecimal discountTotal = subtotal.subtract(sale.getTotalAmount());
        
        receiptText.append("--------------------------------------\n");
        if (discountTotal.compareTo(BigDecimal.ZERO) > 0) {
            receiptText.append("Subtotal: $").append(subtotal).append("\n");
            receiptText.append("Discount: -$").append(discountTotal).append("\n");
        }
        receiptText.append("Grand Total: $").append(sale.getTotalAmount()).append("\n");
        receiptText.append("======================================\n");
        
        receiptArea.setText(receiptText.toString());
        vbox.getChildren().add(receiptArea);
        
        dialog.getDialogPane().setContent(vbox);
        dialog.showAndWait();
    }

    @FXML
    public void handlePrintReceipt() {
        if (currentSelectedSale != null && !itemsData.isEmpty()) {
            utils.SnackReceiptGenerator.generateAndOpenReceipt(currentSelectedSale, itemsData);
        }
    }

    @FXML
    public void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("Snack_Sales_Report.csv");
        
        File file = fileChooser.showSaveDialog(salesTable.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Sale ID,Booking ID,Cashier,Total Amount,Sale Time");
                for (SnackSale sale : salesData) {
                    writer.println(
                        sale.getId() + "," + 
                        (sale.getBookingId() != null ? sale.getBookingId() : "") + "," +
                        (sale.getCashierName() != null ? sale.getCashierName() : "Admin") + "," +
                        sale.getTotalAmount() + "," +
                        sale.getSaleTime()
                    );
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("Report exported successfully to:\n" + file.getAbsolutePath());
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handlePrintReport() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean proceed = job.showPrintDialog(salesTable.getScene().getWindow());
            if (proceed) {
                boolean success = job.printPage(salesTable);
                if (success) {
                    job.endJob();
                }
            }
        }
    }
}
