package models;

import java.sql.Timestamp;
import java.math.BigDecimal;

public class Snack {
    private int id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal costPrice;
    private int quantity;
    private int minStock;
    private String category;
    private String status; // 'ACTIVE', 'INACTIVE'
    private String imagePath;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Snack() {}

    // Constructor without ID (for inserts)
    public Snack(String name, String description, BigDecimal price, BigDecimal costPrice, int quantity, int minStock, String category, String status, String imagePath) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.costPrice = costPrice;
        this.quantity = quantity;
        this.minStock = minStock;
        this.category = category;
        this.status = status;
        this.imagePath = imagePath;
    }

    // Full constructor
    public Snack(int id, String name, String description, BigDecimal price, BigDecimal costPrice, int quantity, int minStock, String category, String status, String imagePath, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.costPrice = costPrice;
        this.quantity = quantity;
        this.minStock = minStock;
        this.category = category;
        this.status = status;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getMinStock() { return minStock; }
    public void setMinStock(int minStock) { this.minStock = minStock; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return name; // Useful for ComboBoxes
    }
}
