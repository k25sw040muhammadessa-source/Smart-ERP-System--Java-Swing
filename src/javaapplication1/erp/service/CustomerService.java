package javaapplication1.erp.service;

import javaapplication1.erp.dao.CustomerDAO;
import javaapplication1.erp.model.Customer;
import javaapplication1.erp.util.ValidationUtil;

import java.util.List;

public class CustomerService {
    private final CustomerDAO dao;

    public CustomerService(CustomerDAO dao) {
        this.dao = dao;
    }

    public Customer createCustomer(Customer c) throws Exception {
        ValidationUtil.requireNonEmpty(c.getName(), "Name");
        if (c.getEmail() != null && !c.getEmail().trim().isEmpty()) ValidationUtil.requireEmail(c.getEmail(), "Email");
        return dao.create(c);
    }

    public boolean updateCustomer(Customer c) throws Exception {
        ValidationUtil.requireNonEmpty(c.getName(), "Name");
        return dao.update(c);
    }

    public List<Customer> search(String term, int limit, int offset) throws Exception {
        return dao.search(term, limit, offset);
    }
}
