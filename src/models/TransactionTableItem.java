package models;

import javafx.beans.property.*;

public class TransactionTableItem {
    private final StringProperty bookingId;
    private final StringProperty dateTime;
    private final StringProperty movieTitle;
    private final IntegerProperty adults;
    private final IntegerProperty kids;
    private final StringProperty soldBy;
    private final StringProperty status;
    private final StringProperty totalAmount;

    public TransactionTableItem(String bookingId, String dateTime, String movieTitle, int adults, int kids, String soldBy, String status, double totalAmount) {
        this.bookingId = new SimpleStringProperty(bookingId);
        this.dateTime = new SimpleStringProperty(dateTime);
        this.movieTitle = new SimpleStringProperty(movieTitle);
        this.adults = new SimpleIntegerProperty(adults);
        this.kids = new SimpleIntegerProperty(kids);
        this.soldBy = new SimpleStringProperty(soldBy);
        this.status = new SimpleStringProperty(status);
        this.totalAmount = new SimpleStringProperty(String.format("$%.2f", totalAmount));
    }

    public String getBookingId() { return bookingId.get(); }
    public StringProperty bookingIdProperty() { return bookingId; }

    public String getDateTime() { return dateTime.get(); }
    public StringProperty dateTimeProperty() { return dateTime; }

    public String getMovieTitle() { return movieTitle.get(); }
    public StringProperty movieTitleProperty() { return movieTitle; }

    public int getAdults() { return adults.get(); }
    public IntegerProperty adultsProperty() { return adults; }

    public int getKids() { return kids.get(); }
    public IntegerProperty kidsProperty() { return kids; }

    public String getSoldBy() { return soldBy.get(); }
    public StringProperty soldByProperty() { return soldBy; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }

    public String getTotalAmount() { return totalAmount.get(); }
    public StringProperty totalAmountProperty() { return totalAmount; }
}
