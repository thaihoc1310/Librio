package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import librio.cache.ImageCache;
import librio.models.Book;
import librio.models.Borrow;
import librio.models.User;
import librio.session.Session;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.getBookByIsbn;
import static librio.util.DatabaseUtil.getUserById;
import static librio.util.DesignUtil.loadDefaultBookImage;

/**
 * The BorrowDetailController is responsible for managing the user interface for displaying
 * the details of a borrowing transaction in a library system. It implements the Initializable
 * interface to perform any necessary setup once its associated FXML file has been loaded.
 *
 * The controller interacts with the UI elements, including labels and buttons, and displays
 * information about a specific Borrow object such as the book's title and ISBN, the member's
 * name and email, and dates related to the borrowing transaction.
 */
public class BorrowDetailController implements Initializable {
    @FXML
    private Label bookIsbnLabel, bookTitleLabel, borrowDateLabel, borrowIdLabel, dueDateLabel,
            fineLabel, memberEmailLabel, returnDateLabel, statusLabel, memberNameLabel;
    @FXML
    private Button backButton;
    @FXML
    private ImageView bookImageView;

    private Borrow borrow;

    /**
     * Initializes the controller class. This method is automatically called after the
     * fxml file has been loaded. This is where any logic related to initialization should be placed.
     *
     * @param location The location used to resolve relative paths for the root object, or null if
     *        the location is not known.
     * @param resources The resources used to localize the root object, or null if resources were not
     *        specified.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    /**
     * Sets the current Borrow object and updates the corresponding fields in the UI.
     *
     * @param borrow the Borrow object containing details of the borrowing instance
     */
    public void setBorrow(Borrow borrow) {
        this.borrow = borrow;
        populateFields();
    }

    /**
     * Populates the UI fields with data from the current borrow instance. This method retrieves
     * information about the borrowed book and the user, displaying details such as the borrow ID,
     * member email, member name, book ISBN, book title, borrow date, due date, return date, status,
     * and any associated fine in the respective labels.
     * <p>
     * If the book has an associated image, it sets that image in the bookImageView; otherwise, it
     * loads a default image.
     * <p>
     * This method is called only if the borrow instance is not null.
     */
    private void populateFields() {
        if (borrow != null) {
            Book borrowedBook = getBookByIsbn(borrow.getBookIsbn());
            User user = getUserById(borrow.getEmail());

            borrowIdLabel.setText(String.valueOf(borrow.getId()));
            memberEmailLabel.setText(user.getEmail());
            memberNameLabel.setText(user.getName());
            bookIsbnLabel.setText(borrow.getBookIsbn());
            bookTitleLabel.setText(borrowedBook.getTitle());
            borrowDateLabel.setText(borrow.getBorrowDate().toString());
            dueDateLabel.setText(borrow.getDueDate().toString());
            if (borrow.getReturnDate() != null) {
                returnDateLabel.setText(borrow.getReturnDate().toString());
            } else {
                returnDateLabel.setText("Not returned yet!");
            }
            statusLabel.setText(borrow.getStatus().toString());
            fineLabel.setText(String.valueOf(borrow.getFine()));

            if (borrowedBook.getImagePath() != null && !borrowedBook.getImagePath().isEmpty()) {
                String projectDir = System.getProperty("user.dir");
                String booksDir = projectDir + "/src/main/resources/images/book/";
                String path = booksDir + borrowedBook.getImagePath();
                Image image = ImageCache.getInstance().getImage(path, booksDir + "defaultBook.jpg");
                bookImageView.setImage(image);
            } else {
                loadDefaultBookImage(bookImageView);
            }
        }
    }

    /**
     * Handles the action of the back button in the UI by closing the current window.
     * This method is typically mapped to a button in the user interface via FXML.
     */
    @FXML
    void back() {
        closeWindow();
    }

    /**
     * Closes the current window associated with the back button event.
     * This method retrieves the Stage object from the back button's current scene
     * and invokes the close operation to terminate the window.
     */
    private void closeWindow() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

}
