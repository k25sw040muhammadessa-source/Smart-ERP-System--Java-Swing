package javaapplication1.erp.model;

import java.util.Date;

public class InventoryMovement {
    private int id;
    private int productId;
    private int qty;
    private String type; // "IN" or "OUT"
    private String refType;
    private Integer refId;
    private Date movementDate;
    private String note;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }
    public Integer getRefId() { return refId; }
    public void setRefId(Integer refId) { this.refId = refId; }
    public Date getMovementDate() { return movementDate; }
    public void setMovementDate(Date movementDate) { this.movementDate = movementDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
