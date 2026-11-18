package retail;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory operations. Methods take the RAW text the user typed, validate it,
 * and translate any database failure into a ShopException with a friendly message.
 */
public class ProductDAO {

    public List<Product> getAll() throws ShopException {
        String sql = "SELECT product_id, name, price, stock_qty FROM products WHERE is_active = 1 ORDER BY name";
        List<Product> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Product(rs.getInt("product_id"), rs.getString("name"),
                                     rs.getBigDecimal("price"), rs.getInt("stock_qty")));
            }
        } catch (SQLException e) {
            throw ShopException.from(e);
        }
        return list;
    }

    public int add(String name, String price, String stock) throws ShopException {
        String n = Validator.name(name);
        BigDecimal p = Validator.price(price);
        int s = Validator.stock(stock);

        String sql = "INSERT INTO products (name, price, stock_qty) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, n);
            ps.setBigDecimal(2, p);
            ps.setInt(3, s);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("The database did not return the new product ID.");
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw ShopException.from(e);
        }
    }

    public void rename(int productId, String name) throws ShopException {
        requireFound(update("UPDATE products SET name = ? WHERE product_id = ? AND is_active = 1",
                Validator.name(name), productId));
    }

    public void changePrice(int productId, String price) throws ShopException {
        requireFound(update("UPDATE products SET price = ? WHERE product_id = ? AND is_active = 1",
                Validator.price(price), productId));
    }

    public void changeStock(int productId, String stock) throws ShopException {
        requireFound(update("UPDATE products SET stock_qty = ? WHERE product_id = ? AND is_active = 1",
                Validator.stock(stock), productId));
    }

    /** Soft delete so past bills still reference a valid product row. */
    public void remove(int productId) throws ShopException {
        requireFound(update("UPDATE products SET is_active = 0 WHERE product_id = ?", productId));
    }

    // ---------- helpers ----------
    private static int update(String sql, Object... params) throws ShopException {
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw ShopException.from(e);
        }
    }

    private static void requireFound(int rows) throws ShopException {
        if (rows == 0) throw new ShopException("That product no longer exists. The list has been refreshed.");
    }
}
