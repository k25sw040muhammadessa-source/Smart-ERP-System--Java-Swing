package javaapplication1.erp.service;

import javaapplication1.erp.dao.EmployeeDAO;
import javaapplication1.erp.model.Employee;
import javaapplication1.erp.util.ValidationUtil;

import java.util.List;

/**
 * Employee business rules.
 */
public class EmployeeService {
    private final EmployeeDAO dao;

    public EmployeeService(EmployeeDAO dao) {
        this.dao = dao;
    }

    public Employee createEmployee(Employee employee) throws Exception {
        ValidationUtil.requireNonEmpty(employee.getFirstName(), "First name");
        ValidationUtil.requireNonEmpty(employee.getLastName(), "Last name");
        ValidationUtil.requireEmail(employee.getEmail(), "Email");
        ValidationUtil.requirePhone(employee.getPhone(), "Phone");
        if (employee.getHireDate() == null) {
            throw new IllegalArgumentException("Hire date is required");
        }
        return dao.create(employee);
    }

    public boolean updateEmployee(Employee employee) throws Exception {
        ValidationUtil.requireNonEmpty(employee.getFirstName(), "First name");
        ValidationUtil.requireNonEmpty(employee.getLastName(), "Last name");
        ValidationUtil.requireEmail(employee.getEmail(), "Email");
        ValidationUtil.requirePhone(employee.getPhone(), "Phone");
        if (employee.getHireDate() == null) {
            throw new IllegalArgumentException("Hire date is required");
        }
        return dao.update(employee);
    }

    public List<Employee> search(String term, int limit, int offset) throws Exception {
        return dao.search(term, limit, offset);
    }
}
