package librio.controllers.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import librio.controllers.LogoutController;
import librio.controllers.auth.Session;
import librio.database.DatabaseConnection;
import librio.models.Gender;
import librio.models.Role;
import librio.models.User;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.isEmailExists;
import static librio.util.DesignUtil.cropAndClipToCircle;

public class ChangePasswordController implements Initializable {

    private User loggedInUser =  Session.getInstance().getLoggedInUser();

    @FXML
    private ImageView avatar;

    @FXML
    private ImageView avatarUser;

    @FXML
    private Label confirmPasswordErrorLabel;

    @FXML
    private TextField confirmPasswordTextField;

    @FXML
    private Label currentPasswordErrorLabel;

    @FXML
    private TextField currentPastwordTextField;

    @FXML
    private Label newPasswordErrorLabel;

    @FXML
    private TextField newPasswordTextField;

    @FXML
    private Label notification;

    @FXML
    private Button saveButton;

    @FXML
    private Label userNameLabel;




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(loggedInUser == null) {
            return;
        }
        currentPastwordTextField.setText("");
        newPasswordTextField.setText("");
        confirmPasswordTextField.setText("");
        hideErrorLabels();
        addListeners();
        setAvatarAndUserName();
    }

    @FXML
    private void save(){
        if (loggedInUser == null) {
            return;
        }
        String currentPassword = currentPastwordTextField != null ? currentPastwordTextField.getText() : "";
        String newPassword = newPasswordTextField != null ? newPasswordTextField.getText() : "";
        String confirmPassword = confirmPasswordTextField != null ? confirmPasswordTextField.getText() : "";
        boolean validation = false;

        if(currentPassword.isEmpty()){
            currentPasswordErrorLabel.setText("Password must not be empty!");
            validation = true;
        }else if(!currentPassword.equals(loggedInUser.getPassword())){
            currentPasswordErrorLabel.setText("Incorrect password!");
            validation = true;
        }else{
            currentPasswordErrorLabel.setText("");
        }

        if(validation){
            return;
        }

        if(newPassword.isEmpty()){
            newPasswordErrorLabel.setText("Password must not be empty!");
            validation = true;
        }else if(newPassword.equals(currentPassword)){
            newPasswordErrorLabel.setText("Password must not be the same as the previous one!");
            validation = true;
        }else{
            newPasswordErrorLabel.setText("");
        }

        if(validation){
            return;
        }

        if(confirmPassword.isEmpty()){
            confirmPasswordErrorLabel.setText("Password must not be empty!");
            validation = true;
        }else if(!confirmPassword.equals(newPassword)){
            confirmPasswordErrorLabel.setText("Passwords does not match!");
            validation = true;
        }else{
            confirmPasswordErrorLabel.setText("");
        }
        if(validation){
            return;
        }
        loggedInUser.setPassword(newPassword);

        String query = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, newPassword);
            statement.setString(2, loggedInUser.getId());

            int rowsUpdated = statement.executeUpdate();
            if(rowsUpdated > 0){
                notification.setText("Password updated successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addListeners() {
        currentPastwordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.trim().isEmpty()){
                currentPasswordErrorLabel.setText("Password must not be empty!");
            }else{
                currentPasswordErrorLabel.setText("");
            }
        });

        newPasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.trim().isEmpty()){
                newPasswordErrorLabel.setText("Password must not be empty!");
            }else{
                newPasswordErrorLabel.setText("");
            }
        });

        confirmPasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.trim().isEmpty()){
                confirmPasswordErrorLabel.setText("Password must not be empty!");
            }else{
                confirmPasswordErrorLabel.setText("");
            }
        });
    }

    @FXML
    private void openAdDashboardScene() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdDashboard.fxml"));
            Parent adminDashboardRoot  = loader.load();

            Stage currentStage = (Stage) saveButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(adminDashboardRoot);
        }catch(IOException e){
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

            Stage currentStage = (Stage) saveButton.getScene().getWindow();

            LogoutController logoutController = loader.getController();
            logoutController.setOwnerStage(currentStage);
            // Tạo stage mới cho scene
            Stage stage = new Stage();
            stage.setTitle("Logout");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initOwner(currentStage);
            stage.initModality(Modality.WINDOW_MODAL);
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
    }

    public void setAvatarAndUserName(){
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + loggedInUser.getAvatar();

        File file = new File(path);
        if (file.exists()) {
            Image image = new Image(file.toURI().toString());
            cropAndClipToCircle(image, avatarUser, 38.5);
            cropAndClipToCircle(image, avatar, 100);
        } else {
            String defaultImage = avatarsDir + "Male User.png";
            File defaultImageFile = new File(defaultImage);
            Image image = new Image(defaultImageFile.toURI().toString());
            cropAndClipToCircle(image, avatarUser, 38.5);
            cropAndClipToCircle(image, avatar, 100);
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
}
