SELECT DATABASE();
USE smart_erp;

SHOW TABLES;
SELECT COUNT(*) AS total_users FROM users;
SELECT id, username, email, created_at FROM users ORDER BY id DESC;
SELECT COUNT(*) AS total_products FROM products;
SELECT id, sku, name, qty_in_stock FROM products ORDER BY id DESC;


CREATE DATABASE IF NOT EXISTS smart_erp
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
USE smart_erp;

CREATE TABLE IF NOT EXISTS roles (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS role_permissions (
  role_id INT NOT NULL,
  permission_key VARCHAR(60) NOT NULL,
  PRIMARY KEY (role_id, permission_key),
  CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  role_id INT NOT NULL,
  active TINYINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS employees (
  id INT AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(80) NOT NULL,
  last_name VARCHAR(80) NOT NULL,
  email VARCHAR(100) NOT NULL,
  phone VARCHAR(25),
  hire_date DATE NOT NULL,
  department VARCHAR(80),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customers (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  contact_name VARCHAR(120),
  email VARCHAR(100),
  phone VARCHAR(25),
  address VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS suppliers (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  contact_name VARCHAR(120),
  email VARCHAR(100),
  phone VARCHAR(25),
  address VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
  id INT AUTO_INCREMENT PRIMARY KEY,
  sku VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(150) NOT NULL,
  description TEXT,
  price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  reorder_level INT NOT NULL DEFAULT 5,
  active TINYINT NOT NULL DEFAULT 1,
  qty_in_stock INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS units (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80) NOT NULL UNIQUE,
  symbol VARCHAR(20) NOT NULL UNIQUE,
  active TINYINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory_movements (
  id INT AUTO_INCREMENT PRIMARY KEY,
  product_id INT NOT NULL,
  qty INT NOT NULL,
  type VARCHAR(10) NOT NULL,
  ref_type VARCHAR(30),
  ref_id INT,
  movement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  note VARCHAR(255),
  CONSTRAINT fk_inv_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS sales_orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_no VARCHAR(30) NOT NULL UNIQUE,
  customer_id INT NOT NULL,
  order_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sales_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE IF NOT EXISTS sales_order_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  sales_order_id INT NOT NULL,
  product_id INT NOT NULL,
  qty INT NOT NULL,
  unit_price DECIMAL(12,2) NOT NULL,
  line_total DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_soi_order FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_soi_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS sales_invoices (
  id INT AUTO_INCREMENT PRIMARY KEY,
  invoice_no VARCHAR(30) NOT NULL UNIQUE,
  sales_order_id INT NOT NULL UNIQUE,
  invoice_date DATE NOT NULL,
  amount_due DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
  CONSTRAINT fk_sales_invoice_order FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id)
);

CREATE TABLE IF NOT EXISTS purchases (
  id INT AUTO_INCREMENT PRIMARY KEY,
  purchase_no VARCHAR(30) NOT NULL UNIQUE,
  supplier_id INT NOT NULL,
  purchase_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_purchase_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

CREATE TABLE IF NOT EXISTS purchase_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  purchase_id INT NOT NULL,
  product_id INT NOT NULL,
  qty INT NOT NULL,
  unit_cost DECIMAL(12,2) NOT NULL,
  line_total DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_pi_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(id) ON DELETE CASCADE,
  CONSTRAINT fk_pi_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS purchase_invoices (
  id INT AUTO_INCREMENT PRIMARY KEY,
  invoice_no VARCHAR(30) NOT NULL UNIQUE,
  purchase_id INT NOT NULL UNIQUE,
  invoice_date DATE NOT NULL,
  amount_due DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
  CONSTRAINT fk_purchase_invoice_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NULL,
  action VARCHAR(60) NOT NULL,
  entity_name VARCHAR(60) NOT NULL,
  entity_id INT NULL,
  details VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

SET @sql = (
  SELECT IF(COUNT(*) = 0, 'CREATE INDEX idx_users_role_active ON users(role_id, active)', 'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'users' AND index_name = 'idx_users_role_active'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0, 'CREATE INDEX idx_employees_name ON employees(first_name, last_name)', 'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'employees' AND index_name = 'idx_employees_name'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0, 'CREATE INDEX idx_customers_name ON customers(name)', 'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'customers' AND index_name = 'idx_customers_name'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0, 'CREATE INDEX idx_suppliers_name ON suppliers(name)', 'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'suppliers' AND index_name = 'idx_suppliers_name'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0, 'CREATE INDEX idx_products_name ON products(name)', 'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'products' AND index_name = 'idx_products_name'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0, 'CREATE INDEX idx_products_sku ON products(sku)', 'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'products' AND index_name = 'idx_products_sku'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0, 'CREATE INDEX idx_inv_product_date ON inventory_movements(product_id, movement_date)', 'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'inventory_movements' AND index_name = 'idx_inv_product_date'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0, 'CREATE INDEX idx_inv_date ON inventory_movements(movement_date)', 'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'inventory_movements' AND index_name = 'idx_inv_date'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0, 'CREATE INDEX idx_sales_date ON sales_orders(order_date)', 'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'sales_orders' AND index_name = 'idx_sales_date'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0, 'CREATE INDEX idx_purchase_date ON purchases(purchase_date)', 'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'purchases' AND index_name = 'idx_purchase_date'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO roles (id, name) VALUES (1, 'Admin'), (2, 'User') AS new
ON DUPLICATE KEY UPDATE name = new.name;
INSERT INTO role_permissions (role_id, permission_key) VALUES
(1, 'MANAGE_USERS'),
(1, 'MANAGE_PRODUCTS'),
(1, 'MANAGE_CUSTOMERS'),
(1, 'MANAGE_SUPPLIERS'),
(1, 'MANAGE_EMPLOYEES'),
(1, 'MANAGE_INVENTORY'),
(1, 'CREATE_SALES'),
(1, 'CREATE_PURCHASES'),
(1, 'VIEW_REPORTS'),
(1, 'VIEW_AUDIT_LOGS'),
(2, 'MANAGE_PRODUCTS'),
(2, 'MANAGE_CUSTOMERS'),
(2, 'MANAGE_SUPPLIERS'),
(2, 'MANAGE_INVENTORY'),
(2, 'CREATE_SALES'),
(2, 'VIEW_REPORTS') AS new
ON DUPLICATE KEY UPDATE permission_key = new.permission_key;

INSERT INTO users (username, password_hash, email, role_id, active)
VALUES
('admin', '$2a$12$REPLACE_WITH_BCRYPT_HASH', 'admin@erp.local', 1, 1),
('staff1', '$2a$12$REPLACE_WITH_BCRYPT_HASH', 'staff1@erp.local', 2, 1) AS new
ON DUPLICATE KEY UPDATE
  password_hash = new.password_hash,
  role_id = new.role_id,
  active = new.active,
  email = new.email;

INSERT INTO customers (id, name, contact_name, email, phone, address) VALUES
(1, 'ABC Traders', 'Ali Raza', 'abc@client.com', '+923001111111', 'Lahore'),
(2, 'Noor Retail', 'Umair Aslam', 'noor@client.com', '+923002222222', 'Karachi') AS new
ON DUPLICATE KEY UPDATE
  name = new.name,
  contact_name = new.contact_name,
  email = new.email,
  phone = new.phone,
  address = new.address;

INSERT INTO suppliers (id, name, contact_name, email, phone, address) VALUES
(1, 'Prime Supplies', 'Hamza Khan', 'prime@supplier.com', '+923003333333', 'Islamabad'),
(2, 'Metro Wholesale', 'Asif Malik', 'metro@supplier.com', '+923004444444', 'Faisalabad') AS new
ON DUPLICATE KEY UPDATE
  name = new.name,
  contact_name = new.contact_name,
  email = new.email,
  phone = new.phone,
  address = new.address;

INSERT INTO products (sku, name, description, price, cost, reorder_level, active) VALUES
('P-1000', 'Widget A', 'Standard widget', 19.99, 10.00, 10, 1),
('P-1001', 'Widget B', 'Advanced widget', 29.99, 15.00, 5, 1) AS new
ON DUPLICATE KEY UPDATE
  name = new.name,
  description = new.description,
  price = new.price,
  cost = new.cost,
  reorder_level = new.reorder_level,
  active = new.active;

INSERT INTO units (name, symbol, active) VALUES
('Piece', 'PCS', 1),
('Kilogram', 'KG', 1),
('Liter', 'LTR', 1) AS new
ON DUPLICATE KEY UPDATE
  name = new.name,
  active = new.active;

