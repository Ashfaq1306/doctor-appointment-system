import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Appointment implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String patientId;
    private String doctorId;
    private LocalDate date;
    private TimeSlot slot;
    private String reason;
    private AppointmentStatus status;
    private int rating; // 0 means not rated yet

    public Appointment(String id, String patientId, String doctorId, LocalDate date,
                       TimeSlot slot, String reason) throws InvalidInputException {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.slot = slot;
        setReason(reason);
        this.status = AppointmentStatus.PENDING;
    }

    // pending, approved and completed appointments keep the slot booked
    public boolean isSlotTaken() {
        return status == AppointmentStatus.PENDING
                || status == AppointmentStatus.APPROVED
                || status == AppointmentStatus.COMPLETED;
    }

    // only pending and approved appointments can be cancelled
    public boolean isActive() {
        return status == AppointmentStatus.PENDING || status == AppointmentStatus.APPROVED;
    }

    public boolean isPast() {
        return !getDateTime().isAfter(LocalDateTime.now());
    }

    public LocalDateTime getDateTime() {
        return LocalDateTime.of(date, slot.getStartTime());
    }

    public String getId() {
        return id;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public LocalDate getDate() {
        return date;
    }

    public TimeSlot getSlot() {
        return slot;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) throws InvalidInputException {
        this.reason = Validator.checkNotEmpty(reason, "Reason");
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) throws InvalidInputException {
        if (rating < 1 || rating > 5) {
            throw new InvalidInputException("Rating must be between 1 and 5.");
        }
        this.rating = rating;
    }
}
