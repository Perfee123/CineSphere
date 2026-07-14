package controllers.ticket;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class BookingConfirmedController {

    @FXML private Label bookingIdLabel;
    @FXML private Label movieTitleLabel;
    @FXML private Label showtimeLabel;
    @FXML private Label seatsLabel;
    @FXML private Label totalPaidLabel;
    @FXML private Label ticketBadgeLabel;
    @FXML private javafx.scene.layout.VBox qrCodeContainer;
    
    @FXML private Label displayMovieTitle;
    @FXML private Label displayShowtime;
    @FXML private Label displaySeats;
    @FXML private javafx.scene.layout.VBox displayQrCodeContainer;

    private String currentBookingId;

    public void setReceiptData(String bookingId, String movieTitle, String showtime, String seats, String totalPaid) {
        this.currentBookingId = bookingId;
        bookingIdLabel.setText("Booking ID: " + bookingId);
        movieTitleLabel.setText(movieTitle);
        showtimeLabel.setText(showtime);
        seatsLabel.setText("Seats: " + seats);
        totalPaidLabel.setText("Total Paid: " + totalPaid);

        displayMovieTitle.setText(movieTitle);
        displayShowtime.setText(showtime);
        displaySeats.setText(seats);

        // Generate neat QR Code data
        String qrData = utils.QRCodeUtils.buildTicketPayload(bookingId, movieTitle, showtime, seats);
        
        // Setup hidden receipt QR
        javafx.scene.image.Image qrImg = utils.QRCodeUtils.generateQRCodeImage(qrData, 180, 180);
        if (qrImg != null) {
            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(qrImg);
            imgView.setFitWidth(180);
            imgView.setFitHeight(180);
            qrCodeContainer.getChildren().clear();
            qrCodeContainer.getChildren().add(imgView);
            qrCodeContainer.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;"); 
        }

        // Setup display QR using the same image
        if (qrImg != null) {
            javafx.scene.image.ImageView displayImgView = new javafx.scene.image.ImageView(qrImg);
            displayImgView.setFitWidth(150);
            displayImgView.setFitHeight(150);
            displayImgView.setPreserveRatio(true);
            displayQrCodeContainer.getChildren().clear();
            displayQrCodeContainer.getChildren().add(displayImgView);
        }
    }

    @FXML
    public void handleDownloadReceipt() {
        javafx.scene.Node receiptCard = bookingIdLabel.getParent(); // The VBox receipt-card
        utils.ReceiptUtils.downloadReceiptWithoutBadge(receiptCard, ticketBadgeLabel, bookingIdLabel.getScene().getWindow(), "Receipt_" + currentBookingId);
    }

    @FXML
    public void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/TicketOverview.fxml"));
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

    @FXML
    public void handleTicketDesk() {
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
