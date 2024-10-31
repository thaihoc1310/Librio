package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import librio.controllers.LogoutController;
import librio.controllers.auth.Session;
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

    private User loggedInUser =  Session.getInstance().getLoggedInUser();

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




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(loggedInUser == null) {
            return;
        }
        currentPasswordTextField.setText("");
        newPasswordTextField.setText("");
        confirmPasswordTextField.setText("");
        hideErrorLabels();
        addListeners();
    }

    @FXML
    private void save(){
        hideErrorLabels();
        if (loggedInUser == null) {
            return;
        }

        String currentPassword = currentPasswordTextField != null ? currentPasswordTextField.getText() : "";
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
                clearPasswordFieldAndHideErrorLabels();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addListeners() {
        hideErrorLabels();

        // Ẩn notification khi click vào 1 textField nào đó
        currentPasswordTextField.setOnMouseClicked(event -> notification.setText(""));
        newPasswordTextField.setOnMouseClicked(event -> notification.setText(""));
        confirmPasswordTextField.setOnMouseClicked(event -> notification.setText(""));

        currentPasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!ignoreListener) {
                if(newValue.trim().isEmpty()){
                    currentPasswordErrorLabel.setText("Password must not be empty!");
                }else{
                    currentPasswordErrorLabel.setText("");
                }
            }

        });

        newPasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!ignoreListener) {
                if(newValue.trim().isEmpty()){
                    newPasswordErrorLabel.setText("Password must not be empty!");
                }else{
                    newPasswordErrorLabel.setText("");
                }
            }
        });

        confirmPasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!ignoreListener){
                if(newValue.trim().isEmpty()){
                    confirmPasswordErrorLabel.setText("Password must not be empty!");
                }else{
                    confirmPasswordErrorLabel.setText("");
                }
            }
        });
    }



    private void hideErrorLabels() {
        newPasswordErrorLabel.setText("");
        confirmPasswordErrorLabel.setText("");
        currentPasswordErrorLabel.setText("");
    }


    @FXML
    private void openEditProfileScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/EditProfile.fxml"));
            Parent manageBorrowRoot = loader.load();

            Stage currentStage = (Stage) saveButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBorrowRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearPasswordFieldAndHideErrorLabels() {
        hideErrorLabels();
        ignoreListener = true;

        currentPasswordTextField.clear();
        newPasswordTextField.clear();
        confirmPasswordTextField.clear();

        // Bật lại các listener sau khi đã xóa
        ignoreListener = false;
    }
}
