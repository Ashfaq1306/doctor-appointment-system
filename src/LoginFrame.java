import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private DataStore store;
    private JComboBox<String> roleBox = new JComboBox<>(new String[] {"Patient", "Doctor", "Admin"});
    private JTextField emailField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();

    public LoginFrame(DataStore store) {
        this.store = store;

        setTitle("Doctor Appointment System - Login");
        setSize(420, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel title = new JLabel("Doctor Appointment System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));
        GuiHelper.addField(form, "Login as:", roleBox);
        GuiHelper.addField(form, "Email:", emailField);
        GuiHelper.addField(form, "Password:", passwordField);

        JButton registerButton = new JButton("Register (Patient)");
        JButton loginButton = new JButton("Login");
        form.add(registerButton);
        form.add(loginButton);
        add(form, BorderLayout.CENTER);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> new RegisterDialog(this, store).setVisible(true));
        getRootPane().setDefaultButton(loginButton); // Enter key
    }

    private void login() {
        try {
            AuthService auth = new AuthService(store);
            String role = roleBox.getSelectedItem().toString();
            String password = new String(passwordField.getPassword());

            // polymorphism: user can be a Patient, Doctor or Admin
            User user = auth.login(role, emailField.getText(), password);
            dispose();
            user.showDashboard(store);
        } catch (InvalidLoginException | InvalidInputException e) {
            passwordField.setText("");
            GuiHelper.showError(this, e.getMessage());
        }
    }

    // used after registration
    public void setEmail(String email) {
        roleBox.setSelectedItem("Patient");
        emailField.setText(email);
    }
}
