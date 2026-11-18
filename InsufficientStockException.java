package retail;

/** Tried to sell more than what is in stock. */
public class InsufficientStockException extends ShopException {
    public InsufficientStockException(String message) { super(message); }
}
