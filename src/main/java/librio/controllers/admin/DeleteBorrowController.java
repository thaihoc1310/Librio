package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import librio.models.Borrow;
import librio.util.DatabaseUtil;

import java.net.URL;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.getBookByIsbn;
import static librio.util.DatabaseUtil.updateBookAverageRating;

/**
 * Controller class responsible for handling the deletion of borrow records in the system.
 * Implements the Initializable interface and is linked to a JavaFX UI component
 * allowing users to delete a specified borrow record and update relevant book information.
 */
public class DeleteBorrowController implements Initializable {
    private Borrow borrow;

    @FXML
    private Button cancelButton;

    @FXML
    private Button deleteButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    /**
     * Handles the action of cancelling the current operation.
     * Invokes the method to close the current window, effectively
     * dismissing the operation without making changes.
     */
    @FXML
    void cancel() {
        closeWindow();
    }

    /**
     * Handles the deletion of a borrow record and updates the interface accordingly.
     *
     * This method performs the following actions:
     * 1. Deletes the current borrow record from the database.
     * 2. Updates the average rating of the book associated with the borrow record.
     * 3. Closes the window containing the borrow record interface.
     *
     * It assumes that the instance variable `borrow` has been previously initialized
     * and contains a valid borrow record to delete. It also updates the book's average
     * rating immediately after the record's deletion, reflecting any changes caused
     * by the removal of the borrow record.
     */
    @FXML
    void deleteBorrow() {
        DatabaseUtil.deleteBorrow(borrow);
        updateBookAverageRating(getBookByIsbn(borrow.getBookIsbn()).getIsbn());
        closeWindow();
    }

    /**
     * Closes the current window associated with the delete button.
     * This method retrieves the window from the scene of the deleteButton
     * and invokes the close operation on the Stage object,
     * effectively closing the window.
     */
    private void closeWindow() {
        Stage stage = (Stage) deleteButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Sets the current Borrow instance to be used by this controller.
     *
     * @param borrow the Borrow instance that contains details about a book borrowing.
     */
    public void setBorrow(Borrow borrow) {
        this.borrow = borrow;
    }
}
