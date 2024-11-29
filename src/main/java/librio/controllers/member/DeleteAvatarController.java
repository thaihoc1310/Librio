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

public class DeleteAvatarController {
    private final User loggedInUser = Session.getInstance().getLoggedInUser();

    @FXML
    private Button cancelButton;
    @FXML
    private Button deleteButton;

    private ImageView avatar;

    public void setAvatar(ImageView avatar) {
        this.avatar = avatar;
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

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
