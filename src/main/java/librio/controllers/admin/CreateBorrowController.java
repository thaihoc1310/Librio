package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import librio.session.Session;
import librio.database.DatabaseConnection;
import librio.models.Book;
import librio.enums.Role;
import librio.models.User;
import librio.util.DesignUtil;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.*;
import static librio.util.DatabaseUtil.isEmailExists;
import static librio.util.DesignUtil.setDatePickerFormat;

/**
 * The CreateBorrowController class is responsible for managing the borrowing process
 * of books in a library system. This class is implemented as a JavaFX controller
 * and handles various user interactions via the UI. The primary responsibilities
 * include validating user inputs for creating borrow transactions, updating the
 * system's database accordingly, and managing UI components such as buttons,
 * text fields, and labels.
 */
public class CreateBorrowController implements Initializable {
    @FXML
    protected Button createButton;

    @FXML
    protected TextField emailTextField;
    @FXML
    protected TextField bookIsbnTextField;
    @FXML
    protected TextField bookTitleTextField;
    @FXML
    protected TextField nameTextField;

    @FXML
    protected ImageView bookImageView;

    @FXML
    protected Label bookIsbnErrorLabel;
    @FXML
    protected Label emailErrorLabel;
    @FXML
    protected Label dueDateErrorLabel;
    @FXML
    protected Label userAlreadyBorrowErrorLabel;

    @FXML
    protected DatePicker borrowDatePicker;
    @FXML
    protected DatePicker dueDatePicker;


    /**
     * Initializes the controller class by setting up the date picker, hiding error labels,
     * and adding listeners to UI components for user interaction.
     *
     * @param location The location used to resolve relative paths for the root object, or
     *                 null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the
     *                  root object was not localized.
     */
    public void initialize(URL location, ResourceBundle resources) {
        initDatePicker();
        hideErrorLabels();
        addListeners();
    }

    /**
     * Initializes the date pickers for borrowing and setting due dates.
     * The borrowing date picker is set to the current date, and the due
     * date picker is set to 30 days from the current date. The format
     * for displaying dates in the due date picker is also set.
     */
    private void initDatePicker() {
        setDatePickerFormat(dueDatePicker);
        setDatePickerFormat(borrowDatePicker);
        borrowDatePicker.setValue(LocalDate.now());
        dueDatePicker.setValue(LocalDate.now().plusDays(30));
    }

    /**
     * Handles the creation of a new borrow transaction for a library system.
     * This method validates user input such as email, book ISBN, and date formats before processing.
     * If validation fails, appropriate error messages are displayed on the UI.
     * If all validations pass, the method records the borrow transaction in the database and updates the book's available copies.
     * Utilizes various helper methods to verify user and book information, and to check constraints related to roles and borrowing periods.
     */
    @FXML
    protected void createBorrow() {
        String email = emailTextField.getText();
        String isbn = bookIsbnTextField.getText();
        String dueDateString = dueDatePicker.getEditor().getText();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate dueDate = null;
        LocalDate borrowDate = LocalDate.parse(borrowDatePicker.getEditor().getText(), formatter);

        String dateRegex = "^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/\\d{4}$";
        boolean validation = false;

        Book book = getBookByIsbn(isbn);

        if (email.trim().isEmpty()) {
            emailErrorLabel.setText("Email is required!");
            validation = true;
        } else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            emailErrorLabel.setText("Invalid email format!");
            validation = true;
        } else if (!isEmailExists(email)) {
            emailErrorLabel.setText("Email not exists!");
            validation = true;
        } else {
            User user = getUserByEmail(email);
            if (user.getRole() == Role.LIBRARIAN) {
                emailErrorLabel.setText("LIBRARIAN cannot borrow books!");
                validation = true;
            }
        }

        if (isbn.isEmpty()) {
            bookIsbnErrorLabel.setText("ISBN must not be empty!");
            validation = true;
        } else if (!isbn.matches("\\d{10}|\\d{13}")) {
            bookIsbnErrorLabel.setText("ISBN must have 10 or 13 digits!");
            validation = true;
        } else if (book == null) {
            bookIsbnErrorLabel.setText("Book not exists!");
            validation = true;
        } else if (book != null && book.getAvailableCopy() <= 0) {
            bookIsbnErrorLabel.setText("Out of stock!");
            validation = true;
        }

        if (isBookAlreadyBorrowedByUser(getUserByEmail(email).getId(), isbn) == true) {
            userAlreadyBorrowErrorLabel.setText("This user has already borrowed this book!");
            validation = true;
        }

        if (!dueDateString.matches(dateRegex)) {
            dueDateErrorLabel.setText("Invalid date format!");
            validation = true;
        } else {
            try {
                dueDate = LocalDate.parse(dueDateString, formatter);
                if (dueDate.isBefore(borrowDate)) {
                    dueDateErrorLabel.setText("Due date cannot be before borrow date!");
                    validation = true;
                } else if (ChronoUnit.DAYS.between(borrowDate, dueDate) > 90) {
                    dueDateErrorLabel.setText("The borrowing period cannot exceed 90 days!");
                    validation = true;
                }
            } catch (Exception e) {
                dueDateErrorLabel.setText("Invalid date format!");
                validation = true;
            }
        }

        if (validation) {
            return;
        }

        String memberId = getUserByEmail(email).getId();

        String query = "INSERT INTO borrows (member_id, book_isbn, borrow_date, due_date, return_date, status, fine, created_by, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        String updateBookQuery = "UPDATE books SET available_copy = available_copy - 1 WHERE isbn = ? AND available_copy > 0";
        try (Connection connection = DatabaseConnection.getConnection()) {

            try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement updateBookStatement = connection.prepareStatement(updateBookQuery)) {
                statement.setString(1, memberId);
                statement.setString(2, isbn);
                statement.setString(3, LocalDate.now().toString());
                statement.setString(4, dueDate.toString());
                statement.setString(5, null);
                statement.setString(6, "BORROWING");
                statement.setString(7, String.valueOf(0));
                statement.setString(8, Session.getInstance().getLoggedInUser().getEmail());

                int rowsInserted = statement.executeUpdate();

                if (rowsInserted > 0) {
                    updateBookStatement.setString(1, isbn);
                    updateBookStatement.executeUpdate();
                }
                closeStage();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Handles the action of navigating back to the previous screen.
     * This method is triggered by the associated FXML control, typically a back button,
     * and closes the current stage, effectively returning the user to the previous state.
     */
    @FXML
    private void back() {
        closeStage();
    }

    /**
     * Adds event listeners to various UI components to manage user interactions and input validation.
     *
     * This method sets mouse click event handlers for several text fields and a date picker to
     * trigger `hideErrorLabels()`, which hides error labels. It also adds text change listeners
     * to `emailTextField` and `bookIsbnTextField` to handle logical operations based on user inputs.
     *
     * For `emailTextField`, the listener checks if the input is a valid, non-empty email address
     * and if the email exists in the database. If these conditions are met, it retrieves the user
     * details and updates `nameTextField` with the user's name. Otherwise, it clears the name field.
     *
     * For `bookIsbnTextField`, the listener checks if the input is a valid ISBN (either 10 or 13 digits)
     * and if the corresponding book exists. If valid, it updates `bookTitleTextField` with the book's title,
     * retrieves and displays the book's image if available, or loads a default image otherwise.
     * This ensures that user inputs are validated in real-time and related field values are auto-filled
     * when a valid input matches existing records.
     */
    private void addListeners() {
        emailTextField.setOnMouseClicked(event -> {hideErrorLabels();});
        bookIsbnTextField.setOnMouseClicked(event -> {hideErrorLabels();});
        nameTextField.setOnMouseClicked(event -> {hideErrorLabels();});
        dueDatePicker.setOnMouseClicked(event -> {hideErrorLabels();});

        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.trim().isEmpty() &&
                    newValue.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$") &&
                    isEmailExists(newValue)) {
                User user = getUserByEmail(newValue);
                nameTextField.setText(user.getName());
            }else{
                nameTextField.setText("");
            }
        });


        bookIsbnTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            Book book = getBookByIsbn(newValue);
            if (!newValue.trim().isEmpty() && newValue.matches("\\d{10}|\\d{13}") && book != null) {
                bookTitleTextField.setText(book.getTitle());
                String projectDir = System.getProperty("user.dir");
                String booksDir = projectDir + "/src/main/resources/images/book/";
                String path = booksDir + book.getImagePath();
                File file = new File(path);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    DesignUtil.cropToAspectRatio(image, bookImageView, 194, 280);
                } else {
                    DesignUtil.loadDefaultBookImage(bookImageView);
                }
            }else{
                bookTitleTextField.setText("");
            }
        });


    }

    /**
     * Closes the current window or stage associated with the createButton.
     * This method is typically used to close the window after an operation is completed,
     * or to navigate back from the current view.
     */
    private void closeStage() {
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Clears the text of all error labels related to email, book ISBN, due date, and previous borrow conditions.
     * This method is typically called to reset the UI state by hiding any error messages displayed to the user.
     */
    private void hideErrorLabels() {
        emailErrorLabel.setText("");
        bookIsbnErrorLabel.setText("");
        dueDateErrorLabel.setText("");
        userAlreadyBorrowErrorLabel.setText("");
    }
}
