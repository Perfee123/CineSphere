package models;

import java.math.BigDecimal;

public class BookingPOSDetails {
    private String movieName;
    private String hallName;
    private String seatNumbers;
    private String showTime;
    private BigDecimal snackDiscount;

    public BookingPOSDetails(String movieName, String hallName, String seatNumbers, String showTime, BigDecimal snackDiscount) {
        this.movieName = movieName;
        this.hallName = hallName;
        this.seatNumbers = seatNumbers;
        this.showTime = showTime;
        this.snackDiscount = snackDiscount;
    }

    public String getMovieName() { return movieName; }
    public String getHallName() { return hallName; }
    public String getSeatNumbers() { return seatNumbers; }
    public String getShowTime() { return showTime; }
    public BigDecimal getSnackDiscount() { return snackDiscount; }
}
