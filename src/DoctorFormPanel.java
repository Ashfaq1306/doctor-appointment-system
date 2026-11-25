import java.awt.GridLayout;
import java.io.IOException;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

// Doctor form. The admin uses it to add or edit a doctor, the doctor uses it for the profile.
public class DoctorFormPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextField nameField = new JTextField(18);
    private JTextField phoneField = new JTextField();
    private JTextField emailField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private JComboBox<String> specializationBox = new JComboBox<>(new String[] {
        "Medicine Specialist", "Cardiologist", "Dermatologist", "Neurologist", "Orthopedic Surgeon",
        "Pediatrician", "Gynecologist", "ENT Specialist", "Dentist", "Eye Specialist"});
    private JTextField departmentField = new JTextField();
    private JTextField feeField = new JTextField();
    private JTextField experienceField = new JTextField();

    public DoctorFormPanel(boolean newDoctor) {
        setLayout(new GridLayout(8, 2, 10, 10));
        specializationBox.setEditable(true); // the user can also type a new one
        GuiHelper.addField(this, "Name:", nameField);
        GuiHelper.addField(this, "Phone:", phoneField);
        GuiHelper.addField(this, "Email:", emailField);
        String passwordLabel = newDoctor ? "Password (min 6):" : "New password (empty = no change):";
        GuiHelper.addField(this, passwordLabel, passwordField);
        GuiHelper.addField(this, "Specialization:", specializationBox);
        GuiHelper.addField(this, "Department:", departmentField);
        GuiHelper.addField(this, "Fee (Tk):", feeField);
        GuiHelper.addField(this, "Experience (years):", experienceField);
    }

    public void fill(Doctor d) {
        nameField.setText(d.getName());
        phoneField.setText(d.getPhone());
        emailField.setText(d.getEmail());
        passwordField.setText("");
        specializationBox.setSelectedItem(d.getSpecialization());
        departmentField.setText(d.getDepartment());
        feeField.setText(String.valueOf(d.getFee()));
        experienceField.setText(String.valueOf(d.getExperience()));
    }

    // If doctor is null a new doctor is added, otherwise the doctor is updated.
    public void save(Doctor doctor, DataStore store) throws InvalidInputException, IOException {
        double fee = Validator.parseDouble(feeField.getText(), "Fee");
        int experience = Validator.parseInt(experienceField.getText(), "Experience");
        String password = new String(passwordField.getPassword());
        // getEditor is used because the typed text may not be in the list
        String specialization = specializationBox.getEditor().getItem().toString();

        if (doctor == null) {
            store.addDoctor(nameField.getText(), phoneField.getText(), emailField.getText(), password,
                    specialization, departmentField.getText(), fee, experience);
        } else {
            if (password.isEmpty()) {
                password = doctor.getPassword();
            }
            store.updateDoctor(doctor, nameField.getText(), phoneField.getText(), emailField.getText(),
                    password, specialization, departmentField.getText(), fee, experience);
        }
    }
}
