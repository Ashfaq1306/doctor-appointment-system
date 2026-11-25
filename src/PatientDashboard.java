import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class PatientDashboard extends DashboardFrame {

    private static final long serialVersionUID = 1L;

    private Patient patient;

    // book tab
    private JComboBox<String> searchBox = new JComboBox<>(
            new String[] {"All", "Name", "Specialization", "Department"});
    private JTextField searchField = new JTextField(20);
    private JTable doctorTable = GuiHelper.makeTable("Id", "Name", "Specialization", "Department",
            "Experience (years)", "Fee (Tk)", "Rating");
    private JTextField dateField = new JTextField(LocalDate.now().plusDays(1).toString(), 8);
    private JComboBox<TimeSlot> slotBox = new JComboBox<>();
    private JTextField reasonField = new JTextField(18);

    // history tab
    private JTable appointmentTable = GuiHelper.makeTable("Id", "Doctor", "Date", "Time", "Reason",
            "Status", "My Rating");

    // profile tab
    private JTextField nameField = new JTextField();
    private JTextField ageField = new JTextField();
    private JTextField phoneField = new JTextField();
    private JTextField emailField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();

    public PatientDashboard(Patient patient, DataStore store) {
        super(patient, store);
        this.patient = patient;

        tabs.addTab("Find Doctor & Book", createBookTab());
        tabs.addTab("My Appointments", createHistoryTab());
        tabs.addTab("My Profile", createProfileTab());

        fillProfile();
        refreshAll();
    }

    @Override
    protected void refreshAll() {
        searchDoctors();
        loadAppointments();
        showReminder();
    }

    private void showReminder() {
        int count = 0;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        for (Appointment a : store.searchAppointments(patient.getId(), null,
                AppointmentStatus.APPROVED, null)) {
            if (!a.isPast() && !a.getDate().isAfter(tomorrow)) {
                count++;
            }
        }
        if (count > 0) {
            setReminder("Reminder: you have " + count + " approved appointment(s) today or tomorrow.");
        } else {
            setReminder("You have no approved appointment today or tomorrow.");
        }
    }

    // ---------- book tab ----------

    private JPanel createBookTab() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");
        JButton scheduleButton = new JButton("View Weekly Schedule");
        searchPanel.add(new JLabel("Search by:"));
        searchPanel.add(searchBox);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);
        searchPanel.add(scheduleButton);

        JPanel bookPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bookPanel.setBorder(BorderFactory.createTitledBorder("Book an appointment (select a doctor first)"));
        JButton slotButton = new JButton("Show Free Slots");
        JButton bookButton = new JButton("Book");
        bookPanel.add(new JLabel("Date:"));
        bookPanel.add(dateField);
        bookPanel.add(slotButton);
        bookPanel.add(new JLabel("Time:"));
        bookPanel.add(slotBox);
        bookPanel.add(new JLabel("Reason:"));
        bookPanel.add(reasonField);
        bookPanel.add(bookButton);
        slotBox.setPrototypeDisplayValue(new TimeSlot(DayOfWeek.MONDAY, java.time.LocalTime.of(10, 0)));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(doctorTable), BorderLayout.CENTER);
        panel.add(bookPanel, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> searchDoctors());
        searchField.addActionListener(e -> searchDoctors());
        showAllButton.addActionListener(e -> {
            searchBox.setSelectedItem("All");
            searchField.setText("");
            searchDoctors();
        });
        scheduleButton.addActionListener(e -> showWeeklySchedule());
        slotButton.addActionListener(e -> showFreeSlots());
        bookButton.addActionListener(e -> book());
        // old slots are not valid when another doctor is selected
        doctorTable.getSelectionModel().addListSelectionListener(e -> slotBox.removeAllItems());
        return panel;
    }

    private void searchDoctors() {
        String field = searchBox.getSelectedItem().toString();
        GuiHelper.fillDoctorTable(doctorTable, store.searchDoctors(searchField.getText(), field), store);
    }

    private void showWeeklySchedule() {
        try {
            Doctor doctor = store.findDoctor(GuiHelper.getSelectedId(doctorTable));
            String text = doctor.displayInfo() + "\n\n";
            for (DayOfWeek day : DayOfWeek.values()) {
                ArrayList<TimeSlot> slots = doctor.getSlots(day);
                text += day + ": ";
                if (slots.isEmpty()) {
                    text += "not available";
                }
                for (int i = 0; i < slots.size(); i++) {
                    if (i > 0 && i % 5 == 0) {
                        text += "\n        ";
                    }
                    text += slots.get(i) + "   ";
                }
                text += "\n";
            }
            GuiHelper.showInfo(this, text);
        } catch (InvalidInputException | DoctorNotFoundException e) {
            GuiHelper.showError(this, e.getMessage());
        }
    }

    private void showFreeSlots() {
        slotBox.removeAllItems();
        try {
            Doctor doctor = store.findDoctor(GuiHelper.getSelectedId(doctorTable));
            LocalDate date = Validator.parseDate(dateField.getText());
            if (date.isBefore(LocalDate.now())) {
                throw new InvalidInputException("You cannot book an appointment in the past.");
            }
            for (TimeSlot slot : store.getFreeSlots(doctor, date)) {
                slotBox.addItem(slot);
            }
            if (slotBox.getItemCount() == 0) {
                GuiHelper.showInfo(this, "No free slot on " + date + " (" + date.getDayOfWeek() + ").");
            }
        } catch (InvalidInputException | DoctorNotFoundException e) {
            GuiHelper.showError(this, e.getMessage());
        }
    }

    private void book() {
        try {
            Doctor doctor = store.findDoctor(GuiHelper.getSelectedId(doctorTable));
            LocalDate date = Validator.parseDate(dateField.getText());
            TimeSlot slot = (TimeSlot) slotBox.getSelectedItem();
            if (slot == null) {
                throw new InvalidInputException("Press \"Show Free Slots\" and select a time.");
            }
            String question = "Book " + doctor.getName() + " on " + date + " at " + slot
                    + "?\nFee: " + doctor.getFee() + " Tk";
            if (!GuiHelper.confirm(this, question)) {
                return;
            }
            Appointment a = store.bookAppointment(patient, doctor.getId(), date, slot, reasonField.getText());
            reasonField.setText("");
            refreshAll();
            GuiHelper.showInfo(this, "Appointment " + a.getId() + " is booked. Please wait for approval.");
        } catch (InvalidInputException | DoctorNotFoundException | AppointmentConflictException e) {
            GuiHelper.showError(this, e.getMessage());
        } catch (IOException e) {
            GuiHelper.showError(this, "Could not save data.\n" + e.getMessage());
        }
    }

    // ---------- history tab ----------

    private JPanel createHistoryTab() {
        JButton cancelButton = new JButton("Cancel Appointment");
        JButton rateButton = new JButton("Rate Doctor");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(cancelButton);
        buttons.add(rateButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(appointmentTable), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> cancelAppointment());
        rateButton.addActionListener(e -> rateDoctor());
        return panel;
    }

    private void loadAppointments() {
        GuiHelper.clearTable(appointmentTable);
        for (Appointment a : store.searchAppointments(patient.getId(), null, null, null)) {
            String rating = a.getRating() == 0 ? "-" : String.valueOf(a.getRating());
            GuiHelper.addRow(appointmentTable, a.getId(), store.getDoctorName(a.getDoctorId()),
                    a.getDate(), a.getSlot().toString(), a.getReason(), a.getStatus(), rating);
        }
    }

    private void cancelAppointment() {
        try {
            Appointment a = store.findAppointment(GuiHelper.getSelectedId(appointmentTable));
            if (!GuiHelper.confirm(this, "Cancel appointment " + a.getId() + "?")) {
                return;
            }
            store.cancel(a);
            refreshAll();
        } catch (InvalidInputException e) {
            GuiHelper.showError(this, e.getMessage());
        } catch (IOException e) {
            GuiHelper.showError(this, "Could not save data.\n" + e.getMessage());
        }
    }

    private void rateDoctor() {
        try {
            Appointment a = store.findAppointment(GuiHelper.getSelectedId(appointmentTable));
            Integer[] options = {5, 4, 3, 2, 1};
            Object choice = JOptionPane.showInputDialog(this, "Give a rating (5 is best):", "Rate Doctor",
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (choice == null) {
                return;
            }
            store.rate(a, (Integer) choice);
            refreshAll();
        } catch (InvalidInputException e) {
            GuiHelper.showError(this, e.getMessage());
        } catch (IOException e) {
            GuiHelper.showError(this, "Could not save data.\n" + e.getMessage());
        }
    }

    // ---------- profile tab ----------

    private JPanel createProfileTab() {
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        GuiHelper.addField(form, "Full name:", nameField);
        GuiHelper.addField(form, "Age:", ageField);
        GuiHelper.addField(form, "Phone:", phoneField);
        GuiHelper.addField(form, "Email:", emailField);
        GuiHelper.addField(form, "New password (empty = no change):", passwordField);
        JButton saveButton = new JButton("Save Changes");
        form.add(new JLabel());
        form.add(saveButton);
        saveButton.addActionListener(e -> saveProfile());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));
        panel.add(form, BorderLayout.NORTH);
        return panel;
    }

    private void fillProfile() {
        nameField.setText(patient.getName());
        ageField.setText(String.valueOf(patient.getAge()));
        phoneField.setText(patient.getPhone());
        emailField.setText(patient.getEmail());
        passwordField.setText("");
    }

    private void saveProfile() {
        try {
            int age = Validator.parseInt(ageField.getText(), "Age");
            String password = new String(passwordField.getPassword());
            if (password.isEmpty()) {
                password = patient.getPassword();
            }
            store.updatePatient(patient, nameField.getText(), age, phoneField.getText(),
                    emailField.getText(), password);
            fillProfile();
            showUserInfo();
            GuiHelper.showInfo(this, "Profile updated.");
        } catch (InvalidInputException e) {
            GuiHelper.showError(this, e.getMessage());
        } catch (IOException e) {
            GuiHelper.showError(this, "Could not save data.\n" + e.getMessage());
        }
    }
}
