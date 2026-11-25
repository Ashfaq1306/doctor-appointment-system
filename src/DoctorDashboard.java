import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class DoctorDashboard extends DashboardFrame {

    private static final long serialVersionUID = 1L;

    private Doctor doctor;

    // requests tab
    private JTable requestTable = GuiHelper.makeTable("Id", "Patient", "Age", "Phone", "Date", "Time", "Reason");

    // schedule tab
    private JComboBox<String> scheduleBox = new JComboBox<>(new String[] {"Today", "Next 7 days", "All"});
    private JTable scheduleTable = GuiHelper.makeTable("Id", "Date", "Time", "Patient", "Reason", "Status");

    // patients tab
    private JTable patientTable = GuiHelper.makeTable("Id", "Name", "Age", "Phone", "Email");

    // availability tab
    private JComboBox<DayOfWeek> dayBox = new JComboBox<>(DayOfWeek.values());
    private JComboBox<LocalTime> fromBox = new JComboBox<>();
    private JComboBox<LocalTime> toBox = new JComboBox<>();
    private DefaultListModel<TimeSlot> slotModel = new DefaultListModel<>();
    private JList<TimeSlot> slotList = new JList<>(slotModel);

    // profile tab
    private DoctorFormPanel form = new DoctorFormPanel(false);

    public DoctorDashboard(Doctor doctor, DataStore store) {
        super(doctor, store);
        this.doctor = doctor;

        tabs.addTab("Requests", createRequestTab());
        tabs.addTab("My Schedule", createScheduleTab());
        tabs.addTab("My Patients", createPatientTab());
        tabs.addTab("Availability", createAvailabilityTab());
        tabs.addTab("My Profile", createProfileTab());

        form.fill(doctor);
        loadSlots();
        refreshAll();
    }

    @Override
    protected void refreshAll() {
        loadRequests();
        loadSchedule();
        loadPatients();

        int today = store.searchAppointments(null, doctor.getId(), AppointmentStatus.APPROVED,
                LocalDate.now()).size();
        setReminder("Today's approved appointments: " + today
                + "     New requests: " + requestTable.getRowCount()
                + "     Rating: " + store.getRatingText(doctor.getId()));
    }

    // ---------- requests tab ----------

    private JPanel createRequestTab() {
        JButton approveButton = new JButton("Approve");
        JButton rejectButton = new JButton("Reject");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(approveButton);
        buttons.add(rejectButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(requestTable), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        approveButton.addActionListener(e -> approveRequest());
        rejectButton.addActionListener(e -> rejectRequest());
        return panel;
    }

    private void loadRequests() {
        GuiHelper.clearTable(requestTable);
        for (Appointment a : store.searchAppointments(null, doctor.getId(), AppointmentStatus.PENDING, null)) {
            Patient p = store.findPatient(a.getPatientId());
            if (p != null) {
                GuiHelper.addRow(requestTable, a.getId(), p.getName(), p.getAge(), p.getPhone(),
                        a.getDate(), a.getSlot().toString(), a.getReason());
            }
        }
    }

    private void approveRequest() {
        try {
            Appointment a = store.findAppointment(GuiHelper.getSelectedId(requestTable));
            store.approve(a);
            refreshAll();
        } catch (InvalidInputException e) {
            GuiHelper.showError(this, e.getMessage());
        } catch (IOException e) {
            GuiHelper.showError(this, "Could not save data.\n" + e.getMessage());
        }
    }

    private void rejectRequest() {
        try {
            Appointment a = store.findAppointment(GuiHelper.getSelectedId(requestTable));
            if (!GuiHelper.confirm(this, "Reject appointment " + a.getId() + "?")) {
                return;
            }
            store.reject(a);
            refreshAll();
        } catch (InvalidInputException e) {
            GuiHelper.showError(this, e.getMessage());
        } catch (IOException e) {
            GuiHelper.showError(this, "Could not save data.\n" + e.getMessage());
        }
    }

    // ---------- schedule tab ----------

    private JPanel createScheduleTab() {
        JButton completeButton = new JButton("Mark as Completed");
        JButton exportButton = new JButton("Export to Text File");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Show:"));
        top.add(scheduleBox);
        top.add(completeButton);
        top.add(exportButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        scheduleBox.addActionListener(e -> loadSchedule());
        completeButton.addActionListener(e -> completeAppointment());
        exportButton.addActionListener(e -> GuiHelper.exportReport(this, store, getSchedule()));
        return panel;
    }

    // approved and completed appointments of the selected days
    private ArrayList<Appointment> getSchedule() {
        LocalDate today = LocalDate.now();
        String choice = scheduleBox.getSelectedItem().toString();
        ArrayList<Appointment> result = new ArrayList<>();
        for (Appointment a : store.searchAppointments(null, doctor.getId(), null, null)) {
            if (a.getStatus() != AppointmentStatus.APPROVED && a.getStatus() != AppointmentStatus.COMPLETED) {
                continue;
            }
            LocalDate date = a.getDate();
            boolean inWeek = !date.isBefore(today) && date.isBefore(today.plusDays(7));
            if (choice.equals("All")
                    || choice.equals("Today") && date.equals(today)
                    || choice.equals("Next 7 days") && inWeek) {
                result.add(a);
            }
        }
        return result;
    }

    private void loadSchedule() {
        GuiHelper.clearTable(scheduleTable);
        for (Appointment a : getSchedule()) {
            GuiHelper.addRow(scheduleTable, a.getId(), a.getDate(), a.getSlot().toString(),
                    store.getPatientName(a.getPatientId()), a.getReason(), a.getStatus());
        }
    }

    private void completeAppointment() {
        try {
            Appointment a = store.findAppointment(GuiHelper.getSelectedId(scheduleTable));
            store.complete(a);
            refreshAll();
        } catch (InvalidInputException e) {
            GuiHelper.showError(this, e.getMessage());
        } catch (IOException e) {
            GuiHelper.showError(this, "Could not save data.\n" + e.getMessage());
        }
    }

    // ---------- patients tab ----------

    private JPanel createPatientTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(patientTable), BorderLayout.CENTER);
        return panel;
    }

    // patients who have an approved or completed appointment with this doctor
    private void loadPatients() {
        GuiHelper.clearTable(patientTable);
        ArrayList<Patient> added = new ArrayList<>();
        for (Appointment a : store.searchAppointments(null, doctor.getId(), null, null)) {
            if (a.getStatus() != AppointmentStatus.APPROVED && a.getStatus() != AppointmentStatus.COMPLETED) {
                continue;
            }
            Patient p = store.findPatient(a.getPatientId());
            if (p != null && !added.contains(p)) {
                added.add(p);
                GuiHelper.addRow(patientTable, p.getId(), p.getName(), p.getAge(), p.getPhone(), p.getEmail());
            }
        }
    }

    // ---------- availability tab ----------

    private JPanel createAvailabilityTab() {
        // 08:00, 08:30 ... 22:00
        for (int hour = 8; hour <= 21; hour++) {
            fromBox.addItem(LocalTime.of(hour, 0));
            fromBox.addItem(LocalTime.of(hour, 30));
            toBox.addItem(LocalTime.of(hour, 30));
            toBox.addItem(LocalTime.of(hour + 1, 0));
        }
        fromBox.setSelectedItem(LocalTime.of(9, 0));
        toBox.setSelectedItem(LocalTime.of(12, 0));

        JButton addButton = new JButton("Add Slots");
        JButton removeButton = new JButton("Remove Selected Slots");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Day:"));
        top.add(dayBox);
        top.add(new JLabel("From:"));
        top.add(fromBox);
        top.add(new JLabel("To:"));
        top.add(toBox);
        top.add(addButton);
        top.add(removeButton);

        JScrollPane scroll = new JScrollPane(slotList);
        scroll.setBorder(BorderFactory.createTitledBorder("Slots of the selected day (every week)"));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        dayBox.addActionListener(e -> loadSlots());
        addButton.addActionListener(e -> addSlots());
        removeButton.addActionListener(e -> removeSlots());
        return panel;
    }

    private void loadSlots() {
        slotModel.clear();
        for (TimeSlot slot : doctor.getSlots((DayOfWeek) dayBox.getSelectedItem())) {
            slotModel.addElement(slot);
        }
    }

    private void addSlots() {
        try {
            int count = store.addSlots(doctor, (DayOfWeek) dayBox.getSelectedItem(),
                    (LocalTime) fromBox.getSelectedItem(), (LocalTime) toBox.getSelectedItem());
            loadSlots();
            GuiHelper.showInfo(this, count + " new slot(s) added.");
        } catch (InvalidInputException e) {
            GuiHelper.showError(this, e.getMessage());
        } catch (IOException e) {
            GuiHelper.showError(this, "Could not save data.\n" + e.getMessage());
        }
    }

    private void removeSlots() {
        if (slotList.isSelectionEmpty()) {
            GuiHelper.showError(this, "Please select one or more slots from the list.");
            return;
        }
        if (!GuiHelper.confirm(this, "Remove the selected slots?\nAlready booked appointments will not change.")) {
            return;
        }
        try {
            for (TimeSlot slot : slotList.getSelectedValuesList()) {
                store.removeSlot(doctor, slot);
            }
            loadSlots();
        } catch (IOException e) {
            GuiHelper.showError(this, "Could not save data.\n" + e.getMessage());
        }
    }

    // ---------- profile tab ----------

    private JPanel createProfileTab() {
        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> saveProfile());
        JPanel buttons = new JPanel();
        buttons.add(saveButton);

        JPanel box = new JPanel(new BorderLayout());
        box.add(form, BorderLayout.CENTER);
        box.add(buttons, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));
        panel.add(box, BorderLayout.NORTH);
        return panel;
    }

    private void saveProfile() {
        try {
            form.save(doctor, store);
            form.fill(doctor);
            showUserInfo();
            GuiHelper.showInfo(this, "Profile updated.");
        } catch (InvalidInputException e) {
            GuiHelper.showError(this, e.getMessage());
        } catch (IOException e) {
            GuiHelper.showError(this, "Could not save data.\n" + e.getMessage());
        }
    }
}
