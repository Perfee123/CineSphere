package models;

import javafx.beans.property.*;

public class BookingTableItem {
    private final StringProperty bookingId;
    private final StringProperty date;
    private final StringProperty movieTitle;
    private final StringProperty hall;
    private final IntegerProperty tickets;
    private final StringProperty status;
    private final DoubleProperty amount;
    private final StringProperty seats;

    public BookingTableItem(String bookingId, String date, String movieTitle, String hall, int tickets, String status, double amount, String seats) {
        this.bookingId = new SimpleStringProperty(bookingId);
        this.date = new SimpleStringProperty(date);
        this.movieTitle = new SimpleStringProperty(movieTitle);
        this.hall = new SimpleStringProperty(hall);
        this.tickets = new SimpleIntegerProperty(tickets);
        this.status = new SimpleStringProperty(status);
        this.amount = new SimpleDoubleProperty(amount);
        this.seats = new SimpleStringProperty(seats);
    }

    public String getBookingId() { return bookingId.get(); }
    public StringProperty bookingIdProperty() { return bookingId; }

    public String getDate() { return date.get(); }
    public StringProperty dateProperty() { return date; }

    public String getMovieTitle() { return movieTitle.get(); }
    public StringProperty movieTitleProperty() { return movieTitle; }

    public String getHall() { return hall.get(); }
    public StringProperty hallProperty() { return hall; }

    public int getTickets() { return tickets.get(); }
    public IntegerProperty ticketsProperty() { return tickets; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    public void setStatus(String status) { this.status.set(status); }

    public double getAmount() { return amount.get(); }
    public DoubleProperty amountProperty() { return amount; }

    public String getSeats() { return seats.get(); }
    public StringProperty seatsProperty() { return seats; }
}
