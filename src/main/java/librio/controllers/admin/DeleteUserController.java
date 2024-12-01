package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import librio.models.User;
import librio.util.DatabaseUtil;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The DeleteUserController class is responsible for handling the deletion
 * of a user within the application's user interface. It manages the associated
 * UI components to facilitate this operation, ensuring proper user interaction
 * and feedback in the event of errors during the deletion process.
 */
public class DeleteUserController implements Initializable {
    @FXML
    private Button deleteButton;
    @FXML
    private Label errorLabel;

    private User user;

    /**
     * Initializes the controller and prepares the UI components.
     *
     * @param url the location used to resolve relative paths for the root object
     * @param resourceBundle the resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        errorLabel.setVisible(false);
    }

    /**
     * Deletes a user from the system. This method first checks whether the user
     * is currently borrowing a book. If so, it will display an error message
     * and abort the deletion process. If the user is not borrowing a book, it
     * will proceed to delete the user from the database and attempt to remove
     * the user's avatar image from the file system, if it exists. The method
     * concludes by closing the current window.
     *
     * Error handling is performed when file deletion fails, which results in
     * a message output to the console but does not stop the execution of the method.
     */
    @FXML
    protected void deleteUser() {
        if (DatabaseUtil.checkIfUserBorrowingBook(user)) {
            errorLabel.setVisible(true);
            return;
        }
        DatabaseUtil.deleteUser(user);
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            File oldFile = new File(avatarsDir + user.getAvatar());
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
     * Cancels the current operation and closes the window.
     * This method is typically used as an event handler for UI components
     * to discard the current changes and exit the current window.
     */
    @FXML
    private void cancel() {
        closeWindow();
    }

    /**
     * Sets the User object for this controller.
     *
     * @param user the User object to be associated with this controller
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Closes the window associated with the delete button in the scene.
     * This method retrieves the current window from the delete button's scene and invokes the close operation on it.
     */
    private void closeWindow() {
        Stage stage = (Stage) deleteButton.getScene().getWindow();
        stage.close();
    }

}
