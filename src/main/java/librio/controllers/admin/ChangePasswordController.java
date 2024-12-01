package librio.controllers.admin;

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
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import librio.controllers.auth.LogoutController;
import librio.database.DatabaseConnection;
import librio.models.User;
import librio.session.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static librio.util.DesignUtil.setAvatarAndUserName;
import static librio.util.DesignUtil.switchScene;

/**
 * The ChangePasswordController class manages the user interface and logic for changing
 * the user's password within the application. It handles password validation, updates
 * the backend database, and provides feedback to the user through various UI components.
 *
 * Fields in this class represent various UI components such as text fields, buttons,
 * and labels used to capture and display password-related information and notifications
 * to the user.
 *
 * The methods within this class facilitate the password change workflow, including
 * user input validation, displaying passwords in a visible format, transitioning scenes
 * in the application, and handling user interactions through event listeners.
 *
 * The ChangePasswordController uses the JavaFX framework, leveraging FXML to define
 * its UI layout and allowing for interaction through annotated methods that respond
 * to user actions in the application.
 */
public class ChangePasswordController implements Initializable {

    private final User loggedInUser = Session.getInstance().getLoggedInUser();

    @FXML
    private ImageView avatarUser, currentPasswordOpenEyeImage, newPasswordOpenEyeImage, confirmPasswordOpenEyeImage,
            currentPasswordCloseEyeImage, newPasswordCloseEyeImage, confirmPasswordCloseEyeImage;
    @FXML
    private Label confirmPasswordErrorLabel, currentPasswordErrorLabel, newPasswordErrorLabel, notification, userNameLabel;
    @FXML
    private Button saveButton;
    @FXML
    private TextField currentPasswordTextVisible, newPasswordTextVisible, confirmPasswordTextVisible;
    @FXML
    private PasswordField currentPasswordTextField, newPasswordTextField, confirmPasswordTextField;
    @FXML
    private StackPane stackPaneRoot;

    /**
     * Initializes the ChangePasswordController by setting up the necessary UI components.
     * This method is called automatically after the FXML file has been loaded.
     *
     * @param url The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resourceBundle The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initPassword();
        addListeners();
        hideErrorLabels();
        setAvatarAndUserName(avatarUser, userNameLabel);
    }

    /**
     * Handles the password change process for the logged-in user. This method performs several
     * validation checks, including verifying the current password, ensuring the new password
     * meets security standards, and updating the backend database with the new password.
     *
     * If the logged-in user is null or any validation check fails, the method will exit early
     * without making any changes. Error messages are displayed through corresponding error labels
     * to guide the user on any input issues.
     *
     * It begins by hiding existing error messages, retrieves user input for passwords (current,
     * new, and confirmation), and validates them against various criteria:
     *
     * - Ensures the current password matches the user's current password.
     * - Verifies the new password follows rules: non-empty, different from the current password,
     *   and meets a minimum length requirement.
     * - Confirms the confirmation password matches the new password.
     *
     * If all validations pass, it updates the user's password in the database.
     * Upon successful update, it provides user feedback via a notification label
     * and clears the input fields. In the event of a SQL exception, the stack trace is printed.
     */
    @FXML
    private void save() {
        hideErrorLabels();
        if (loggedInUser == null) {
            return;
        }
        String currentPassword = currentPasswordTextField.isVisible() ? currentPasswordTextField.getText() : currentPasswordTextVisible.getText();
        String newPassword = newPasswordTextField.isVisible() ? newPasswordTextField.getText() : newPasswordTextVisible.getText();
        String confirmPassword = confirmPasswordTextField.isVisible() ? confirmPasswordTextField.getText() : confirmPasswordTextVisible.getText();
        boolean validation = false;

        if (currentPassword.isEmpty()) {
            currentPasswordErrorLabel.setText("Password must not be empty!");
            validation = true;
        } else if (!currentPassword.equals(loggedInUser.getPassword())) {
            currentPasswordErrorLabel.setText("Incorrect password!");
            validation = true;
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
        } else if (newPassword.length() < 6) {
            newPasswordErrorLabel.setText("Password must be at least 6 characters!");
            validation = true;
        }

        if (validation) {
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordErrorLabel.setText("Password must not be empty!");
            validation = true;
        } else if (!confirmPassword.equals(newPassword)) {
            confirmPasswordErrorLabel.setText("Passwords do not match!");
            validation = true;
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
                clearPasswordFieldAndHideErrorLabels();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds mouse click event listeners to password fields and visible password indicators
     * to trigger the hiding of error labels. The event listeners are associated with
     * the current password, new password, and confirm password text fields as well as
     * their respective visible indicators. When any of these components are clicked,
     * it invokes the {@code hideErrorLabels} method to clear any error messages.
     */
    private void addListeners() {
        currentPasswordTextField.setOnMouseClicked(event -> hideErrorLabels());
        currentPasswordTextVisible.setOnMouseClicked(event -> hideErrorLabels());
        newPasswordTextField.setOnMouseClicked(event -> hideErrorLabels());
        newPasswordTextVisible.setOnMouseClicked(event -> hideErrorLabels());
        confirmPasswordTextField.setOnMouseClicked(event -> hideErrorLabels());
        confirmPasswordTextVisible.setOnMouseClicked(event -> hideErrorLabels());
    }

    /**
     * Opens the advertisement dashboard scene by switching from the current scene
     * to the one defined in the FXML file located at "/fxml/admin/AdDashboard.fxml".
     * This method is triggered through the FXML framework.
     */
    @FXML
    private void openAdDashboardScene() {
        switchScene(saveButton, "/fxml/admin/AdDashboard.fxml");
    }

    /**
     * Opens the Manage Borrow scene by switching the current scene to the specified FXML layout.
     * This method is triggered in response to a user action, such as clicking a button,
     * and updates the view to display the Manage Borrow interface.
     *
     * The scene switch operation is performed by the switchScene method, which takes a reference
     * to a button and the path to the FXML file corresponding to the Manage Borrow interface.
     * This method assumes that the button triggering the scene change is named 'saveButton'.
     *
     * In case of an error (such as failing to load the FXML file), the switchScene method
     * encapsulates error handling, which involves printing the stack trace of any
     * IOException encountered during the process.
     */
    @FXML
    private void openManageBorrowScene() {
        switchScene(saveButton, "/fxml/admin/ManageBorrow.fxml");
    }

    /**
     * Opens the Manage Book scene by switching the current scene to the ManageBook.fxml layout.
     * This method utilizes the switchScene helper method to load and display the new scene.
     * The switch is triggered by the saveButton UI component.
     */
    @FXML
    private void openManageBookScene() {
        switchScene(saveButton, "/fxml/admin/ManageBook.fxml");
    }

    /**
     * Opens the Manage User scene within the application.
     * This method switches the current scene to the ManageUser.fxml layout,
     * allowing the user to manage user-related functionalities.
     * It utilizes the switchScene method to perform the scene transition.
     */
    @FXML
    private void openManageUserScene() {
        switchScene(saveButton, "/fxml/admin/ManageUser.fxml");
    }

    /**
     * Opens the logout scene as a modal dialog. This method loads the Logout.fxml
     * file, configures the scene and stage properties, and displays it as a modal
     * window over the current application window. The opacity of the current window
     * is reduced to emphasize the modal dialog. On closing the dialog, the method
     * ensures that the application window regains its original opacity.
     *
     * The logout scene is styled to be non-resizable and undecorated.
     * It is centered over the current application window when displayed.
     *
     * This method will display any caught IO exceptions related to loading the
     * FXML file to the standard error stream.
     */
    @FXML
    private void openLogOutScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Logout.fxml"));
            Parent root = loader.load();
            stackPaneRoot.setOpacity(0.45);
            Stage currentStage = (Stage) saveButton.getScene().getWindow();

            LogoutController logoutController = loader.getController();
            logoutController.setOwnerStage(currentStage);
            logoutController.setStackPaneRoot(stackPaneRoot);
            Stage stage = new Stage();
            stage.setTitle("Logout");
            stage.setScene(new Scene(root));
            Rectangle clip = new Rectangle();
            clip.setWidth(424);
            clip.setHeight(204);
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            root.setClip(clip);
            stage.setResizable(false);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initOwner(currentStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnShown(event -> {
                stage.setX(currentStage.getX() + (currentStage.getWidth() - stage.getWidth()) / 2);
                stage.setY(currentStage.getY() + (currentStage.getHeight() - stage.getHeight()) / 2);
            });

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears the text of error labels and notification in the interface.
     * This method sets the error messages for the new, confirm, and current password fields,
     * as well as the notification text, to empty strings, effectively hiding any visible error messages.
     */
    private void hideErrorLabels() {
        newPasswordErrorLabel.setText("");
        confirmPasswordErrorLabel.setText("");
        currentPasswordErrorLabel.setText("");
        notification.setText("");
    }

    /**
     * Opens the Personal Information scene within the application.
     * This method is invoked through a user interaction event and will
     * transition the current scene to the personal information view,
     * specified by the given FXML path.
     *
     * The transition is triggered using the 'saveButton' control as
     * a reference to fetch the current stage and scene for the switch.
     */
    @FXML
    private void openPersonalInformationScene() {
        switchScene(saveButton, "/fxml/admin/ProfileSettings.fxml");
    }

    /**
     * Clears all the password input fields and associated visible fields.
     * This method is typically used to reset the password inputs to their
     * default state after a password change operation, ensuring that no
     * residual data is left in the input fields.
     */
    private void clearPasswordFieldAndHideErrorLabels() {
        currentPasswordTextField.clear();
        currentPasswordTextVisible.clear();
        newPasswordTextField.clear();
        newPasswordTextVisible.clear();
        confirmPasswordTextField.clear();
        confirmPasswordTextVisible.clear();
    }

    /**
     * Displays the current password in plain text within the view.
     * This method makes the current password visible to the user by updating the UI
     * components appropriately. It transfers the text from the password field
     * to a text view, hides certain UI elements, and updates the focus.
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
     * Handles the action of hiding the current password in the UI.
     * This method transfers the text from the visible password field to the hidden one,
     * makes the hidden field visible, and the visible field hidden.
     * It also manages the visibility of UI elements associated with showing and hiding the password,
     * such as eye images.
     * Finally, it sets focus on the password text field and positions the caret at the end of the text.
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
     * Handles the visibility toggle of the new password field in the UI.
     * Displays the password in a non-obscured format for user clarity and hides the
     * obscured password field. Additionally, it updates the visibility of the eye
     * icons to reflect this change.
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
     * Hides the visible text representation of the new password and reverts back to a masked format.
     * This method transfers the content from the new password's visible text field
     * to the password field and updates the UI accordingly:
     * - The password text field is made visible and focused.
     * - The visible text field is hidden.
     * - The open eye icon is shown to indicate the masked state.
     * - The closed eye icon is hidden.
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
     * Reveals the content of the confirm password text field in a visible text format.
     * This method updates the UI to show the plain text of the confirm password,
     * hides the password field with hidden characters, and manages the visibility
     * of the eye icons used to toggle password visibility.
     *
     * Specifically, this method:
     * - Copies the text from the confirmPasswordTextField to confirmPasswordTextVisible.
     * - Shows confirmPasswordTextVisible and hides confirmPasswordTextField.
     * - Displays the closed eye icon and hides the open eye icon.
     * - Sets focus on confirmPasswordTextVisible and places the caret at the end of the text.
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
     * Hides the visible text representation of the confirm password field and
     * displays the masked password field instead. This method updates the text
     * of the masked password field to match the visible text, makes the masked
     * password field visible, hides the visible text representation, shows the
     * 'open eye' icon to indicate that the password is hidden, and hides the
     * 'close eye' icon. It also sets the focus to the masked password field and
     * positions the caret at the end of the text.
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
     * Initializes the password fields by clearing their content and hiding
     * associated visibility indicators. This method is typically used to reset
     * the password input fields in the UI when the password change process begins
     * or after a password change attempt.
     */
    private void initPassword() {
        currentPasswordTextField.setText("");
        newPasswordTextField.setText("");
        confirmPasswordTextField.setText("");
        currentPasswordTextVisible.setVisible(false);
        newPasswordTextVisible.setVisible(false);
        confirmPasswordTextVisible.setVisible(false);
    }
}
