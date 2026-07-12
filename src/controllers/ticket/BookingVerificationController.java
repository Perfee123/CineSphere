package controllers.ticket;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import models.BookingTableItem;
import java.util.Optional;

public class BookingVerificationController {

    @FXML private Label bookingIdLabel;
    @FXML private Label movieTitleLabel;
    @FXML private Label showtimeLabel;
    @FXML private Label seatsLabel;
    @FXML private Label totalPaidLabel;
    @FXML private Label ticketBadgeLabel;
    @FXML private javafx.scene.layout.VBox qrCodeContainer;
    
    @FXML private Label currentStatusLabel;
    @FXML private Label systemNotesLabel;
    
    @FXML private Button checkInBtn;
    @FXML private Button cancelBtn;

    private BookingTableItem currentItem;

    public void setBookingData(BookingTableItem item) {
        this.currentItem = item;
        
        bookingIdLabel.setText("Booking ID: " + item.getBookingId());
        movieTitleLabel.setText(item.getMovieTitle());
        showtimeLabel.setText(item.getDate() + " - " + item.getHall());
        seatsLabel.setText("Seats: " + item.getSeats());
        totalPaidLabel.setText(String.format("Total Paid: $%.2f", item.getAmount()));
        
        ticketBadgeLabel.setText(item.getStatus());
        currentStatusLabel.setText(item.getStatus());

        // Generate neat QR Code data
        String qrData = utils.QRCodeUtils.buildTicketPayload(item.getBookingId(), item.getMovieTitle(), item.getDate() + " - " + item.getHall(), item.getSeats());
        javafx.scene.image.Image qrImg = utils.QRCodeUtils.generateQRCodeImage(qrData, 180, 180);
        if (qrImg != null) {
            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(qrImg);
            imgView.setFitWidth(180);
            imgView.setFitHeight(180);
            qrCodeContainer.getChildren().clear();
            qrCodeContainer.getChildren().add(imgView);
            qrCodeContainer.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;"); // Remove dashed border
        }

        updateUIBasedOnStatus();
    }

    private void updateUIBasedOnStatus() {
        String status = currentItem.getStatus();
        
        ticketBadgeLabel.getStyleClass().removeAll("badge-confirmed", "badge-cancelled", "badge-pending");
        
        if ("CHECKED IN".equals(status)) {
            ticketBadgeLabel.getStyleClass().add("badge-confirmed");
            systemNotesLabel.setText("Customer has successfully checked in.");
            checkInBtn.setDisable(true);
            cancelBtn.setDisable(true);
        } else if ("CONFIRMED".equals(status)) {
            ticketBadgeLabel.getStyleClass().add("badge-confirmed");
            systemNotesLabel.setText("This ticket is active and ready for check-in.");
            checkInBtn.setDisable(false);
            cancelBtn.setDisable(false);
        } else if ("CANCELLED".equals(status)) {
            ticketBadgeLabel.getStyleClass().add("badge-cancelled");
            systemNotesLabel.setText("This ticket has been cancelled and refunded.");
            checkInBtn.setDisable(true);
            cancelBtn.setDisable(true);
        } else {
            ticketBadgeLabel.getStyleClass().add("badge-pending");
            systemNotesLabel.setText("This ticket is pending verification.");
            checkInBtn.setDisable(false);
            cancelBtn.setDisable(false);
        }
    }

    @FXML
    public void handleCheckIn() {
        models.BookingDAO dao = new models.BookingDAO();
        if (dao.updateStatus(currentItem.getBookingId(), "CHECKED IN")) {
            currentItem.setStatus("CHECKED IN");
            systemNotesLabel.setText("Customer successfully checked in.");
            checkInBtn.setDisable(true);
            cancelBtn.setDisable(true);
            ticketBadgeLabel.setText("CHECKED IN");
            currentStatusLabel.setText("CHECKED IN");
            System.out.println("Booking marked as CHECKED IN.");
        } else {
            systemNotesLabel.setText("Failed to check in. DB error.");
        }
    }

    @FXML
    public void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Cancellation");
        alert.setHeaderText("Cancel & Refund Ticket");
        alert.setContentText("Are you sure you want to cancel this ticket and process a refund of " + String.format("$%.2f", currentItem.getAmount()) + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            handleCancelRefund();
        }
    }
    
    @FXML
    public void handleCancelRefund() {
        models.BookingDAO dao = new models.BookingDAO();
        if (dao.updateStatus(currentItem.getBookingId(), "CANCELLED")) {
            currentItem.setStatus("CANCELLED");
            systemNotesLabel.setText("Ticket cancelled and refund initiated.");
            checkInBtn.setDisable(true);
            cancelBtn.setDisable(true);
            ticketBadgeLabel.setText("CANCELLED");
            ticketBadgeLabel.getStyleClass().remove("badge-confirmed");
            ticketBadgeLabel.getStyleClass().add("badge-cancelled");
            currentStatusLabel.setText("CANCELLED");
            System.out.println("Booking cancelled.");
        } else {
            systemNotesLabel.setText("Failed to cancel. DB error.");
        }
    }

    @FXML
    public void handleDownloadReceipt() {
        if (currentItem != null) {
            javafx.scene.Node receiptCard = bookingIdLabel.getParent(); // The VBox receipt-card is the parent
            utils.ReceiptUtils.downloadReceiptWithoutBadge(receiptCard, ticketBadgeLabel, bookingIdLabel.getScene().getWindow(), "Receipt_" + currentItem.getBookingId());
        }
    }
    
    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/BookingHistory.fxml"));
            Parent root = loader.load();
            
            StackPane contentArea = (StackPane) bookingIdLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
