package javaapplication1.erp.dao.impl;

import javaapplication1.erp.dao.SalesOrderDAO;
import javaapplication1.erp.model.SalesItem;
import javaapplication1.erp.model.SalesOrder;
import javaapplication1.erp.util.DBUtil;
import javaapplication1.erp.util.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalesOrderDAOImpl implements SalesOrderDAO {
    private static final String INSERT_SQL = "INSERT INTO sales_orders (order_no,customer_id,order_date,total_amount,status) VALUES (?,?,?,?,?)";
    private static final String INSERT_ITEM_SQL = "INSERT INTO sales_order_items (sales_order_id,product_id,qty,unit_price,line_total) VALUES (?,?,?,?,?)";
    private static final String SELECT_BY_ID = "SELECT * FROM sales_orders WHERE id = ?";
    private static final String SEARCH_SQL = "SELECT * FROM sales_orders WHERE customer_id = ? LIMIT ? OFFSET ?";
    private static final String DELETE_SQL = "DELETE FROM sales_orders WHERE id = ?";

    @Override
    public SalesOrder create(SalesOrder so) throws Exception {
        try {
            try (Connection conn = DBUtil.getConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    String orderNo = "SO-LEGACY-" + System.currentTimeMillis();
                    ps.setString(1, orderNo);
                    ps.setInt(2, so.getCustomerId());
                    ps.setDate(3, so.getOrderDate() == null ? null : new java.sql.Date(so.getOrderDate().getTime()));
                    ps.setDouble(4, so.getTotalAmount());
                    ps.setString(5, so.getStatus());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) so.setId(rs.getInt(1)); }
                }
                if (so.getItems() != null) {
                    try (PreparedStatement psi = conn.prepareStatement(INSERT_ITEM_SQL)) {
                        for (SalesItem it : so.getItems()) {
                            psi.setInt(1, so.getId());
                            psi.setInt(2, it.getProductId());
                            psi.setInt(3, it.getQty());
                            psi.setDouble(4, it.getPrice());
                            psi.setDouble(5, it.getQty() * it.getPrice());
                            psi.addBatch();
                        }
                        psi.executeBatch();
                    }
                }
                conn.commit();
                conn.setAutoCommit(true);
                return so;
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to create sales order", ex);
        }
    }

    @Override
    public Optional<SalesOrder> findById(int id) throws Exception {
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(mapRow(rs)); }
                return Optional.empty();
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to find sales order by id", ex);
        }
    }

    @Override
    public List<SalesOrder> search(String term, int limit, int offset) throws Exception {
        List<SalesOrder> list = new ArrayList<>();
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SEARCH_SQL)) {
                ps.setInt(1, Integer.parseInt(term));
                ps.setInt(2, limit);
                ps.setInt(3, offset);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRow(rs)); }
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to search sales orders", ex);
        }
        return list;
    }

    @Override
    public boolean update(SalesOrder so) throws Exception { throw new UnsupportedOperationException("Update via business flow"); }

    @Override
    public boolean delete(int id) throws Exception {
        try {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Failed to delete sales order", ex);
        }
    }

    private SalesOrder mapRow(ResultSet rs) throws java.sql.SQLException {
        SalesOrder so = new SalesOrder();
        so.setId(rs.getInt("id"));
        so.setCustomerId(rs.getInt("customer_id"));
        so.setOrderDate(rs.getDate("order_date"));
        so.setTotalAmount(rs.getDouble("total_amount"));
        so.setStatus(rs.getString("status"));
        return so;
    }
}
