package models;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class PromoCode {
    private int id;
    private String code;
    private BigDecimal discountPercentage;
    private String status;
    private Timestamp createdAt;

    public PromoCode(int id, String code, BigDecimal discountPercentage, String status, Timestamp createdAt) {
        this.id = id;
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
