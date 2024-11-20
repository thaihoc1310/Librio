package librio.controllers.member;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import librio.auth.Session;
import librio.database.DatabaseConnection;
import librio.models.Book;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Consumer;

public class ConfirmBorrow {

    @FXML
    private Label authorNameLabel;

    @FXML
    private ImageView bookImage;

    @FXML
    private AnchorPane borrowConfirmationPane;

    @FXML
    private Button confirmButton;

    @FXML
    private Text dueDateLabel;

    @FXML
    private Label titleLabel;

    private Book book;

    private LocalDate dueDate;

    public void setBook (Book book) {
        this.book = book;

        titleLabel.setText(book.getTitle());
        authorNameLabel.setText(book.getAuthor());
        dueDate = LocalDate.now().plusDays(90);
        dueDateLabel.setText("Due Date:  " + dueDate);

        String projectDir = System.getProperty("user.dir");
        String booksDir = projectDir + "/src/main/resources/images/book/";
        String path = booksDir + book.getImagePath();
        File file = new File(path);
        Image image;

        if (file.exists()) {
            image = new Image(file.toURI().toString());
        } else {
            image = new Image(getClass().getResource("/images/book/defaultBook.jpg").toExternalForm());
        }
        bookImage.setImage(image);
    }

    @FXML
    private void closeBorrowConfirmationPane() {
            Stage stage = (Stage) authorNameLabel.getScene().getWindow();
            stage.close();
    }

    @FXML
    private void confirmAction() {
        String query = "INSERT INTO borrows (member_id, book_isbn, borrow_date, due_date, return_date, status, fine, created_at, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, Session.getInstance().getLoggedInUser().getId());
            statement.setString(2, book.getIsbn());
            statement.setString(3, LocalDate.now().toString());
            statement.setString(4, dueDate.toString());
            statement.setString(5, null);
            statement.setString(6, "BORROWING");
            statement.setString(7, String.valueOf(0));
            statement.setString(8, LocalDateTime.now().toString());
            statement.setString(9,  Session.getInstance().getLoggedInUser().getEmail());
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                updateQuantityBook();
            }
            closeBorrowConfirmationPane();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateQuantityBook() {
        String query = "UPDATE books SET quantity_copy = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1,book.getQuantityCopy() - 1);
            statement.setInt(2, book.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
