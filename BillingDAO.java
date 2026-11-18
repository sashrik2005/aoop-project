package retail;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BillingDAO {

    private static final String SELECT_SQL =
            "SELECT name, price, stock_qty FROM products WHERE product_id = ? AND is_active = 1 FOR UPDATE";
    private static final String SALE_SQL  = "INSERT INTO sales (total_amount) VALUES (?)";
    private static final String ITEM_SQL  = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
    private static final String STOCK_SQL =
            "UPDATE products SET stock_qty = stock_qty - ? WHERE product_id = ? AND is_active = 1 AND stock_qty >= ?";

    /**
     * Saves the sale, its line items and the stock reduction in ONE transaction.
     * Any failure rolls everything back. Prices come from the database, never from the GUI.
     */
    public Bill generateBill(Cart cart) throws ShopException {
        if (cart == null || cart.isEmpty()) throw new ValidationException("The cart is empty.");

        // lock rows in a consistent order to avoid deadlocks between concurrent bills
        List<Cart.Line> items = cart.lines().stream()
                .sorted(Comparator.comparingInt(Cart.Line::productId)).toList();

        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                List<Cart.Line> billed = new ArrayList<>();
                BigDecimal total = BigDecimal.ZERO;

                // 1) lock + validate every item, take the real price from the DB
                try (PreparedStatement sel = c.prepareStatement(SELECT_SQL)) {
                    for (Cart.Line it : items) {
                        if (it.qty() <= 0) throw new ValidationException("Quantity must be at least 1.");
                        sel.setInt(1, it.productId());
                        try (ResultSet rs = sel.executeQuery()) {
                            if (!rs.next())
                                throw new ShopException("'" + it.name() + "' is no longer available. Remove it from the cart.");
                            String name = rs.getString("name");
                            int stock = rs.getInt("stock_qty");
                            if (stock < it.qty())
                                throw new InsufficientStockException("Not enough stock for '" + name
                                        + "' (available: " + stock + ", requested: " + it.qty() + ").");
                            Cart.Line line = new Cart.Line(it.productId(), name, rs.getBigDecimal("price"), it.qty());
                            billed.add(line);
                            total = total.add(line.subtotal());
                        }
                    }
                }

                // 2) the sale
                int saleId;
                try (PreparedStatement ps = c.prepareStatement(SALE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setBigDecimal(1, total);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("The database did not return the sale ID.");
                        saleId = keys.getInt(1);
                    }
                }

                // 3) line items + stock
                try (PreparedStatement itemPs = c.prepareStatement(ITEM_SQL);
                     PreparedStatement stockPs = c.prepareStatement(STOCK_SQL)) {
                    for (Cart.Line l : billed) {
                        itemPs.setInt(1, saleId);
                        itemPs.setInt(2, l.productId());
                        itemPs.setInt(3, l.qty());
                        itemPs.setBigDecimal(4, l.price());
                        itemPs.addBatch();
                    }
                    itemPs.executeBatch();

                    for (Cart.Line l : billed) {
                        stockPs.setInt(1, l.qty());
                        stockPs.setInt(2, l.productId());
                        stockPs.setInt(3, l.qty());
                        if (stockPs.executeUpdate() != 1)
                            throw new InsufficientStockException("Stock changed while generating the bill. Please refresh and try again.");
                    }
                }

                c.commit();
                return new Bill(saleId, LocalDateTime.now(), total, billed);

            } catch (SQLException | ShopException | RuntimeException e) {
                try {
                    c.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw ShopException.from(e);
        }
    }
}
