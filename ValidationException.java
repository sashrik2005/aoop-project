package retail;

/** The user typed something invalid (empty name, negative price, ...). */
public class ValidationException extends ShopException {
    public ValidationException(String message) { super(message); }
}
