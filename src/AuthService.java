// Login related work.
public class AuthService {

    private DataStore store;

    public AuthService(DataStore store) {
        this.store = store;
    }

    // Returns a User reference. It can hold a Patient, Doctor or Admin object (polymorphism).
    public User login(String role, String email, String password)
            throws InvalidLoginException, InvalidInputException {
        email = Validator.checkNotEmpty(email, "Email").toLowerCase();
        if (password.isEmpty()) {
            throw new InvalidInputException("Password cannot be empty.");
        }

        User user = null;
        if (role.equals("Admin")) {
            Admin admin = new Admin();
            if (admin.getEmail().equals(email)) {
                user = admin;
            }
        } else if (role.equals("Doctor")) {
            for (Doctor d : store.getDoctors()) {
                if (d.getEmail().equals(email)) {
                    user = d;
                }
            }
        } else {
            for (Patient p : store.getPatients()) {
                if (p.getEmail().equals(email)) {
                    user = p;
                }
            }
        }

        if (user == null || !user.checkPassword(password)) {
            throw new InvalidLoginException("Wrong email or password for " + role + ".");
        }
        return user;
    }
}
