package retail;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** All input checking lives here, so the GUI can pass raw text straight through. */
public final class Validator {
    private Validator() {}

    public static String name(String s) throws ValidationException {
        if (s == null || s.isBlank()) throw new ValidationException("Product name is required.");
        String n = s.trim();
        if (n.length() > 100) throw new ValidationException("Product name is too long (max 100 characters).");
        return n;
    }

    public static BigDecimal price(String s) throws ValidationException {
        if (s == null || s.isBlank()) throw new ValidationException("Price is required.");
        BigDecimal p;
        try {
            p = new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Price must be a number, for example 49.50.");
        }
        if (p.signum() < 0) throw new ValidationException("Price cannot be negative.");
        p = p.setScale(2, RoundingMode.HALF_UP);
        if (p.compareTo(new BigDecimal("99999999.99")) > 0)
            throw new ValidationException("Price is too large (maximum 99999999.99).");
        return p;
    }

    public static int stock(String s) throws ValidationException {
        if (s == null || s.isBlank()) throw new ValidationException("Stock is required.");
        int q;
        try {
            q = Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Stock must be a whole number, for example 25.");
        }
        if (q < 0) throw new ValidationException("Stock cannot be negative.");
        if (q > 10_000_000) throw new ValidationException("Stock quantity is too large.");
        return q;
    }
}
