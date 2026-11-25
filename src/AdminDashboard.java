import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class AdminDashboard extends DashboardFrame {

    private static final long serialVersionUID = 1L;

    // summary tab
    private JLabel doctorCount = new JLabel();
    private JLabel patientCount = new JLabel();
    private JLabel appointmentCount = new JLabel();
    private JLabel todayCount = new JLabel();
    private JLabel[] statusCounts = new JLabel[AppointmentStatus.values().length];

    // doctors tab
    private JTextField doctorSearchField = new JTextField(20);
    private JTable doctorTable = GuiHelper.makeTable("Id", "Name", "Specialization", "Department",
            "Experience (years)", "Fee (Tk)", "Rating");

    // patients tab
    private JTextField patientSearchField = new JTextField(20);
    private JTable patientTable = GuiHelper.makeTable("Id", "Name", "Age", "Phone", "Email");

    // appointments tab
    private JComboBox<String> statusBox = new JComboBox<>();
    private JComboBox<String> doctorBox = new JComboBox<>();
    private JTextField dateField = new JTextField(8);
    private JTable appointmentTable = GuiHelper.makeTable("Id", "Patient", "Doctor", "Date", "Time",
            "Reason", "Status");
    private ArrayList<Appointment> shownAppointments = new ArrayList<>();

    public AdminDashboard(Admin admin, DataStore store) {
        super(admin, store);

        tabs.addTab("Summary", createSummaryTab());
        tabs.addTab("Doctors", createDoctorTab());
        tabs.addTab("Patients", createPatientTab());
        tabs.addTab("Appointments", createAppointmentTab());

        refreshAll();
    }

    @Override
    protected void refreshAll() {
        loadSummary();
        loadDoctors();
        loadPatients();
        loadDoctorBox();
        loadAppointments();
    }

    // ---------- summary tab ----------

    private JPanel createSummaryTab() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 10, 12));
        GuiHelper.addField(grid, "Total doctors:", doctorCount);
        GuiHelper.addField(grid, "Total patients:", patientCount);
        GuiHelper.addField(grid, "Total appointments:", appointmentCount);
        GuiHelper.addField(grid, "Today's appointments:", todayCount);
        AppointmentStatus[] all = AppointmentStatus.values();
        for (int i = 0; i < all.length; i++) {
            statusCounts[i] = new JLabel();
            GuiHelper.addField(grid, all[i] + ":", statusCounts[i]);
        }
        Font font = new Font("Arial", Font.PLAIN, 16);
        for (java.awt.Component c : grid.getComponents()) {
            c.setFont(font);
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));
        panel.add(grid, BorderLayout.NORTH);
        return panel;
    }

    private void loadSummary() {
        doctorCount.setText(String.valueOf(store.getDoctors().size()));
        patientCount.setText(String.valueOf(store.getPatients().size()));
        appointmentCount.setText(String.valueOf(store.getAppointments().size()));
        todayCount.setText(String.valueOf(store.searchAppointments(null, null, null, LocalDate.now()).size()));
        AppointmentStatus[] all = AppointmentStatus.values();
        for (int i = 0; i < all.length; i++) {
            statusCounts[i].setText(String.valueOf(store.searchAppointments(null, null, all[i], null).size()));
        }
    }

    // ---------- doctors tab ----------

    private JPanel createDoctorTab() {
        JButton searchButton = new JButton("Search");
        JButton addButton = new JButton("Add Doctor");
        JButton editButton = new JButton("Edit Doctor");
        JButton removeButton = new JButton("Remove Doctor");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search:"));
        top.add(doctorSearchField);
        top.add(searchButton);
        top.add(addButton);
        top.add(editButton);
        top.add(removeButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(doctorTable), BorderLayout.CENTER);

        searchButton.addActionListener(e -> loadDoctors());
        doctorSearchField.addActionListener(e -> loadDoctors());
        addButton.addActionListener(e -> showDoctorForm(null));
        editButton.addActionListener(e -> editDoctor());
        removeButton.addActionListener(e -> removeDoctor());
        return panel;
    }

    private void loadDoctors() {
        GuiHelper.fillDoctorTable(doctorTable, store.searchDoctors(doctorSearchField.getText()), store);
    }

    // doctor is null when adding a new one
    private void showDoctorForm(Doctor doctor) {
        DoctorFormPanel form = new DoctorFormPanel(doctor == null);
        String title = "Add Doctor";
        if (doctor != null) {
            title = "Edit Doctor " + doctor.getId();
            form.fill(doctor);
        }
        // show the form again until the input is correct or the admin presses cancel
        while (true) {
            int answer = JOptionPane.showConfirmDialog(this, form, title,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                form.save(doctor, store);
                refreshAll();
                GuiHelper.showInfo(this, "Doctor saved.");
                return;
            } catch (InvalidInputException e) {
                GuiHelper.showError(this, e.getMessage());
            } catch (IOException e) {
                GuiHelper.showError(this, "Could not save data.\n" + e.getMessage());
                return;
            }
        }
    }

    private void editDoctor() {
        try {
            showDoctorForm(store.findDoctor(GuiHelper.getSelectedId(doctorTable)));
        } catch (InvalidInputException | DoctorNotFoundException e) {
            GuiHelper.showError(this, e.getMessage());
        }
    }

    private void removeDoctor() {
        try {
            Doctor d = store.findDoctor(GuiHelper.getSelectedId(doctorTable));
            if (GuiHelper.confirm(this, "Remove " + d.getName()
                    + "?\nPending and approved appointments of this doctor will be cancelled.")) {
                store.removeDoctor(d);
                refreshAll();
            }
        } catch (InvalidInputException | DoctorNotFoundException e) {
            GuiHelper.showError(this, e.getMessage());
        } catch (IOException e) {
            GuiHelper.showError(this, "Could not save data.\n" + e.getMessage());
        }
    }

    // ---------- patients tab ----------

    private JPanel createPatientTab() {
        JButton searchButton = new JButton("Search");
        JButton removeButton = new JButton("Remove Patient");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search:"));
        top.add(patientSearchField);
        top.add(searchButton);
        top.add(removeButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(patientTable), BorderLayout.CENTER);

        searchButton.addActionListener(e -> loadPatients());
        patientSearchField.addActionListener(e -> loadPatients());
        removeButton.addActionListener(e -> removePatient());
        return panel;
    }

    private void loadPatients() {
        GuiHelper.clearTable(patientTable);
        for (Patient p : store.searchPatients(patientSearchField.getText())) {
            GuiHelper.addRow(patientTable, p.getId(), p.getName(), p.getAge(), p.getPhone(), p.getEmail());
        }
    }

    private void removePatient() {
        try {
            Patient p = store.findPatient(GuiHelper.getSelectedId(patientTable));
            if (GuiHelper.confirm(this, "Remove " + p.getName()
                    + "?\nPending and approved appointments of this patient will be cancelled.")) {
                store.removePatient(p);
                refreshAll();
            }
        } catch (InvalidInputException e) {
            GuiHelper.showError(this, e.getMessage());
        } catch (IOException e) {
            GuiHelper.showError(this, "Could not save data.\n" + e.getMessage());
        }
    }

    // ---------- appointments tab ----------

    private JPanel createAppointmentTab() {
        statusBox.addItem("All status");
        for (AppointmentStatus s : AppointmentStatus.values()) {
            statusBox.addItem(s.toString());
        }
        JButton filterButton = new JButton("Filter");
        JButton clearButton = new JButton("Clear");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Status:"));
        top.add(statusBox);
        top.add(new JLabel("Doctor:"));
        top.add(doctorBox);
        top.add(new JLabel("Date (yyyy-mm-dd):"));
        top.add(dateField);
        top.add(filterButton);
        top.add(clearButton);

        JButton cancelButton = new JButton("Cancel Appointment");
        JButton overrideButton = new JButton("Change Status");
        JButton exportButton = new JButton("Export to Text File");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(cancelButton);
        bottom.add(overrideButton);
        bottom.add(exportButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(appointmentTable), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        filterButton.addActionListener(e -> loadAppointments());
        clearButton.addActionListener(e -> {
            statusBox.setSelectedIndex(0);
            doctorBox.setSelectedIndex(0);
            dateField.setText("");
            loadAppointments();
        });
        cancelButton.addActionListener(e -> cancelAppointment());
        overrideButton.addActionListener(e -> changeStatus());
        exportButton.addActionListener(e -> GuiHelper.exportReport(this, store, shownAppointments));
        return panel;
    }

    private void loadDoctorBox() {
        doctorBox.removeAllItems();
        doctorBox.addItem("All doctors");
        for (Doctor d : store.getDoctors()) {
            doctorBox.addItem(d.getId() + " " + d.getName());
        }
    }

    private void loadAppointments() {
        try {
            AppointmentStatus status = null;
            if (statusBox.getSelectedIndex() > 0) {
                status = AppointmentStatus.valueOf(statusBox.getSelectedItem().toString());
            }
            String doctorId = null;
            if (doctorBox.getSelectedIndex() > 0) {
                // "D1001 Dr. Rahim" -> "D1001"
                doctorId = doctorBox.getSelectedItem().toString().split(" ")[0];
            }
            LocalDate date = null;
            if (!dateField.getText().trim().isEmpty()) {
                date = Validator.parseDate(dateField.getText());
            }

            shownAppointments = store.searchAppointments(null, doctorId, status, date);
            GuiHelper.clearTable(appointmentTable);
            for (Appointment a : shownAppointments) {
                GuiHelper.addRow(appointmentTable, a.getId(), store.getPatientName(a.getPatientId()),
                        store.getDoctorName(a.getDoctorId()), a.getDate(), a.getSlot().toString(),
                        a.getReason(), a.getStatus());
            }
        } catch (InvalidInputException e) {
            GuiHelper.showError(this, e.getMessage());
        }
    }

    private void cancelAppointment() {
        try {
            Appointment a = store.findAppointment(GuiHelper.getSelectedId(appointmentTable));
            if (GuiHelper.confirm(this, "Cancel appointment " + a.getId() + "?")) {
                store.cancel(a);
                loadSummary();
                loadAppointments();
            }
        } catch (InvalidInputException e) {
            GuiHelper.showError(this, e.getMessage());
        } catch (IOException e) {
            GuiHelper.showError(this, "Could not save data.\n" + e.getMessage());
        }
    }

    private void changeStatus() {
        try {
            Appointment a = store.findAppointment(GuiHelper.getSelectedId(appointmentTable));
            Object choice = JOptionPane.showInputDialog(this, "New status of " + a.getId() + ":",
                    "Change Status", JOptionPane.QUESTION_MESSAGE, null,
                    AppointmentStatus.values(), a.getStatus());
            if (choice == null) {
                return;
            }
            store.overrideStatus(a, (AppointmentStatus) choice);
            loadSummary();
            loadAppointments();
        } catch (InvalidInputException | DoctorNotFoundException | AppointmentConflictException e) {
            GuiHelper.showError(this, e.getMessage());
        } catch (IOException e) {
            GuiHelper.showError(this, "Could not save data.\n" + e.getMessage());
        }
    }
}
