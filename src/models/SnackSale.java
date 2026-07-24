package models;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public class SnackSale {
    private int id;
    private Integer bookingId;
    private Integer userId;
    private BigDecimal totalAmount;
    private Timestamp saleTime;
    private String cashierName;
    
    // Additional field to hold items
    private List<SnackSaleItem> items;

    public SnackSale() {}

    public SnackSale(int id, Integer bookingId, Integer userId, BigDecimal totalAmount, Timestamp saleTime) {
        this.id = id;
        this.bookingId = bookingId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.saleTime = saleTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Timestamp getSaleTime() { return saleTime; }
    public void setSaleTime(Timestamp saleTime) { this.saleTime = saleTime; }

    public List<SnackSaleItem> getItems() { return items; }
    public void setItems(List<SnackSaleItem> items) { this.items = items; }
}
