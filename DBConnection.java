package retail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/retail_db";

    private DBConnection() {}

    public static Connection get() throws SQLException {
        String url = setting("retail.db.url", "RETAIL_DB_URL", DEFAULT_URL);
        String user = setting("retail.db.user", "RETAIL_DB_USER", "root");
        String password = setting("retail.db.password", "RETAIL_DB_PASSWORD", "1604daksh");
        return DriverManager.getConnection(url, user, password);
    }

    private static String setting(String property, String environment, String fallback) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) value = System.getenv(environment);
        return value == null ? fallback : value;
    }

    /** Called once at startup so problems are reported with a friendly message. */
    public static void test() throws ShopException {
        try (Connection c = get()) {
            if (!c.isValid(3)) throw new ShopException("The database connection is not working.");
        } catch (SQLException e) {
            throw ShopException.from(e);
        }
    }
}
