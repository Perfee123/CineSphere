package controllers.ticket;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeatSelectionController {

    @FXML private GridPane seatGrid;
    @FXML private Label movieTitleLabel;
    @FXML private Label showtimeLabel;
    @FXML private Label selectedSeatsLabel;
    @FXML private Label adultCountLabel;
    @FXML private Label childCountLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Button proceedBtn;

    private int adultCount = 0;
    private int childCount = 0;
    private final double ADULT_PRICE = 350.0;
    private final double CHILD_PRICE = 200.0;
    
    private List<String> selectedSeats = new ArrayList<>();
    
    private String showId;
    private String movieTitle;
    private String showtimeDetails;

    // Initialization method called by BookingTicketController to pass data
    public void setBookingData(String showId, String title, String details) {
        this.showId = showId;
        this.movieTitle = title;
        this.showtimeDetails = details;
        
        movieTitleLabel.setText(title);
        showtimeLabel.setText(details);
        
        generateSeatGrid();
        updateSummary();
    }

    private void generateSeatGrid() {
        models.BookingDAO dao = new models.BookingDAO();
        int[] dims = dao.getHallDimensions(showId);
        int rows = dims[0];
        int cols = dims[1];
        
        List<String> mockBooked = dao.getBookedSeats(showId);
        
        int hallId = dao.getHallId(showId);
        models.HallDAO hallDAO = new models.HallDAO();
        List<String> maintenanceSeats = hallDAO.getMaintenanceSeats(hallId);
        
        buildSeatGrid(rows, cols, mockBooked, maintenanceSeats);
    }

    // Reusable method for rendering a seat grid given specific data
    public void buildSeatGrid(int rows, int cols, List<String> bookedSeats, List<String> maintenanceSeats) {
        seatGrid.getChildren().clear();
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String seatId = (char)('A' + r) + String.valueOf(c + 1);
                Button seatBtn = new Button(seatId); // Show text inside seat
                seatBtn.setPrefSize(45, 45); // Make it slightly bigger
                seatBtn.setStyle("-fx-font-size: 10px;"); // Ensure it fits
                
                // Add Tooltip for seat number hover
                Tooltip tooltip = new Tooltip(seatId);
                seatBtn.setTooltip(tooltip);
                
                if (maintenanceSeats != null && maintenanceSeats.contains(seatId)) {
                    seatBtn.getStyleClass().add("seat-btn-maintenance");
                    seatBtn.setDisable(true);
                } else if (bookedSeats != null && bookedSeats.contains(seatId)) {
                    seatBtn.getStyleClass().add("seat-btn-booked");
                    seatBtn.setDisable(true);
                } else {
                    seatBtn.getStyleClass().add("seat-btn");
                    seatBtn.setOnAction(e -> handleSeatToggle(seatBtn, seatId));
                }
                
                seatGrid.add(seatBtn, c, r);
            }
        }
    }

    private void handleSeatToggle(Button seatBtn, String seatId) {
        if (selectedSeats.contains(seatId)) {
            selectedSeats.remove(seatId);
            seatBtn.getStyleClass().remove("seat-btn-selected");
            if (!seatBtn.getStyleClass().contains("seat-btn")) {
                seatBtn.getStyleClass().add("seat-btn");
            }
        } else {
            selectedSeats.add(seatId);
            seatBtn.getStyleClass().remove("seat-btn");
            if (!seatBtn.getStyleClass().contains("seat-btn-selected")) {
                seatBtn.getStyleClass().add("seat-btn-selected");
            }
        }
        
        // Auto-increment ticket counter logic
        if (getTotalTickets() < selectedSeats.size()) {
            adultCount++; // Default assign to adult
        } else if (getTotalTickets() > selectedSeats.size()) {
            // Need to decrement
            if (adultCount > 0) adultCount--;
            else if (childCount > 0) childCount--;
        }
        
        updateSummary();
    }

    @FXML
    public void handleClearSeats() {
        selectedSeats.clear();
        adultCount = 0;
        childCount = 0;
        
        for (javafx.scene.Node node : seatGrid.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                if (btn.getStyleClass().contains("seat-btn-selected")) {
                    btn.getStyleClass().remove("seat-btn-selected");
                    if (!btn.getStyleClass().contains("seat-btn")) {
                        btn.getStyleClass().add("seat-btn");
                    }
                }
            }
        }
        updateSummary();
    }

    @FXML
    public void incrementAdult() {
        if (getTotalTickets() < selectedSeats.size()) {
            adultCount++;
            updateSummary();
        }
    }

    @FXML
    public void decrementAdult() {
        if (adultCount > 0) {
            adultCount--;
            updateSummary();
        }
    }

    @FXML
    public void incrementChild() {
        if (getTotalTickets() < selectedSeats.size()) {
            childCount++;
            updateSummary();
        }
    }

    @FXML
    public void decrementChild() {
        if (childCount > 0) {
            childCount--;
            updateSummary();
        }
    }

    private int getTotalTickets() {
        return adultCount + childCount;
    }

    private void updateSummary() {
        if (selectedSeats.isEmpty()) {
            selectedSeatsLabel.setText("-");
        } else {
            selectedSeatsLabel.setText(String.join(", ", selectedSeats));
        }

        adultCountLabel.setText(String.valueOf(adultCount));
        childCountLabel.setText(String.valueOf(childCount));

        double total = (adultCount * ADULT_PRICE) + (childCount * CHILD_PRICE);
        totalAmountLabel.setText(String.format("$%.2f", total));

        // Enable proceed if at least 1 seat selected AND ticket count matches seat count
        if (!selectedSeats.isEmpty() && getTotalTickets() == selectedSeats.size()) {
            proceedBtn.setDisable(false);
            proceedBtn.getStyleClass().remove("primary-action-btn-disabled");
            if (!proceedBtn.getStyleClass().contains("primary-action-btn")) {
                proceedBtn.getStyleClass().add("primary-action-btn");
            }
        } else {
            proceedBtn.setDisable(true);
            proceedBtn.getStyleClass().remove("primary-action-btn");
            if (!proceedBtn.getStyleClass().contains("primary-action-btn-disabled")) {
                proceedBtn.getStyleClass().add("primary-action-btn-disabled");
            }
        }
    }

    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/BookingTicket.fxml"));
            Parent root = loader.load();
            StackPane contentArea = (StackPane) proceedBtn.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleProceed() {
        double total = (adultCount * ADULT_PRICE) + (childCount * CHILD_PRICE);
        String formattedTotal = String.format("$%.2f", total);
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Booking");
        alert.setHeaderText("Please confirm the booking details:");
        alert.setContentText(
            "Movie: " + movieTitle + "\n" +
            "Showtime: " + showtimeDetails + "\n" +
            "Seats: " + String.join(", ", selectedSeats) + "\n" +
            "Adults: " + adultCount + ", Children: " + childCount + "\n" +
            "Total Amount: " + formattedTotal
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            
            // 1. Save Booking to DB
            models.BookingDAO dao = new models.BookingDAO();
            // Using a dummy user ID = 2 for Counter Staff
            String bookingId = dao.createBooking(showId, 2, adultCount, childCount, total, selectedSeats);
            
            if (bookingId != null) {
                System.out.println("Booking confirmed and saved to DB! ID: " + bookingId);
                
                // Route to Booking Confirmed
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/BookingConfirmed.fxml"));
                    Parent root = loader.load();
                    
                    BookingConfirmedController controller = loader.getController();
                    controller.setReceiptData(
                        bookingId,
                        movieTitle,
                        showtimeDetails,
                        String.join(", ", selectedSeats),
                        formattedTotal
                    );
                    
                    StackPane contentArea = (StackPane) proceedBtn.getScene().lookup("#contentArea");
                    if (contentArea != null) {
                        contentArea.getChildren().clear();
                        contentArea.getChildren().add(root);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Alert err = new Alert(Alert.AlertType.ERROR, "Failed to save booking to database!");
                err.show();
            }
        }
    }
}
