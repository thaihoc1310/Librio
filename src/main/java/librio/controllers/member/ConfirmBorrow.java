package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import librio.cache.ImageCache;
import librio.database.DatabaseConnection;
import librio.models.Book;
import librio.session.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * The ConfirmBorrow class is responsible for managing the borrowing process
 * of a book. It handles the user interface updates relevant to the book's
 * details and manages borrowing records in the database. This class is
 * equipped to interact with the graphical user interface components and
 * update them with details regarding the author, title, due date, and book
 * image. It also facilitates the confirmation of a borrow action by updating
 * records in the database and adjusting the available quantities of books.
 */
public class ConfirmBorrow {
    @FXML
    private Label authorNameLabel;
    @FXML
    private ImageView bookImage;
    @FXML
    private Text dueDateLabel;
    @FXML
    private Text titleText;

    private Book book;

    private LocalDate dueDate;

    /**
     * Sets the specified book for borrowing. Updates the user interface
     * components with the book's relevant details such as title, author,
     * due date, and image.
     *
     * @param book the book to be set for borrowing, containing necessary details
     *             such as title, author, image path, and other attributes required
     *             for updating the user interface and system records.
     */
    public void setBook(Book book) {
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

    /**
     * Closes the current stage associated with the authorNameLabel.
     * This method retrieves the stage from the scene of the authorNameLabel
     * and invokes the close operation on it. The method is designed to be
     * triggered by an event, such as a button click, to terminate the current
     * window when the user confirms an action or wishes to exit.
     */
    @FXML
    private void closeStage() {
        Stage stage = (Stage) authorNameLabel.getScene().getWindow();
        stage.close();
    }

    /**
     * Handles the confirmation action for borrowing a book. This method inserts a new
     * borrowing record into the database, including details such as member ID, book ISBN,
     * borrow date, due date, return date, status, fine, and the user who created the record.
     * Upon successful insertion, it updates the book's quantity and closes the current stage.
     * In case of an SQL exception, the stack trace is printed for debugging purposes.
     */
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
            statement.setString(8, Session.getInstance().getLoggedInUser().getEmail());
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                updateQuantityBook();
            }
            closeStage();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the quantity of available copies of a book in the database.
     * This method decreases the available copy count of the specified book by one.
     * The update is performed on the 'books' table using the book's ID as a reference.
     * It establishes a connection to the database, prepares an SQL update statement,
     * and executes it. If an SQL exception occurs during this process, the exception
     * is caught and its stack trace is printed.
     */
    private void updateQuantityBook() {
        String query = "UPDATE books SET available_copy = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, book.getAvailableCopy() - 1);
            statement.setInt(2, book.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
