// Thrown when email or password is wrong at login.
public class InvalidLoginException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidLoginException(String message) {
        super(message);
    }
}
