package models;

public class Showtime {
    private String id;
    private String time;
    private String hall;
    private int availableSeats;
    private int totalSeats;

    public Showtime(String id, String time, String hall, int availableSeats, int totalSeats) {
        this.id = id;
        this.time = time;
        this.hall = hall;
        this.availableSeats = availableSeats;
        this.totalSeats = totalSeats;
    }

    public String getId() { return id; }
    public String getTime() { return time; }
    public String getHall() { return hall; }
    public int getAvailableSeats() { return availableSeats; }
    public int getTotalSeats() { return totalSeats; }
}
