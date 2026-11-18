package retail;

import java.math.BigDecimal;

public record Product(int id, String name, BigDecimal price, int stockQty) {}
