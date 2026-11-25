import java.awt.GridLayout;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class RegisterDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private LoginFrame loginFrame;
    private DataStore store;
    private JTextField nameField = new JTextField();
    private JTextField ageField = new JTextField();
    private JTextField phoneField = new JTextField();
    private JTextField emailField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private JPasswordField confirmField = new JPasswordField();

    public RegisterDialog(LoginFrame loginFrame, DataStore store) {
        super(loginFrame, "Patient Registration", true);
        this.loginFrame = loginFrame;
        this.store = store;

        setSize(400, 340);
        setLocationRelativeTo(loginFrame);

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GuiHelper.addField(form, "Full name:", nameField);
        GuiHelper.addField(form, "Age:", ageField);
        GuiHelper.addField(form, "Phone (01XXXXXXXXX):", phoneField);
        GuiHelper.addField(form, "Email:", emailField);
        GuiHelper.addField(form, "Password (min 6):", passwordField);
        GuiHelper.addField(form, "Confirm password:", confirmField);

        JButton cancelButton = new JButton("Cancel");
        JButton registerButton = new JButton("Register");
        form.add(cancelButton);
        form.add(registerButton);
        add(form);

        cancelButton.addActionListener(e -> dispose());
        registerButton.addActionListener(e -> register());
    }

    private void register() {
        try {
            int age = Validator.parseInt(ageField.getText(), "Age");
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());
            if (!password.equals(confirm)) {
                throw new InvalidInputException("The two passwords are not the same.");
            }
            Patient patient = store.addPatient(nameField.getText(), age, phoneField.getText(),
                    emailField.getText(), password);
            GuiHelper.showInfo(this, "Registration successful. Your id is " + patient.getId() + ".");
            loginFrame.setEmail(patient.getEmail());
            dispose();
        } catch (InvalidInputException e) {
            GuiHelper.showError(this, e.getMessage());
        } catch (IOException e) {
            GuiHelper.showError(this, "Could not save data.\n" + e.getMessage());
        }
    }
}
