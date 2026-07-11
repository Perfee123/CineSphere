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
    @FXML private javafx.scene.layout.VBox qrCodeContainer;

    public void setReceiptData(String bookingId, String movieTitle, String showtime, String seats, String totalPaid) {
        bookingIdLabel.setText("Booking ID: " + bookingId);
        movieTitleLabel.setText(movieTitle);
        showtimeLabel.setText(showtime);
        seatsLabel.setText("Seats: " + seats);
        totalPaidLabel.setText("Total Paid: " + totalPaid);

        // Generate QR Code data string format: BK-1|The Dark Knight|14:00 - Hall A|Seats: A1, A2
        String qrData = bookingId + "|" + movieTitle + "|" + showtime + "|" + seats;
        javafx.scene.image.Image qrImg = utils.QRCodeUtils.generateQRCodeImage(qrData, 120, 120);
        if (qrImg != null) {
            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(qrImg);
            imgView.setFitWidth(120);
            imgView.setFitHeight(120);
            qrCodeContainer.getChildren().clear();
            qrCodeContainer.getChildren().add(imgView);
            qrCodeContainer.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;"); // Remove dashed border
        }
    }

    @FXML
    public void handleDownloadReceipt() {
        javafx.scene.Node receiptCard = bookingIdLabel.getParent().getParent(); // StackPane -> VBox(receipt-card)
        String bId = bookingIdLabel.getText().replace("Booking ID: ", "");
        utils.ReceiptUtils.downloadReceiptAsImage(receiptCard, bookingIdLabel.getScene().getWindow(), "Receipt_" + bId);
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
