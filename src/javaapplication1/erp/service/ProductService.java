package javaapplication1.erp.service;

import javaapplication1.erp.dao.ProductDAO;
import javaapplication1.erp.model.Product;
import javaapplication1.erp.util.ValidationUtil;

import java.util.List;

public class ProductService {
    private final ProductDAO dao;
    public ProductService(ProductDAO dao) { this.dao = dao; }

    public Product createProduct(Product p) throws Exception {
        ValidationUtil.requireNonEmpty(p.getSku(), "SKU");
        ValidationUtil.requireNonEmpty(p.getName(), "Name");
        if (p.getPrice() == null) throw new IllegalArgumentException("Price is required");
        return dao.create(p);
    }

    public boolean updateProduct(Product p) throws Exception {
        ValidationUtil.requireNonEmpty(p.getName(), "Name");
        return dao.update(p);
    }

    public List<Product> search(String term, int limit, int offset) throws Exception {
        return dao.search(term, limit, offset);
    }
}
