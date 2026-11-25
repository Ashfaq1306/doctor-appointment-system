import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

// Common part of the three dashboards: title, logout button and tabs.
public abstract class DashboardFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    protected DataStore store;
    protected JTabbedPane tabs = new JTabbedPane();

    private User user;
    private JLabel infoLabel = new JLabel();
    private JLabel reminderLabel = new JLabel(" ");

    public DashboardFrame(User user, DataStore store) {
        this.user = user;
        this.store = store;

        setTitle("Doctor Appointment System - " + user.getRole());
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        infoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        JPanel top = new JPanel(new BorderLayout(10, 5));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        top.add(infoLabel, BorderLayout.CENTER);
        top.add(logoutButton, BorderLayout.EAST);
        top.add(reminderLabel, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        showUserInfo();
    }

    // every dashboard loads its own tables
    protected abstract void refreshAll();

    protected void showUserInfo() {
        infoLabel.setText(user.displayInfo());
    }

    protected void setReminder(String text) {
        reminderLabel.setText(text);
    }

    private void logout() {
        if (GuiHelper.confirm(this, "Do you want to logout?")) {
            dispose();
            new LoginFrame(store).setVisible(true);
        }
    }
}
