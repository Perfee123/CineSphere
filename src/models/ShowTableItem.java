package models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ShowTableItem {
    private final StringProperty showId;
    private final StringProperty movieTitle;
    private final StringProperty hall;
    private final StringProperty time;
    private final StringProperty seats;
    private final StringProperty status;
    private final StringProperty period;

    public ShowTableItem(String showId, String movieTitle, String hall, String time, String seats, String status, String period) {
        this.showId = new SimpleStringProperty(showId);
        this.movieTitle = new SimpleStringProperty(movieTitle);
        this.hall = new SimpleStringProperty(hall);
        this.time = new SimpleStringProperty(time);
        this.seats = new SimpleStringProperty(seats);
        this.status = new SimpleStringProperty(status);
        this.period = new SimpleStringProperty(period);
    }

    public String getShowId() { return showId.get(); }
    public StringProperty showIdProperty() { return showId; }

    public String getMovieTitle() { return movieTitle.get(); }
    public StringProperty movieTitleProperty() { return movieTitle; }

    public String getHall() { return hall.get(); }
    public StringProperty hallProperty() { return hall; }

    public String getTime() { return time.get(); }
    public StringProperty timeProperty() { return time; }

    public String getSeats() { return seats.get(); }
    public StringProperty seatsProperty() { return seats; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }

    public String getPeriod() { return period.get(); }
    public StringProperty periodProperty() { return period; }

    @Override
    public String toString() {
        return getMovieTitle() + " (" + getHall() + " @ " + getTime() + ")";
    }
}
