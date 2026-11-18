package retail;

import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;

/**
 * The ONE error type the GUI has to know about. Its message is always safe to show to the shop owner.
 * Raw database errors are translated into friendly messages by {@link #from(SQLException)}.
 */
public class ShopException extends Exception {

    public ShopException(String message) { super(message); }

    public ShopException(String message, Throwable cause) { super(message, cause); }

    public static ShopException from(SQLException e) {
        e.printStackTrace(); // full details go to the console / log, not to the user
        String state = e.getSQLState() == null ? "" : e.getSQLState();
        int code = e.getErrorCode();

        if (state.startsWith("08") || e instanceof SQLNonTransientConnectionException)
            return new ShopException("Cannot reach the database. Is MySQL running?", e);
        return switch (code) {
            case 1045 -> new ShopException("Database login failed. Check the user name / password in DBConnection.java.", e);
            case 1049 -> new ShopException("Database 'retail_db' does not exist. Run schema.sql first.", e);
            case 1146 -> new ShopException("Database tables are missing. Run schema.sql first.", e);
            case 1205, 1213 -> new ShopException("The database is busy. Please try again.", e);
            case 1451 -> new ShopException("This record is still in use and cannot be deleted.", e);
            case 3819, 4025 -> new ShopException("A value is outside the allowed range.", e);
            default -> new ShopException("Database error: " + e.getMessage(), e);
        };
    }
}
