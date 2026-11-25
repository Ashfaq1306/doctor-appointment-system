// Thrown when no doctor is found with the given id.
public class DoctorNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public DoctorNotFoundException(String message) {
        super(message);
    }
}
