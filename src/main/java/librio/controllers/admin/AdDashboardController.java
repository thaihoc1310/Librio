package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import librio.controllers.LogoutController;
import librio.controllers.auth.Session;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static librio.util.DesignUtil.cropAndClipToCircle;

public class AdDashboardController implements Initializable {

    @FXML
    Button openManageBookButton;

    @FXML
    private ImageView avatarUser;

    @FXML
    private Label userNameUser;

    @FXML
    private StackPane stackPaneRoot;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAvatarAndUserName();
    }

    public void setAvatarAndUserName() {
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + Session.getInstance().getLoggedInUser().getAvatar();

        File file = new File(path);
        if (file.exists()) {
            Image image = new Image(file.toURI().toString());
            cropAndClipToCircle(image, avatarUser, 38.5);
        } else {
            String defaultImage = avatarsDir + "Male User.png";
            File defaultImageFile = new File(defaultImage);
            Image image = new Image(defaultImageFile.toURI().toString());
            cropAndClipToCircle(image, avatarUser, 38.5);
        }
        userNameUser.setText(Session.getInstance().getLoggedInUser().getName());
    }

    @FXML
    private void openManageBookScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageBook.fxml"));
            Parent manageBookRoot = loader.load();

            Stage currentStage = (Stage) openManageBookButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBookRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openManageUserScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageUser.fxml"));
            Parent manageUserRoot = loader.load();

            Stage currentStage = (Stage) openManageBookButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageUserRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openManageBorrowScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageBorrow.fxml"));
            Parent manageBorrowRoot = loader.load();

            Stage currentStage = (Stage) openManageBookButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBorrowRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openProfileSettingsScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ProfileSettings.fxml"));
            Parent manageBorrowRoot = loader.load();

            Stage currentStage = (Stage) openManageBookButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBorrowRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openLogOutScene() {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Logout.fxml"));
            Parent root = loader.load();
            stackPaneRoot.setOpacity(0.45);
            Stage currentStage = (Stage) openManageBookButton.getScene().getWindow();

            LogoutController logoutController = loader.getController();
            logoutController.setOwnerStage(currentStage);
            logoutController.setStackPaneRoot(stackPaneRoot);
            // Tạo stage mới cho scene
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
            // Hiển thị scene
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
