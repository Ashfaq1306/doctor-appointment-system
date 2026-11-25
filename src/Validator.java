import java.time.LocalDate;
import java.time.format.DateTimeParseException;

// Small helper methods to check user input.
public class Validator {

    public static String checkNotEmpty(String text, String fieldName) throws InvalidInputException {
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidInputException(fieldName + " cannot be empty.");
        }
        return text.trim();
    }

    public static int parseInt(String text, String fieldName) throws InvalidInputException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException(fieldName + " must be a whole number.");
        }
    }

    public static double parseDouble(String text, String fieldName) throws InvalidInputException {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException(fieldName + " must be a number.");
        }
    }

    public static LocalDate parseDate(String text) throws InvalidInputException {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Date must be valid and look like 2026-01-31 (yyyy-mm-dd).");
        }
    }
}
