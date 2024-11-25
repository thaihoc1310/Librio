package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
<<<<<<< HEAD
import librio.session.Session;
=======
import librio.auth.Session;
import librio.cache.ImageCache;
>>>>>>> a51587d5162db85c9387a7d90f67b5da45fb9183
import librio.database.DatabaseConnection;
import librio.models.Book;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

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
    private Text titleText;

    private Book book;

    private LocalDate dueDate;

    public void setBook (Book book) {
        this.book = book;

        titleText.setText(book.getTitle());
        authorNameLabel.setText(book.getAuthor());
        dueDate = LocalDate.now().plusDays(90);
        dueDateLabel.setText("Due Date:  " + dueDate);

        String projectDir = System.getProperty("user.dir");
        String booksDir = projectDir + "/src/main/resources/images/book/";
        String path = booksDir + book.getImagePath();

        Image image = ImageCache.getInstance().getImage(path, booksDir + "defaultBook.jpg");
        bookImage.setImage(image);
    }

    @FXML
    private void closeBorrowConfirmationPane() {
            Stage stage = (Stage) authorNameLabel.getScene().getWindow();
            stage.close();
    }

    @FXML
    private void confirmAction() {
        String query = "INSERT INTO borrows (member_id, book_isbn, borrow_date, due_date, return_date, status, fine, created_at, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, Session.getInstance().getLoggedInUser().getId());
            statement.setString(2, book.getIsbn());
            statement.setString(3, LocalDate.now().toString());
            statement.setString(4, dueDate.toString());
            statement.setString(5, null);
            statement.setString(6, "BORROWING");
            statement.setString(7, String.valueOf(0));
            statement.setString(8,  Session.getInstance().getLoggedInUser().getEmail());
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
        String query = "UPDATE books SET available_copy = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1,book.getAvailableCopy() - 1);
            statement.setInt(2, book.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
