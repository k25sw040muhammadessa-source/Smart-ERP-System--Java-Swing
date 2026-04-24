package javaapplication1.erp.dao;

import javaapplication1.erp.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductDAO {
    Product create(Product p) throws Exception;
    Optional<Product> findById(int id) throws Exception;
    Optional<Product> findBySKU(String sku) throws Exception;
    List<Product> search(String term, int limit, int offset) throws Exception;
    List<Product> findAll() throws Exception;
    boolean update(Product p) throws Exception;
    boolean delete(int id) throws Exception;
}
