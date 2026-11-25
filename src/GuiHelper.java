import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

// Small methods which are used by many screens.
public class GuiHelper {

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Message", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String question) {
        int answer = JOptionPane.showConfirmDialog(parent, question, "Confirm", JOptionPane.YES_NO_OPTION);
        return answer == JOptionPane.YES_OPTION;
    }

    // makes a table which cannot be edited and can be sorted by clicking the column name
    public static JTable makeTable(String... columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            // needed to sort numbers as numbers, not as text
            @Override
            public Class<?> getColumnClass(int column) {
                if (getRowCount() == 0) {
                    return Object.class;
                }
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    public static void clearTable(JTable table) {
        ((DefaultTableModel) table.getModel()).setRowCount(0);
    }

    public static void addRow(JTable table, Object... values) {
        ((DefaultTableModel) table.getModel()).addRow(values);
    }

    // the id is always in the first column
    public static String getSelectedId(JTable table) throws InvalidInputException {
        int row = table.getSelectedRow();
        if (row == -1) {
            throw new InvalidInputException("Please select a row from the table first.");
        }
        return table.getValueAt(row, 0).toString();
    }

    public static void addField(JPanel panel, String label, Component field) {
        panel.add(new JLabel(label));
        panel.add(field);
    }

    public static void fillDoctorTable(JTable table, ArrayList<Doctor> list, DataStore store) {
        clearTable(table);
        for (Doctor d : list) {
            addRow(table, d.getId(), d.getName(), d.getSpecialization(), d.getDepartment(),
                    d.getExperience(), d.getFee(), store.getRatingText(d.getId()));
        }
    }

    public static void exportReport(Component parent, DataStore store, ArrayList<Appointment> list) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("appointment_report.txt"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file.exists() && !confirm(parent, "This file already exists. Replace it?")) {
            return;
        }
        try {
            FileManager.writeText(file, store.makeReport(list));
            showInfo(parent, "Report saved to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            showError(parent, "Could not save the report.\n" + e.getMessage());
        }
    }
}
