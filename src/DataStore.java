import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;

// Keeps all lists in memory, checks the rules and saves after every change.
public class DataStore {

    private static final String PATIENT_FILE = "patients.dat";
    private static final String DOCTOR_FILE = "doctors.dat";
    private static final String APPOINTMENT_FILE = "appointments.dat";

    private FileManager fileManager;
    private ArrayList<Patient> patients;
    private ArrayList<Doctor> doctors;
    private ArrayList<Appointment> appointments;

    public DataStore(String folder) throws IOException {
        fileManager = new FileManager(folder);
        patients = fileManager.loadList(PATIENT_FILE);
        doctors = fileManager.loadList(DOCTOR_FILE);
        appointments = fileManager.loadList(APPOINTMENT_FILE);
    }

    private void savePatients() throws IOException {
        fileManager.saveList(PATIENT_FILE, patients);
    }

    private void saveDoctors() throws IOException {
        fileManager.saveList(DOCTOR_FILE, doctors);
    }

    private void saveAppointments() throws IOException {
        fileManager.saveList(APPOINTMENT_FILE, appointments);
    }

    public ArrayList<Patient> getPatients() {
        return patients;
    }

    public ArrayList<Doctor> getDoctors() {
        return doctors;
    }

    public ArrayList<Appointment> getAppointments() {
        return appointments;
    }

    // Adds some doctors so that the program is not empty in the first run.
    public void addSampleDoctors() throws IOException, InvalidInputException {
        if (!doctors.isEmpty() || !patients.isEmpty() || !appointments.isEmpty()) {
            return;
        }
        Doctor d1 = addDoctor("Dr. Rahim Uddin", "01711111111", "rahim@hospital.com", "doctor123",
                "Cardiologist", "Cardiology", 1000, 12);
        Doctor d2 = addDoctor("Dr. Nusrat Jahan", "01722222222", "nusrat@hospital.com", "doctor123",
                "Dermatologist", "Skin", 800, 7);
        Doctor d3 = addDoctor("Dr. Kamal Hossain", "01733333333", "kamal@hospital.com", "doctor123",
                "Medicine Specialist", "Medicine", 600, 9);
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day == DayOfWeek.FRIDAY) {
                continue; // weekend
            }
            for (int hour = 9; hour < 12; hour++) {
                d1.addSlot(new TimeSlot(day, LocalTime.of(hour, 0)));
                d2.addSlot(new TimeSlot(day, LocalTime.of(hour, 30)));
                d3.addSlot(new TimeSlot(day, LocalTime.of(hour + 6, 0)));
            }
        }
        saveDoctors();
    }

    // ---------- id ----------

    // "P1005" becomes 1005
    private int idNumber(String id) {
        return Integer.parseInt(id.substring(1));
    }

    // Old appointments are also checked, so the id of a removed person is not given to
    // a new person who would then get the old person's history.
    private String nextPatientId() {
        int max = 1000;
        for (Patient p : patients) {
            max = Math.max(max, idNumber(p.getId()));
        }
        for (Appointment a : appointments) {
            max = Math.max(max, idNumber(a.getPatientId()));
        }
        return "P" + (max + 1);
    }

    private String nextDoctorId() {
        int max = 1000;
        for (Doctor d : doctors) {
            max = Math.max(max, idNumber(d.getId()));
        }
        for (Appointment a : appointments) {
            max = Math.max(max, idNumber(a.getDoctorId()));
        }
        return "D" + (max + 1);
    }

    private String nextAppointmentId() {
        int max = 1000;
        for (Appointment a : appointments) {
            max = Math.max(max, idNumber(a.getId()));
        }
        return "A" + (max + 1);
    }

    // Two users cannot have the same email. ownId is the id of the user who is updating
    // the profile, it is null for a new user.
    private void checkEmailIsFree(String email, String ownId) throws InvalidInputException {
        ArrayList<User> users = new ArrayList<>();
        users.add(new Admin());
        users.addAll(patients);
        users.addAll(doctors);
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email.trim()) && !u.getId().equals(ownId)) {
                throw new InvalidInputException("This email is already used.");
            }
        }
    }

    // ---------- patient ----------

    public Patient addPatient(String name, int age, String phone, String email, String password)
            throws InvalidInputException, IOException {
        Patient patient = new Patient(nextPatientId(), name, age, phone, email, password);
        checkEmailIsFree(email, null);
        patients.add(patient);
        savePatients();
        return patient;
    }

    public void updatePatient(Patient patient, String name, int age, String phone, String email,
                              String password) throws InvalidInputException, IOException {
        checkEmailIsFree(email, patient.getId());
        patient.updateProfile(name, age, phone, email, password);
        savePatients();
    }

    public void removePatient(Patient patient) throws IOException {
        for (Appointment a : appointments) {
            if (a.getPatientId().equals(patient.getId()) && a.isActive()) {
                a.setStatus(AppointmentStatus.CANCELLED);
            }
        }
        saveAppointments();
        patients.remove(patient);
        savePatients();
    }

    // returns null if not found
    public Patient findPatient(String id) {
        for (Patient p : patients) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    public ArrayList<Patient> searchPatients(String keyword) {
        ArrayList<Patient> result = new ArrayList<>();
        for (Patient p : patients) {
            if (p.matches(keyword)) {
                result.add(p);
            }
        }
        return result;
    }

    public String getPatientName(String id) {
        Patient p = findPatient(id);
        return p == null ? "Removed (" + id + ")" : p.getName();
    }

    // ---------- doctor ----------

    public Doctor addDoctor(String name, String phone, String email, String password,
                            String specialization, String department, double fee, int experience)
            throws InvalidInputException, IOException {
        Doctor doctor = new Doctor(nextDoctorId(), name, phone, email, password,
                specialization, department, fee, experience);
        checkEmailIsFree(email, null);
        doctors.add(doctor);
        saveDoctors();
        return doctor;
    }

    public void updateDoctor(Doctor doctor, String name, String phone, String email, String password,
                             String specialization, String department, double fee, int experience)
            throws InvalidInputException, IOException {
        checkEmailIsFree(email, doctor.getId());
        doctor.updateProfile(name, phone, email, password, specialization, department, fee, experience);
        saveDoctors();
    }

    public void removeDoctor(Doctor doctor) throws IOException {
        for (Appointment a : appointments) {
            if (a.getDoctorId().equals(doctor.getId()) && a.isActive()) {
                a.setStatus(AppointmentStatus.CANCELLED);
            }
        }
        saveAppointments();
        doctors.remove(doctor);
        saveDoctors();
    }

    public Doctor findDoctor(String id) throws DoctorNotFoundException {
        for (Doctor d : doctors) {
            if (d.getId().equals(id)) {
                return d;
            }
        }
        throw new DoctorNotFoundException("No doctor found with id " + id + ".");
    }

    public String getDoctorName(String id) {
        try {
            return findDoctor(id).getName();
        } catch (DoctorNotFoundException e) {
            return "Removed (" + id + ")";
        }
    }

    // search in everything
    public ArrayList<Doctor> searchDoctors(String keyword) {
        ArrayList<Doctor> result = new ArrayList<>();
        for (Doctor d : doctors) {
            if (d.matches(keyword)) {
                result.add(d);
            }
        }
        return result;
    }

    // method overloading: search in one field only
    public ArrayList<Doctor> searchDoctors(String keyword, String field) {
        if (field.equals("All")) {
            return searchDoctors(keyword);
        }
        String key = keyword.trim().toLowerCase();
        ArrayList<Doctor> result = new ArrayList<>();
        for (Doctor d : doctors) {
            String value;
            if (field.equals("Name")) {
                value = d.getName();
            } else if (field.equals("Specialization")) {
                value = d.getSpecialization();
            } else {
                value = d.getDepartment();
            }
            if (value.toLowerCase().contains(key)) {
                result.add(d);
            }
        }
        return result;
    }

    // adds all 30 minute slots between from and to, returns how many were added
    public int addSlots(Doctor doctor, DayOfWeek day, LocalTime from, LocalTime to)
            throws InvalidInputException, IOException {
        if (!from.isBefore(to)) {
            throw new InvalidInputException("Start time must be before end time.");
        }
        int count = 0;
        LocalTime time = from;
        while (time.isBefore(to)) {
            if (doctor.addSlot(new TimeSlot(day, time))) {
                count++;
            }
            time = time.plusMinutes(TimeSlot.MINUTES);
        }
        saveDoctors();
        return count;
    }

    public void removeSlot(Doctor doctor, TimeSlot slot) throws IOException {
        doctor.removeSlot(slot);
        saveDoctors();
    }

    // average rating as text, "-" if nobody rated yet
    public String getRatingText(String doctorId) {
        int total = 0;
        int count = 0;
        for (Appointment a : appointments) {
            if (a.getDoctorId().equals(doctorId) && a.getRating() > 0) {
                total += a.getRating();
                count++;
            }
        }
        if (count == 0) {
            return "-";
        }
        return String.format("%.1f (%d)", (double) total / count, count);
    }

    // ---------- appointment ----------

    // slots of that date which are not booked and not in the past
    public ArrayList<TimeSlot> getFreeSlots(Doctor doctor, LocalDate date) {
        ArrayList<TimeSlot> result = new ArrayList<>();
        for (TimeSlot slot : doctor.getSlots(date.getDayOfWeek())) {
            boolean past = !LocalDateTime.of(date, slot.getStartTime()).isAfter(LocalDateTime.now());
            if (!past && !isDoctorBooked(doctor.getId(), date, slot)) {
                result.add(slot);
            }
        }
        return result;
    }

    private boolean isDoctorBooked(String doctorId, LocalDate date, TimeSlot slot) {
        for (Appointment a : appointments) {
            if (a.isSlotTaken() && a.getDoctorId().equals(doctorId)
                    && a.getDate().equals(date) && a.getSlot().equals(slot)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPatientBusy(String patientId, LocalDate date, TimeSlot slot) {
        for (Appointment a : appointments) {
            if (a.isSlotTaken() && a.getPatientId().equals(patientId)
                    && a.getDate().equals(date) && a.getSlot().equals(slot)) {
                return true;
            }
        }
        return false;
    }

    private void checkConflict(String patientId, String doctorId, LocalDate date, TimeSlot slot)
            throws AppointmentConflictException {
        if (isDoctorBooked(doctorId, date, slot)) {
            throw new AppointmentConflictException("This time slot is already booked.");
        }
        if (isPatientBusy(patientId, date, slot)) {
            throw new AppointmentConflictException("The patient has another appointment at this time.");
        }
    }

    public Appointment bookAppointment(Patient patient, String doctorId, LocalDate date,
                                       TimeSlot slot, String reason)
            throws DoctorNotFoundException, InvalidInputException, AppointmentConflictException, IOException {
        Doctor doctor = findDoctor(doctorId);
        if (slot == null) {
            throw new InvalidInputException("Please select a time slot.");
        }
        if (slot.getDay() != date.getDayOfWeek() || !doctor.hasSlot(slot)) {
            throw new InvalidInputException("The doctor is not available at this time.");
        }
        Appointment appointment = new Appointment(nextAppointmentId(), patient.getId(), doctorId,
                date, slot, reason);
        if (appointment.isPast()) {
            throw new InvalidInputException("You cannot book an appointment in the past.");
        }
        checkConflict(patient.getId(), doctorId, date, slot);

        appointments.add(appointment);
        saveAppointments();
        return appointment;
    }

    // returns null if not found
    public Appointment findAppointment(String id) {
        for (Appointment a : appointments) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public void approve(Appointment a) throws InvalidInputException, IOException {
        if (a.getStatus() != AppointmentStatus.PENDING) {
            throw new InvalidInputException("Only pending appointments can be approved.");
        }
        if (a.isPast()) {
            throw new InvalidInputException("The time of this appointment has already passed.");
        }
        a.setStatus(AppointmentStatus.APPROVED);
        saveAppointments();
    }

    public void reject(Appointment a) throws InvalidInputException, IOException {
        if (a.getStatus() != AppointmentStatus.PENDING) {
            throw new InvalidInputException("Only pending appointments can be rejected.");
        }
        a.setStatus(AppointmentStatus.REJECTED);
        saveAppointments();
    }

    public void cancel(Appointment a) throws InvalidInputException, IOException {
        if (!a.isActive()) {
            throw new InvalidInputException("Only pending or approved appointments can be cancelled.");
        }
        a.setStatus(AppointmentStatus.CANCELLED);
        saveAppointments();
    }

    public void complete(Appointment a) throws InvalidInputException, IOException {
        if (a.getStatus() != AppointmentStatus.APPROVED) {
            throw new InvalidInputException("Only approved appointments can be completed.");
        }
        if (!a.isPast()) {
            throw new InvalidInputException("This appointment has not started yet.");
        }
        a.setStatus(AppointmentStatus.COMPLETED);
        saveAppointments();
    }

    // admin can change the status, but the slot must not be double booked
    public void overrideStatus(Appointment a, AppointmentStatus newStatus)
            throws InvalidInputException, AppointmentConflictException, DoctorNotFoundException, IOException {
        if (newStatus == AppointmentStatus.COMPLETED && !a.isPast()) {
            throw new InvalidInputException("This appointment has not started yet.");
        }
        boolean willTakeSlot = newStatus != AppointmentStatus.REJECTED
                && newStatus != AppointmentStatus.CANCELLED;
        if (willTakeSlot && !a.isSlotTaken()) {
            findDoctor(a.getDoctorId()); // throws exception if the doctor was removed
            if (findPatient(a.getPatientId()) == null) {
                throw new InvalidInputException("This patient was removed.");
            }
            checkConflict(a.getPatientId(), a.getDoctorId(), a.getDate(), a.getSlot());
        }
        a.setStatus(newStatus);
        saveAppointments();
    }

    public void rate(Appointment a, int rating) throws InvalidInputException, IOException {
        if (a.getStatus() != AppointmentStatus.COMPLETED) {
            throw new InvalidInputException("You can rate only completed appointments.");
        }
        if (a.getRating() > 0) {
            throw new InvalidInputException("You have already rated this appointment.");
        }
        a.setRating(rating);
        saveAppointments();
    }

    // Any parameter can be null, null means "do not filter by this".
    public ArrayList<Appointment> searchAppointments(String patientId, String doctorId,
                                                     AppointmentStatus status, LocalDate date) {
        ArrayList<Appointment> result = new ArrayList<>();
        for (Appointment a : appointments) {
            if (patientId != null && !a.getPatientId().equals(patientId)) {
                continue;
            }
            if (doctorId != null && !a.getDoctorId().equals(doctorId)) {
                continue;
            }
            if (status != null && a.getStatus() != status) {
                continue;
            }
            if (date != null && !a.getDate().equals(date)) {
                continue;
            }
            result.add(a);
        }
        result.sort(Comparator.comparing(Appointment::getDateTime));
        return result;
    }

    // ---------- report ----------

    public String makeReport(ArrayList<Appointment> list) {
        String line = "------------------------------------------------------------\n";
        StringBuilder text = new StringBuilder();
        text.append("APPOINTMENT REPORT\n");
        text.append("Date: ").append(LocalDate.now()).append("\n");
        text.append("Total: ").append(list.size()).append("\n");
        text.append(line);
        for (Appointment a : list) {
            text.append("Id      : ").append(a.getId()).append("\n");
            text.append("Patient : ").append(getPatientName(a.getPatientId())).append("\n");
            text.append("Doctor  : ").append(getDoctorName(a.getDoctorId())).append("\n");
            text.append("Time    : ").append(a.getDate()).append(" ").append(a.getSlot()).append("\n");
            text.append("Reason  : ").append(a.getReason()).append("\n");
            text.append("Status  : ").append(a.getStatus()).append("\n");
            text.append(line);
        }
        return text.toString();
    }
}
