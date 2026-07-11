package controllers.ticket;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import models.BookingTableItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BookingHistoryController {

    @FXML private ComboBox<String> timeFilterCombo;
    @FXML private TextField searchField;
    @FXML private TableView<BookingTableItem> historyTable;
    @FXML private TableColumn<BookingTableItem, String> colId;
    @FXML private TableColumn<BookingTableItem, String> colDate;
    @FXML private TableColumn<BookingTableItem, String> colMovie;
    @FXML private TableColumn<BookingTableItem, String> colHall;
    @FXML private TableColumn<BookingTableItem, Integer> colTickets;
    @FXML private TableColumn<BookingTableItem, String> colStatus;
    @FXML private TableColumn<BookingTableItem, Double> colAmount;
    @FXML private Label totalEarningsLabel;
    @FXML private Label totalTicketsLabel;
    @FXML private Label confirmedTicketsLabel;
    @FXML private Label cancelledTicketsLabel;

    private ObservableList<BookingTableItem> bookingList;

    @FXML
    public void initialize() {
        // Init Filter Combo
        timeFilterCombo.setItems(FXCollections.observableArrayList("Today", "Yesterday", "Last 7 Days", "This Month"));
        timeFilterCombo.getSelectionModel().selectFirst();

        // Init Columns
        colId.setCellValueFactory(cellData -> cellData.getValue().bookingIdProperty());
        colDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        colMovie.setCellValueFactory(cellData -> cellData.getValue().movieTitleProperty());
        colHall.setCellValueFactory(cellData -> cellData.getValue().hallProperty());
        colTickets.setCellValueFactory(cellData -> cellData.getValue().ticketsProperty().asObject());
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        colAmount.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());

        // Custom Cell Formatting for Amount
        colAmount.setCellFactory(column -> new TableCell<BookingTableItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        // Custom Row Formatting for Colors
        historyTable.setRowFactory(tv -> new TableRow<BookingTableItem>() {
            @Override
            protected void updateItem(BookingTableItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("table-row-checkedin", "table-row-cancelled");
                if (item == null || empty) {
                    setStyle("");
                } else {
                    if ("CHECKED_IN".equals(item.getStatus()) || "CHECKED IN".equals(item.getStatus())) {
                        getStyleClass().add("table-row-checkedin");
                    } else if ("CANCELLED".equals(item.getStatus())) {
                        getStyleClass().add("table-row-cancelled");
                    }
                    // CONFIRMED is normal (white) by default
                }
            }
        });

        // Handle Row Double Click
        historyTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && (!historyTable.getSelectionModel().isEmpty())) {
                BookingTableItem selectedData = historyTable.getSelectionModel().getSelectedItem();
                openVerificationView(selectedData);
            }
        });

        loadDummyData();
    }

    private void loadDummyData() {
        models.BookingDAO dao = new models.BookingDAO();
        bookingList = FXCollections.observableArrayList(dao.getAllBookings());
        javafx.collections.transformation.FilteredList<BookingTableItem> filteredData = new javafx.collections.transformation.FilteredList<>(bookingList, p -> true);
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(booking -> {
                    if (newValue == null || newValue.isEmpty()) return true;
                    String filterStr = newValue;
                    if (filterStr.contains("|")) {
                        filterStr = filterStr.split("\\|")[0].trim(); // Extract BK-123 from the scanned QR string
                    }
                    String lowerCaseFilter = filterStr.toLowerCase();
                    return booking.getMovieTitle().toLowerCase().contains(lowerCaseFilter) ||
                           booking.getBookingId().toLowerCase().contains(lowerCaseFilter);
                });
                calculateTotal(); // Update stats whenever search changes
            });
        }
        historyTable.setItems(filteredData);
        
        // Initial calculation
        calculateTotal();
    }

    private void calculateTotal() {
        double totalEarnings = 0;
        int totalTickets = 0;
        int confirmed = 0;
        int cancelled = 0;

        for (BookingTableItem item : historyTable.getItems()) {
            totalTickets += item.getTickets();
            if ("CONFIRMED".equals(item.getStatus()) || "CHECKED IN".equals(item.getStatus())) {
                totalEarnings += item.getAmount();
                confirmed += item.getTickets();
            } else if ("CANCELLED".equals(item.getStatus())) {
                cancelled += item.getTickets();
            }
        }
        totalEarningsLabel.setText(String.format("$%.2f", totalEarnings));
        totalTicketsLabel.setText("Total Tickets: " + totalTickets);
        confirmedTicketsLabel.setText("Confirmed: " + confirmed);
        cancelledTicketsLabel.setText("Cancellations: " + cancelled);
    }

    private void openVerificationView(BookingTableItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/BookingVerification.fxml"));
            Parent root = loader.load();
            
            BookingVerificationController controller = loader.getController();
            controller.setBookingData(item);
            
            VBox parent = (VBox) historyTable.getParent();
            parent.getChildren().clear();
            parent.getChildren().add(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDownloadReceipt() {
        BookingTableItem selectedData = historyTable.getSelectionModel().getSelectedItem();
        if (selectedData == null) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("No Ticket Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a ticket from the table first, or double-click to view its details.");
            alert.showAndWait();
        } else {
            openVerificationView(selectedData);
        }
    }
}
