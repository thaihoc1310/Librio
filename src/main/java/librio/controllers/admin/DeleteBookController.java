package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import librio.models.Book;
import librio.util.DatabaseUtil;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


/**
 * Controller class responsible for handling the deletion of a book in the application.
 * This class manages the user interface elements and actions related to deleting a book,
 * including verifying whether the book can be deleted, updating the database, and handling
 * any associated image files.
 */
public class DeleteBookController implements Initializable {
    @FXML
    private Button deleteButton;
    @FXML
    private Label errorLabel;

    private Book book;

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded. It sets the initial state of the
     * controller, particularly for setting the visibility of the error label.
     *
     * @param url The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resourceBundle The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        errorLabel.setVisible(false);
    }

    /**
     * Deletes the currently set book from the database and associated image file if present.
     * This method first checks if the book is currently borrowed, and if so, displays an error
     * message without proceeding. If the book is not borrowed, it deletes the book's record from
     * the database. Additionally, if the book has an associated image file that is not the default
     * image, it attempts to delete this file from the local filesystem. After processing, it
     * closes the current window.
     *
     * Preconditions:
     * - The 'book' field must be initialized before invoking this method.
     * - The method assumes the project's images are located in the specified directory path.
     *
     * Postconditions:
     * - The book is deleted from the database if not currently borrowed.
     * - The book's associated image file is deleted if it is not the default image and exists.
     * - The window is closed after execution.
     *
     * Side effects:
     * - Displays an error message on the UI if the book is borrowed.
     * - Writes a message to the standard output if the image file cannot be deleted.
     * - Closes the application window.
     */
    @FXML
    protected void deleteBook() {
        if (DatabaseUtil.checkIfBookIsBorrowed(book)) {
            errorLabel.setVisible(true);
            return;
        }
        DatabaseUtil.deleteBook(book);
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/book/";
        if (book.getImagePath() != null && !book.getImagePath().isEmpty() ) {
            File oldFile = new File(avatarsDir + book.getImagePath());
            if (oldFile.exists()) {
                boolean deleted = oldFile.delete();
                if (!deleted) {
                    System.out.println("Không thể xóa tệp ảnh cũ: " + oldFile.getAbsolutePath());
                }
            }
        }
        closeWindow();
    }

    /**
     * Handles the action to cancel the current operation and close the window.
     *
     * This method is typically invoked when the user selects a cancel option
     * in the user interface to dismiss the current dialog or window without
     * making changes.
     *
     * It uses the {@code closeWindow} method to close the window.
     */
    @FXML
    private void cancel() {
        closeWindow();
    }

    /**
     * Sets the book to be managed by this controller.
     *
     * @param book the book to be managed
     */
    public void setBook(Book book) {
        this.book = book;
    }

    /**
     * Closes the window associated with the delete button's scene.
     * This method retrieves the current stage from the deleteButton's scene
     * and invokes the close operation on the stage.
     */
    private void closeWindow() {
        Stage stage = (Stage) deleteButton.getScene().getWindow();
        stage.close();
    }

}
