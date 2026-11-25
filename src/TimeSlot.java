import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;

// One 30 minute slot of a week day. Example: MONDAY 09:00 - 09:30
public class TimeSlot implements Serializable, Comparable<TimeSlot> {

    private static final long serialVersionUID = 1L;

    public static final int MINUTES = 30;

    private DayOfWeek day;
    private LocalTime startTime;

    public TimeSlot(DayOfWeek day, LocalTime startTime) {
        this.day = day;
        this.startTime = startTime;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return startTime.plusMinutes(MINUTES);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TimeSlot)) {
            return false;
        }
        TimeSlot other = (TimeSlot) obj;
        return day == other.day && startTime.equals(other.startTime);
    }

    @Override
    public int hashCode() {
        return day.hashCode() + startTime.hashCode();
    }

    @Override
    public int compareTo(TimeSlot other) {
        if (day != other.day) {
            return day.compareTo(other.day);
        }
        return startTime.compareTo(other.startTime);
    }

    @Override
    public String toString() {
        return startTime + " - " + getEndTime();
    }
}
