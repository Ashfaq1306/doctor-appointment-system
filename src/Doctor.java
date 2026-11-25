import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;

public class Doctor extends User {

    private static final long serialVersionUID = 1L;

    private String specialization;
    private String department;
    private double fee;
    private int experience;
    private ArrayList<TimeSlot> slots = new ArrayList<>();

    public Doctor(String id, String name, String phone, String email, String password,
                  String specialization, String department, double fee, int experience)
            throws InvalidInputException {
        super(id, name, phone, email, password);
        setSpecialization(specialization);
        setDepartment(department);
        setFee(fee);
        setExperience(experience);
    }

    public void updateProfile(String name, String phone, String email, String password,
                              String specialization, String department, double fee, int experience)
            throws InvalidInputException {
        // check all values first using a temporary object
        new Doctor(getId(), name, phone, email, password, specialization, department, fee, experience);

        setName(name);
        setPhone(phone);
        setEmail(email);
        setPassword(password);
        setSpecialization(specialization);
        setDepartment(department);
        setFee(fee);
        setExperience(experience);
    }

    @Override
    public String getRole() {
        return "Doctor";
    }

    @Override
    public String displayInfo() {
        return super.displayInfo() + ", " + specialization;
    }

    @Override
    public void showDashboard(DataStore store) {
        new DoctorDashboard(this, store).setVisible(true);
    }

    @Override
    public boolean matches(String keyword) {
        String key = keyword.trim().toLowerCase();
        return super.matches(keyword)
                || specialization.toLowerCase().contains(key)
                || department.toLowerCase().contains(key);
    }

    // ---------- time slots ----------

    // returns false if the slot is already in the list
    public boolean addSlot(TimeSlot slot) {
        if (slots.contains(slot)) {
            return false;
        }
        slots.add(slot);
        Collections.sort(slots);
        return true;
    }

    public void removeSlot(TimeSlot slot) {
        slots.remove(slot);
    }

    public boolean hasSlot(TimeSlot slot) {
        return slots.contains(slot);
    }

    public ArrayList<TimeSlot> getSlots() {
        return new ArrayList<>(slots);
    }

    // method overloading: slots of one day only
    public ArrayList<TimeSlot> getSlots(DayOfWeek day) {
        ArrayList<TimeSlot> result = new ArrayList<>();
        for (TimeSlot slot : slots) {
            if (slot.getDay() == day) {
                result.add(slot);
            }
        }
        return result;
    }

    // ---------- getters and setters ----------

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) throws InvalidInputException {
        this.specialization = Validator.checkNotEmpty(specialization, "Specialization");
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) throws InvalidInputException {
        this.department = Validator.checkNotEmpty(department, "Department");
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) throws InvalidInputException {
        if (!(fee >= 0 && fee <= 100000)) {
            throw new InvalidInputException("Fee must be between 0 and 100000.");
        }
        this.fee = fee;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) throws InvalidInputException {
        if (experience < 0 || experience > 60) {
            throw new InvalidInputException("Experience must be between 0 and 60 years.");
        }
        this.experience = experience;
    }
}
