import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

// Saves and loads lists using object serialization.
public class FileManager {

    private String folder;

    public FileManager(String folder) throws IOException {
        this.folder = folder;
        File dir = new File(folder);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create folder: " + folder);
        }
    }

    public <T> void saveList(String fileName, ArrayList<T> list) throws IOException {
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(new File(folder, fileName)));
            out.writeObject(list);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> loadList(String fileName) throws IOException {
        File file = new File(folder, fileName);
        if (!file.exists()) {
            // first run: create the file with an empty list
            saveList(fileName, new ArrayList<T>());
        }

        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(file));
            return (ArrayList<T>) in.readObject();
        } catch (FileNotFoundException e) {
            throw new IOException("The file " + fileName + " could not be opened.");
        } catch (ClassNotFoundException e) {
            throw new IOException("The file " + fileName + " could not be read.");
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    // writes a normal text file, used for the report
    public static void writeText(File file, String text) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(text);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
