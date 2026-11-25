public class Patient extends User {

    private static final long serialVersionUID = 1L;

    private int age;

    public Patient(String id, String name, int age, String phone, String email, String password)
            throws InvalidInputException {
        super(id, name, phone, email, password);
        setAge(age);
    }

    public void updateProfile(String name, int age, String phone, String email, String password)
            throws InvalidInputException {
        // First make a temporary object. If any value is wrong an exception is thrown
        // here, so the real profile is never half updated.
        new Patient(getId(), name, age, phone, email, password);

        setName(name);
        setAge(age);
        setPhone(phone);
        setEmail(email);
        setPassword(password);
    }

    @Override
    public String getRole() {
        return "Patient";
    }

    @Override
    public String displayInfo() {
        return super.displayInfo() + ", Age: " + age;
    }

    @Override
    public void showDashboard(DataStore store) {
        new PatientDashboard(this, store).setVisible(true);
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) throws InvalidInputException {
        if (age < 1 || age > 120) {
            throw new InvalidInputException("Age must be between 1 and 120.");
        }
        this.age = age;
    }
}
