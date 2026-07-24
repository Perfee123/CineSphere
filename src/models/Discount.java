package models;

import java.sql.Timestamp;
import java.math.BigDecimal;

public class Discount {
    private int id;
    private String targetType; // 'SHOW', 'SNACK', 'MOVIE'
    private int targetId;
    private BigDecimal discountPercentage;
    private String status; // 'ACTIVE', 'INACTIVE'
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // UI Helpers (Optional but useful for tables)
    private String targetName; 

    public Discount(int id, String targetType, int targetId, BigDecimal discountPercentage, String status, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
        this.discountPercentage = discountPercentage;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public int getTargetId() { return targetId; }
    public void setTargetId(int targetId) { this.targetId = targetId; }

    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
}
