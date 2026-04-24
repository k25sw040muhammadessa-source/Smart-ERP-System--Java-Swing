package javaapplication1.erp.model;

public class PurchaseItem {
    private int id;
    private int purchaseId;
    private int productId;
    private int qty;
    private double price;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPurchaseId() { return purchaseId; }
    public void setPurchaseId(int purchaseId) { this.purchaseId = purchaseId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
