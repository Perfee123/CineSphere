package models;

import java.sql.Timestamp;

public class Hall {
    private int id;
    private String name;
    private int totalSeats;
    private int seatRows;
    private int seatColumns;
    private String type;
    private String status;
    private boolean isKidsHall;
    private Timestamp createdAt;

    public Hall(int id, String name, String type, int totalSeats, int seatRows, int seatColumns, String status, boolean isKidsHall, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.totalSeats = totalSeats;
        this.seatRows = seatRows;
        this.seatColumns = seatColumns;
        this.status = status;
        this.isKidsHall = isKidsHall;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getTotalSeats() { return totalSeats; }
    public int getSeatRows() { return seatRows; }
    public int getSeatColumns() { return seatColumns; }
    public String getStatus() { return status; }
    public boolean isKidsHall() { return isKidsHall; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public void setSeatRows(int seatRows) { this.seatRows = seatRows; }
    public void setSeatColumns(int seatColumns) { this.seatColumns = seatColumns; }
    public void setStatus(String status) { this.status = status; }
    public void setKidsHall(boolean kidsHall) { isKidsHall = kidsHall; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name; // Useful for ComboBox displays
    }
}
