package javaapplication1.erp.service;

import javaapplication1.erp.dao.SupplierDAO;
import javaapplication1.erp.model.Supplier;
import javaapplication1.erp.util.ValidationUtil;

import java.util.List;

/**
 * Supplier business rules.
 */
public class SupplierService {
    private final SupplierDAO dao;

    public SupplierService(SupplierDAO dao) {
        this.dao = dao;
    }

    public Supplier createSupplier(Supplier supplier) throws Exception {
        ValidationUtil.requireNonEmpty(supplier.getName(), "Name");
        ValidationUtil.requirePhone(supplier.getPhone(), "Phone");
        if (supplier.getEmail() != null && !supplier.getEmail().trim().isEmpty()) {
            ValidationUtil.requireEmail(supplier.getEmail(), "Email");
        }
        return dao.create(supplier);
    }

    public boolean updateSupplier(Supplier supplier) throws Exception {
        ValidationUtil.requireNonEmpty(supplier.getName(), "Name");
        ValidationUtil.requirePhone(supplier.getPhone(), "Phone");
        if (supplier.getEmail() != null && !supplier.getEmail().trim().isEmpty()) {
            ValidationUtil.requireEmail(supplier.getEmail(), "Email");
        }
        return dao.update(supplier);
    }

    public List<Supplier> search(String term, int limit, int offset) throws Exception {
        return dao.search(term, limit, offset);
    }
}
