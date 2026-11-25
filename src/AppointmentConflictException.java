// Thrown when the selected time slot is already booked.
public class AppointmentConflictException extends Exception {

    private static final long serialVersionUID = 1L;

    public AppointmentConflictException(String message) {
        super(message);
    }
}
