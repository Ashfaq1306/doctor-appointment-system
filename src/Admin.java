// There is only one admin account and it is fixed.
public class Admin extends User {

    private static final long serialVersionUID = 1L;

    public static final String ADMIN_EMAIL = "admin@hospital.com";
    public static final String ADMIN_PASSWORD = "admin123";

    public Admin() throws InvalidInputException {
        super("ADMIN", "System Admin", "01700000000", ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public void showDashboard(DataStore store) {
        new AdminDashboard(this, store).setVisible(true);
    }
}
