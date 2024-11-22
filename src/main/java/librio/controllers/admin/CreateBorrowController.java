package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import librio.auth.Session;
import librio.database.DatabaseConnection;
import librio.models.Book;
import librio.enums.Role;
import librio.models.User;
import librio.util.DesignUtil;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.*;
import static librio.util.DatabaseUtil.isEmailExists;

public class CreateBorrowController implements Initializable {
    @FXML
    private Button createButton;

    @FXML
    private TextField emailTextField;

    @FXML
    private ImageView bookImageView;

    @FXML
    private Label bookIsbnErrorLabel;

    @FXML
    private TextField bookIsbnTextField;

    @FXML
    private TextField bookTitleTextField;

    @FXML
    private TextField borrowDateTextField;

    @FXML
    private DatePicker dueDatePicker;

    @FXML
    private Label emailErrorLabel;

    @FXML
    private TextField nameTextField;

    @FXML
    private Label dueDateErrorLabel;

    @FXML
    private Label userAlreadyBorrowErrorLabel;

    public void initialize(URL location, ResourceBundle resources) {
        borrowDateTextField.setText(LocalDate.now().toString());
        dueDatePicker.setValue(LocalDate.now().plusDays(30));
        hideErrorLabels();
        addListeners();
    }


    @FXML
    private void createBorrow() {
        String email = emailTextField.getText();
        String isbn = bookIsbnTextField.getText();
        LocalDate dueDate = dueDatePicker.getValue();

        Book book = getBookByIsbn(isbn);

        boolean validation = false;

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
        } else if (book != null && book.getQuantityCopy() <= 0) {
            bookIsbnErrorLabel.setText("Out of stock!");
            validation = true;
        }

        if (dueDate.isBefore(LocalDate.now())) {
            dueDateErrorLabel.setText("Due date must not be before current date!");
            validation = true;
        } else if (ChronoUnit.DAYS.between(LocalDate.now(), dueDate) > 60) {
            dueDateErrorLabel.setText("The borrowing period cannot exceed 60 days!");
            validation = true;
        }

        if (isBookAlreadyBorrowedByUser(getUserByEmail(email).getId(), isbn) == true) {
            userAlreadyBorrowErrorLabel.setText("This user has already borrowed this book!");
            validation = true;
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


    @FXML
    private void back() {
        closeStage();
    }

    private void addListeners() {
        emailTextField.setOnMouseClicked(event -> {hideErrorLabels();});
        bookIsbnTextField.setOnMouseClicked(event -> {hideErrorLabels();});
        borrowDateTextField.setOnMouseClicked(event -> {hideErrorLabels();});
        nameTextField.setOnMouseClicked(event -> {hideErrorLabels();});
        dueDatePicker.setOnMouseClicked(event -> {hideErrorLabels();});

        // Email validation
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

    private void closeStage() {
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }

    private void hideErrorLabels() {
        emailErrorLabel.setText("");
        bookIsbnErrorLabel.setText("");
        dueDateErrorLabel.setText("");
        userAlreadyBorrowErrorLabel.setText("");
    }
}
