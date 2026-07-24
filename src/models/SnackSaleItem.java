package models;

import java.math.BigDecimal;

public class SnackSaleItem {
    private int id;
    private int snackSaleId;
    private int snackId;
    private int quantity;
    private BigDecimal priceAtSale;
    private BigDecimal discountApplied;
    
    // Additional transient fields for UI convenience
    private String snackName;

    public SnackSaleItem() {}

    public SnackSaleItem(int id, int snackSaleId, int snackId, int quantity, BigDecimal priceAtSale, BigDecimal discountApplied) {
        this.id = id;
        this.snackSaleId = snackSaleId;
        this.snackId = snackId;
        this.quantity = quantity;
        this.priceAtSale = priceAtSale;
        this.discountApplied = discountApplied;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSnackSaleId() { return snackSaleId; }
    public void setSnackSaleId(int snackSaleId) { this.snackSaleId = snackSaleId; }

    public int getSnackId() { return snackId; }
    public void setSnackId(int snackId) { this.snackId = snackId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getPriceAtSale() { return priceAtSale; }
    public void setPriceAtSale(BigDecimal priceAtSale) { this.priceAtSale = priceAtSale; }

    public BigDecimal getDiscountApplied() { return discountApplied; }
    public void setDiscountApplied(BigDecimal discountApplied) { this.discountApplied = discountApplied; }
    
    public String getSnackName() { return snackName; }
    public void setSnackName(String snackName) { this.snackName = snackName; }
    
    // Helper to calculate total for this line item
    public BigDecimal getLineTotal() {
        if (priceAtSale == null) return BigDecimal.ZERO;
        BigDecimal baseTotal = priceAtSale.multiply(new BigDecimal(quantity));
        if (discountApplied != null && discountApplied.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountAmt = baseTotal.multiply(discountApplied).divide(new BigDecimal(100));
            return baseTotal.subtract(discountAmt);
        }
        return baseTotal;
    }
}
