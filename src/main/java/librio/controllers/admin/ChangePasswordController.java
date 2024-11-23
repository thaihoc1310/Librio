package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import librio.controllers.auth.LogoutController;
import librio.session.Session;
import librio.database.DatabaseConnection;
import librio.models.User;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static librio.util.DesignUtil.cropAndClipToCircle;

public class ChangePasswordController implements Initializable {

    private boolean ignoreListener = false;

    private User loggedInUser = Session.getInstance().getLoggedInUser();

    @FXML
    private ImageView avatarUser;

    @FXML
    private Label confirmPasswordErrorLabel;

    @FXML
    private PasswordField confirmPasswordTextField;

    @FXML
    private Label currentPasswordErrorLabel;

    @FXML
    private PasswordField currentPasswordTextField;

    @FXML
    private Label newPasswordErrorLabel;

    @FXML
    private PasswordField newPasswordTextField;

    @FXML
    private Label notification;

    @FXML
    private Button saveButton;

    @FXML
    private Label userNameLabel;

    @FXML
    private TextField currentPasswordTextVisible;

    @FXML
    private TextField newPasswordTextVisible;

    @FXML
    private TextField confirmPasswordTextVisible;

    @FXML
    private ImageView currentPasswordOpenEyeImage;

    @FXML
    private ImageView newPasswordOpenEyeImage;

    @FXML
    private ImageView confirmPasswordOpenEyeImage;

    @FXML
    private ImageView currentPasswordCloseEyeImage;

    @FXML
    private ImageView newPasswordCloseEyeImage;

    @FXML
    private ImageView confirmPasswordCloseEyeImage;

    @FXML
    private StackPane stackPaneRoot;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (loggedInUser == null) {
            return;
        }
        currentPasswordTextField.setText("");
        newPasswordTextField.setText("");
        confirmPasswordTextField.setText("");
        currentPasswordTextVisible.setVisible(false);
        newPasswordTextVisible.setVisible(false);
        confirmPasswordTextVisible.setVisible(false);
        addListeners();
        hideErrorLabels();
        setAvatarAndUserName();
    }

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

    private void addListeners() {
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

    @FXML
    private void openAdDashboardScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdDashboard.fxml"));
            Parent adminDashboardRoot = loader.load();

            Stage currentStage = (Stage) saveButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(adminDashboardRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openManageBorrowScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageBorrow.fxml"));
            Parent manageBorrowRoot = loader.load();

            Stage currentStage = (Stage) saveButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBorrowRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openManageBookScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageBook.fxml"));
            Parent manageBookRoot = loader.load();

            Stage currentStage = (Stage) saveButton.getScene().getWindow();
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
            Parent manageBookRoot = loader.load();

            Stage currentStage = (Stage) saveButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBookRoot);
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
            Stage currentStage = (Stage) saveButton.getScene().getWindow();

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


    private void hideErrorLabels() {
        newPasswordErrorLabel.setText("");
        confirmPasswordErrorLabel.setText("");
        currentPasswordErrorLabel.setText("");
        notification.setText("");
    }

    public void setAvatarAndUserName() {
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + loggedInUser.getAvatar();

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
        userNameLabel.setText(loggedInUser.getName());
    }

    @FXML
    private void openPersonalInformationScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ProfileSettings.fxml"));
            Parent manageBorrowRoot = loader.load();

            Stage currentStage = (Stage) saveButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBorrowRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearPasswordFieldAndHideErrorLabels() {

        currentPasswordTextField.clear();
        currentPasswordTextVisible.clear();
        newPasswordTextField.clear();
        newPasswordTextVisible.clear();
        confirmPasswordTextField.clear();
        confirmPasswordTextVisible.clear();
    }

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
}
