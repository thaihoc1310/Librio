package librio.controllers.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import librio.database.DatabaseConnection;
import librio.models.Book;
import librio.models.Role;
import librio.models.User;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.*;

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

    public void initialize(URL location, ResourceBundle resources) {
        borrowDateTextField.setText(LocalDate.now().toString());
        dueDatePicker.setValue(LocalDate.now().plusDays(30));
        hideErrorLabels();
        addListeners();
    }

    @FXML
    private void createBorrow(){
        String email = emailTextField.getText();
        String isbn = bookIsbnTextField.getText();
        LocalDate dueDate = dueDatePicker.getValue();

        Book book = getBookByIsbn(isbn);

        boolean validation = false;

        if(email.isEmpty()){
            emailErrorLabel.setText("Email is required!");
            validation = true;
        }else if(!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")){
            emailErrorLabel.setText("Invalid email format!");
            validation = true;
        }else if(!isEmailExists(email)){
            emailErrorLabel.setText("Email not exists!");
            validation = true;
        }else {
            User user = getUserByEmail(email);
            if(user == null || user.getRole() == Role.LIBRARIAN){
                emailErrorLabel.setText("LIBRARIAN cannot borrow books!");
                validation = true;
            }
        }

        if(isbn.isEmpty() ){
            bookIsbnErrorLabel.setText("ISBN must not be empty!");
            validation = true;
        } else if (!isbn.matches("\\d{10}|\\d{13}")) {
            bookIsbnErrorLabel.setText("ISBN must have 10 or 13 digits!");
            validation = true;
        }else if(book == null){
            bookIsbnErrorLabel.setText("Book not exists!");
            validation = true;
        }

        if(dueDate == null){
            dueDatePicker.setValue(LocalDate.now().plusDays(14));
        }else if(dueDate.isBefore(LocalDate.now())){
            dueDateErrorLabel.setText("Due date must be before current date!");
            validation = true;
        }

        if(validation){
            return;
        }

        String memberId = getUserByEmail(email).getId();

        String query = "INSERT INTO borrows (member_id, book_isbn, borrow_date, due_date, return_date, status, fine) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, memberId);
            statement.setString(2, isbn);
            statement.setString(3, LocalDate.now().toString());
            statement.setString(4, dueDate.toString());
            statement.setString(5, null);
            statement.setString(6, "BORROWING");
            statement.setString(7, String.valueOf(0));

            int rowsInserted = statement.executeUpdate();
            closeStage();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void hideErrorLabels() {
        emailErrorLabel.setText("");
        dueDateErrorLabel.setText("");
        bookIsbnErrorLabel.setText("");
    }

    @FXML
    private void back() {
        closeStage();
    }

    private void addListeners() {

        // Email validation
        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue);
            if (newValue.trim().isEmpty()) {
                emailErrorLabel.setText("Email is required!");
            } else if (!newValue.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
                emailErrorLabel.setText("Invalid email format!");
            } else if (!isEmailExists(newValue)) {
                emailErrorLabel.setText("Email not exists!");
            } else {
                User user = getUserByEmail(newValue);
                if(user == null || user.getRole() == Role.LIBRARIAN){
                    emailErrorLabel.setText("LIBRARIAN cannot borrow books!");
                    nameTextField.setText("");
                }else{
                    emailErrorLabel.setText("");
                    nameTextField.setText(user.getName());
                }
            }
        });

        bookIsbnTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                bookIsbnErrorLabel.setText("ISBN must not be empty!");
            } else if (!newValue.matches("\\d{10}|\\d{13}")) {
                bookIsbnErrorLabel.setText("ISBN must have 10 or 13 digits!");
            } else if (getBookByIsbn(newValue) == null) {
                bookIsbnErrorLabel.setText("Book not exists!");
            } else {
                bookIsbnErrorLabel.setText("");
                Book book = getBookByIsbn(newValue);
                if (book != null) {
                    bookTitleTextField.setText(book.getTitle());
                } else {
                    bookTitleTextField.setText("");
                }
            }
        });

        dueDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                dueDateErrorLabel.setText("Due date is required!");
                dueDatePicker.setValue(LocalDate.now().plusDays(14));
            } else if (newValue.isBefore(LocalDate.now())) {
                dueDateErrorLabel.setText("Due date must not be before current date!");
            } else {
                dueDateErrorLabel.setText("");
            }
        });

    }

    private void closeStage() {
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }
}