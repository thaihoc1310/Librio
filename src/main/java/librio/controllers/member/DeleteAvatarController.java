package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import librio.database.DatabaseConnection;
import librio.models.User;
import librio.session.Session;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static librio.util.DesignUtil.cropAndClipToCircle;

/**
 * Controller responsible for handling the deletion of a user's avatar in the application.
 *
 * This controller provides functionality to:
 * - Set the avatar image for display purposes.
 * - Cancel the avatar deletion operation and close the current window.
 * - Delete the current avatar associated with the logged-in user, reverting to a default avatar image.
 *
 * The actions performed by this controller are primarily triggered through
 * user interface interactions, such as button clicks which are wired using JavaFX.
 */
public class DeleteAvatarController {
    private final User loggedInUser = Session.getInstance().getLoggedInUser();

    @FXML
    private Button cancelButton;
    @FXML
    private Button deleteButton;

    private ImageView avatar;

    /**
     * Sets the ImageView object representing the avatar for this controller.
     *
     * @param avatar the ImageView object to be assigned as the avatar
     */
    public void setAvatar(ImageView avatar) {
        this.avatar = avatar;
    }

    /**
     * Closes the current window associated with the cancel button.
     * This action is typically used to dismiss the dialog or stage
     * without performing any additional operations.
     */
    @FXML
    private void cancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Deletes the current avatar of the logged-in user and restores a default avatar.
     *
     * This method performs the following actions:
     * <ul>
     * <li>Checks if the logged-in user has an existing avatar.</li>
     * <li>If an avatar exists, it attempts to delete the corresponding file from
     * the local file system.</li>
     * <li>Replaces the user's avatar with a default image and clips it into a circular shape.</li>
     * <li>Updates the user's avatar information in the database to nullify it.</li>
     * <li>Invokes the cancel method to presumably close the current UI window.</li>
     * </ul>
     *
     * Any exceptions encountered during the database update process are caught and
     * printed to the standard error output.
     */
    @FXML
    private void delete() {
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";

        if (loggedInUser.getAvatar() != null && !loggedInUser.getAvatar().isEmpty()) {
            File oldAvatarFile = new File(avatarsDir + loggedInUser.getAvatar());
            if (oldAvatarFile.exists() && !oldAvatarFile.delete()) {
                System.out.println("Không thể xóa tệp ảnh cũ: " + oldAvatarFile.getAbsolutePath());
            }
        }
        String defaultImage = avatarsDir + "Male User.png";
        File defaultImageFile = new File(defaultImage);
        Image defaultAvatar = new Image(defaultImageFile.toURI().toString());
        cropAndClipToCircle(defaultAvatar, avatar, 50);
        loggedInUser.setAvatar(null);

        String query = "UPDATE users SET avatar = NULL WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, loggedInUser.getId());
            statement.executeUpdate();
            cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
