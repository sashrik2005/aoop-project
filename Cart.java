package retail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The shopping cart. All cart rules (quantity > 0, not more than stock) are enforced here. */
public class Cart {

    public record Line(int productId, String name, BigDecimal price, int qty) {
        public BigDecimal subtotal() { return price.multiply(BigDecimal.valueOf(qty)); }
    }

    private final Map<Integer, Line> lines = new LinkedHashMap<>();

    public void add(Product p, int qty) throws ShopException {
        if (p == null) throw new ValidationException("Select a valid product.");
        if (p.id() <= 0 || p.name() == null || p.price() == null || p.stockQty() < 0 || p.price().signum() < 0)
            throw new ValidationException("The selected product contains invalid data. Refresh the product list.");
        if (qty <= 0) throw new ValidationException("Quantity must be at least 1.");
        int already = lines.containsKey(p.id()) ? lines.get(p.id()).qty() : 0;
        if (qty > p.stockQty() - already) {
            if (p.stockQty() == 0)
                throw new InsufficientStockException("'" + p.name() + "' is out of stock.");
            throw new InsufficientStockException("Only " + p.stockQty() + " of '" + p.name() + "' in stock"
                    + (already > 0 ? " (" + already + " already in cart)." : "."));
        }
        lines.put(p.id(), new Line(p.id(), p.name(), p.price(), already + qty));
    }

    public void remove(int productId) { lines.remove(productId); }
    public void clear()               { lines.clear(); }
    public boolean isEmpty()          { return lines.isEmpty(); }
    public List<Line> lines()         { return List.copyOf(lines.values()); }

    public BigDecimal total() {
        BigDecimal t = BigDecimal.ZERO;
        for (Line l : lines.values()) t = t.add(l.subtotal());
        return t.setScale(2, RoundingMode.HALF_UP);
    }
}
