import java.io.Serializable;

// Parent class of Patient, Doctor and Admin.
public abstract class User implements Serializable, Searchable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String phone;
    private String email;
    private String password;

    public User(String id, String name, String phone, String email, String password)
            throws InvalidInputException {
        this.id = id;
        setName(name);
        setPhone(phone);
        setEmail(email);
        setPassword(password);
    }

    // every child class must write these methods
    public abstract String getRole();

    public abstract void showDashboard(DataStore store);

    public String displayInfo() {
        return getRole() + ": " + name + " (" + id + ")";
    }

    @Override
    public boolean matches(String keyword) {
        String key = keyword.trim().toLowerCase();
        return id.toLowerCase().contains(key)
                || name.toLowerCase().contains(key)
                || phone.contains(key)
                || email.contains(key);
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidInputException {
        name = Validator.checkNotEmpty(name, "Name");
        if (!name.matches("[A-Za-z][A-Za-z .'-]{1,39}")) {
            throw new InvalidInputException("Name must be 2 to 40 characters and contain only letters.");
        }
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) throws InvalidInputException {
        phone = Validator.checkNotEmpty(phone, "Phone");
        if (!phone.matches("01[0-9]{9}")) {
            throw new InvalidInputException("Phone must be 11 digits and start with 01.");
        }
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) throws InvalidInputException {
        email = Validator.checkNotEmpty(email, "Email");
        if (!email.matches("[A-Za-z0-9._+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+")) {
            throw new InvalidInputException("Email is not valid. Example: name@gmail.com");
        }
        this.email = email.toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) throws InvalidInputException {
        if (password == null || password.length() < 6) {
            throw new InvalidInputException("Password must be at least 6 characters.");
        }
        this.password = password;
    }
}
