package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import librio.database.DatabaseConnection;
import librio.models.User;
import librio.session.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controller class responsible for handling the change password functionality.
 * Implements the Initializable interface to perform the necessary initialization for the scene.
 */
public class ChangePasswordController implements Initializable {
    private final User loggedInUser = Session.getInstance().getLoggedInUser();

    @FXML
    private Label confirmPasswordErrorLabel, currentPasswordErrorLabel, newPasswordErrorLabel, notification;
    @FXML
    private PasswordField confirmPasswordTextField, currentPasswordTextField, newPasswordTextField;
    @FXML
    private Button saveButton;
    @FXML
    private TextField currentPasswordTextVisible, newPasswordTextVisible, confirmPasswordTextVisible;
    @FXML
    private ImageView currentPasswordOpenEyeImage, newPasswordOpenEyeImage, confirmPasswordOpenEyeImage,
            currentPasswordCloseEyeImage, newPasswordCloseEyeImage, confirmPasswordCloseEyeImage;

    /**
     * Initializes the change password controller by setting up necessary UI elements
     * and event listeners. This method is automatically called after the FXML file
     * has been loaded.
     *
     * @param url The location used to resolve relative paths for the root object, or
     *            null if the location is not known.
     * @param resourceBundle The resources used to localize the root object, or null if
     *                       the root object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addHideErrorListeners();
        currentPasswordTextVisible.setVisible(false);
        newPasswordTextVisible.setVisible(false);
        confirmPasswordTextVisible.setVisible(false);
    }

    /**
     * Saves the new password for the logged-in user after validating the input fields.
     *
     * <p> This method performs several validation checks to ensure that the current
     * password is correctly entered, the new password is not the same as the
     * current password, and the new password and its confirmation match. If any
     * of these validations fail, corresponding error messages are displayed.
     *
     * <p> Upon successful validation, the method updates the user's password in
     * the database and sets a notification to inform the user of a successful
     * update. It then clears all password input fields.
     *
     * <p> If a SQLException occurs during the database update process, the exception
     * stack trace is printed to the standard error stream.
     */
    @FXML
    private void save() {
        String currentPassword = currentPasswordTextField != null ? currentPasswordTextField.getText() : "";
        String newPassword = newPasswordTextField != null ? newPasswordTextField.getText() : "";
        String confirmPassword = confirmPasswordTextField != null ? confirmPasswordTextField.getText() : "";
        boolean validation = false;

        if (currentPassword.isEmpty()) {
            currentPasswordErrorLabel.setText("Password must not be empty!");
            validation = true;
        } else if (!currentPassword.equals(loggedInUser.getPassword())) {
            currentPasswordErrorLabel.setText("Incorrect password!");
            validation = true;
        } else {
            currentPasswordErrorLabel.setText("");
        }

        if (validation) {
            return;
        }

        if (newPassword.isEmpty()) {
            newPasswordErrorLabel.setText("Password must not be empty!");
            validation = true;
        } else if (newPassword.equals(currentPassword)) {
            newPasswordErrorLabel.setText("Password must not be the same as the previous one!");
            validation = true;
        } else {
            newPasswordErrorLabel.setText("");
        }

        if (validation) {
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordErrorLabel.setText("Password must not be empty!");
            validation = true;
        } else if (!confirmPassword.equals(newPassword)) {
            confirmPasswordErrorLabel.setText("Passwords does not match!");
            validation = true;
        } else {
            confirmPasswordErrorLabel.setText("");
        }
        if (validation) {
            return;
        }
        loggedInUser.setPassword(newPassword);

        String query = "UPDATE users SET password = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, newPassword);
            statement.setString(2, loggedInUser.getEmail());
            statement.setString(3, loggedInUser.getId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                notification.setText("Password updated successfully!");
                clearFieldData();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears the text of all error labels related to password input fields.
     * This method resets the error messages for new password, confirm password,
     * and current password to empty strings. It also clears any notification
     * messages, ensuring the user interface displays a clean state with no
     * error messages.
     */
    private void hideErrorLabels() {
        newPasswordErrorLabel.setText("");
        confirmPasswordErrorLabel.setText("");
        currentPasswordErrorLabel.setText("");
        notification.setText("");
    }

    /**
     * Adds mouse click listeners to password text fields and their visible text representations.
     * The listeners are designed to clear error labels on user interaction.
     * When any of the specified text fields or their visible counterparts are clicked,
     * the method `hideErrorLabels` is invoked to reset error messages related to password input,
     * ensuring that the user is presented with a clean state for subsequent input attempts.
     */
    private void addHideErrorListeners() {
        currentPasswordTextField.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        currentPasswordTextVisible.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        newPasswordTextField.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        newPasswordTextVisible.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        confirmPasswordTextField.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        confirmPasswordTextVisible.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
    }

    /**
     * Clears all password input fields and their visible counterparts in the user interface.
     * This method resets the text in each of the six fields associated with current,
     * new, and confirm password inputs, ensuring they are empty for future password changes.
     */
    private void clearFieldData() {
        currentPasswordTextField.clear();
        currentPasswordTextVisible.clear();
        newPasswordTextField.clear();
        newPasswordTextVisible.clear();
        confirmPasswordTextField.clear();
        confirmPasswordTextVisible.clear();
    }

    /**
     * Opens the edit profile scene by loading the AccountSetting.fxml file and
     * setting it as the root of the current scene. This method changes the visible
     * content of the current stage to the account settings interface, allowing the
     * user to edit their profile. If an IOException occurs during the loading of the
     * FXML file, it will be printed to the console.
     */
    @FXML
    private void openEditProfileScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/AccountSetting.fxml"));
            Parent manageBorrowRoot = loader.load();

            Stage currentStage = (Stage) saveButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBorrowRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays the current password as plain text in the user interface. This method
     * reveals the current password by transferring its value from a password field
     * to a plain text field, making the password visible to the user. It updates the
     * visibility of the password and the eye icons to indicate the password's current
     * status (visible). The method also sets the focus to the visible text field and
     * positions the caret at the end of the text to facilitate easy viewing or further
     * editing of the password.
     */
    @FXML
    private void showCurrentPassword() {
        currentPasswordTextVisible.setText(currentPasswordTextField.getText());
        currentPasswordTextVisible.setVisible(true);
        currentPasswordTextField.setVisible(false);
        currentPasswordOpenEyeImage.setVisible(false);
        currentPasswordCloseEyeImage.setVisible(true);
        currentPasswordTextVisible.requestFocus();
        currentPasswordTextVisible.positionCaret(currentPasswordTextVisible.getText().length());
    }

    /**
     * Hides the current password text by transferring its content to a password field.
     * This method ensures that the password is not visible as plain text to enhance security.
     * It makes the password field visible and hides the plain text label. Additionally,
     * it updates the visibility of the eye icons to reflect the hidden status of the password,
     * refocuses the password field, and positions the caret at the end of the text for
     * continued editing or viewing.
     */
    @FXML
    private void hideCurrentPassword() {
        currentPasswordTextField.setText(currentPasswordTextVisible.getText());
        currentPasswordTextField.setVisible(true);
        currentPasswordTextVisible.setVisible(false);
        currentPasswordOpenEyeImage.setVisible(true);
        currentPasswordCloseEyeImage.setVisible(false);
        currentPasswordTextField.requestFocus();
        currentPasswordTextField.positionCaret(currentPasswordTextVisible.getText().length());
    }

    /**
     * Sets the text of the new password to be visible in plain text form and updates
     * the visibility of relevant UI components accordingly. This method makes the
     * currently entered password visible for inspection by transferring it from a
     * password field to a text field. It also updates the visibility of eye icons to
     * provide visual indication of the password's display status. The focus is shifted
     * to the visible text component and the text caret is positioned at the end for ease
     * of editing.
     */
    @FXML
    private void showNewPassword() {
        newPasswordTextVisible.setText(newPasswordTextField.getText());
        newPasswordTextVisible.setVisible(true);
        newPasswordTextField.setVisible(false);
        newPasswordOpenEyeImage.setVisible(false);
        newPasswordCloseEyeImage.setVisible(true);
        newPasswordTextVisible.requestFocus();
        newPasswordTextVisible.positionCaret(newPasswordTextVisible.getText().length());
    }

    /**
     * Hides the visible new password text by transferring its content
     * to a password field. This method makes the password field visible
     * and the plain text field invisible for better security.
     * Additionally, it updates the visibility of eye icons to indicate
     * the password's hidden status and refocuses the password field,
     * placing the caret at the end of the text.
     */
    @FXML
    private void hideNewPassword() {
        newPasswordTextField.setText(newPasswordTextVisible.getText());
        newPasswordTextField.setVisible(true);
        newPasswordTextVisible.setVisible(false);
        newPasswordOpenEyeImage.setVisible(true);
        newPasswordCloseEyeImage.setVisible(false);
        newPasswordTextField.requestFocus();
        newPasswordTextField.positionCaret(newPasswordTextVisible.getText().length());
    }

    /**
     * Displays the confirm password as plain text instead of a password field.
     * This method transfers the current content of the confirm password field to a
     * visible text component, making it visible for the user. It also updates the visibility
     * of the associated eye icons to visually indicate the password's display status.
     * The focus is set to the visible text component, and the caret is positioned at
     * the end of the text to facilitate text editing.
     */
    @FXML
    private void showConfirmPassword() {
        confirmPasswordTextVisible.setText(confirmPasswordTextField.getText());
        confirmPasswordTextVisible.setVisible(true);
        confirmPasswordTextField.setVisible(false);
        confirmPasswordOpenEyeImage.setVisible(false);
        confirmPasswordCloseEyeImage.setVisible(true);
        confirmPasswordTextVisible.requestFocus();
        confirmPasswordTextVisible.positionCaret(confirmPasswordTextVisible.getText().length());
    }

    /**
     * Hides the confirm password text, showing the password field in its place.
     * The method transfers the content from a visible text component to a password field,
     * making the latter visible for secure user interactions. It also updates the
     * visibility of eye icons to indicate the password display status, focusing the
     * password field and positioning the caret at the end of the text.
     */
    @FXML
    private void hideConfirmPassword() {
        confirmPasswordTextField.setText(confirmPasswordTextVisible.getText());
        confirmPasswordTextField.setVisible(true);
        confirmPasswordTextVisible.setVisible(false);
        confirmPasswordOpenEyeImage.setVisible(true);
        confirmPasswordCloseEyeImage.setVisible(false);
        confirmPasswordTextField.requestFocus();
        confirmPasswordTextField.positionCaret(confirmPasswordTextVisible.getText().length());
    }

    /**
     * Closes the current stage window associated with the save button.
     * This method triggers when the cancel operation is invoked, effectively
     * dismissing the current UI scene.
     */
    @FXML
    private void cancel() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
