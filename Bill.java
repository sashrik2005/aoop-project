package retail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** The finished bill, built from the prices the database actually charged. */
public record Bill(int saleId, LocalDateTime date, BigDecimal total, List<Cart.Line> lines) {

    public String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("RETAIL SHOP\n");
        sb.append("Bill #").append(saleId).append("    ")
          .append(date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))).append('\n');
        sb.append("=".repeat(47)).append('\n');
        sb.append(String.format("%-20s %5s %9s %10s\n", "Item", "Qty", "Price", "Subtotal"));
        sb.append("-".repeat(47)).append('\n');
        for (Cart.Line l : lines)
            sb.append(String.format("%-20.20s %5d %9s %10s\n", l.name(), l.qty(), l.price(), l.subtotal()));
        sb.append("-".repeat(47)).append('\n');
        sb.append(String.format("%-20s %26s\n", "TOTAL", total.setScale(2, RoundingMode.HALF_UP)));
        sb.append("\nThank you!\n");
        return sb.toString();
    }
}
