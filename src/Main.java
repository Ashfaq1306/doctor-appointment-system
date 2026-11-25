import java.io.IOException;
import javax.swing.SwingUtilities;

// Program starts from here.
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                start();
            }
        });
    }

    private static void start() {
        try {
            DataStore store = new DataStore("data");
            store.addSampleDoctors();
            new LoginFrame(store).setVisible(true);
        } catch (IOException | InvalidInputException e) {
            GuiHelper.showError(null, "Could not load the data files.\n" + e.getMessage());
        }
    }
}
