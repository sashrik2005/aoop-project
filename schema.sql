CREATE DATABASE IF NOT EXISTS retail_db;
USE retail_db;

CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100)  NOT NULL,
    price      DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    stock_qty  INT           NOT NULL DEFAULT 0 CHECK (stock_qty >= 0),
    is_active  TINYINT(1)    NOT NULL DEFAULT 1   -- soft delete, keeps old bills intact
);

CREATE TABLE sales (
    sale_id      INT AUTO_INCREMENT PRIMARY KEY,
    sale_date    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) NOT NULL
);

CREATE TABLE sale_items (
    sale_item_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id      INT           NOT NULL,
    product_id   INT           NOT NULL,
    quantity     INT           NOT NULL CHECK (quantity > 0),
    unit_price   DECIMAL(10,2) NOT NULL,  -- price at time of sale
    FOREIGN KEY (sale_id)    REFERENCES sales(sale_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- sample data
INSERT INTO products (name, price, stock_qty) VALUES
('Notebook A4',  45.00, 100),
('Ball Pen',     10.00, 250),
('Stapler',      85.50,  30);
