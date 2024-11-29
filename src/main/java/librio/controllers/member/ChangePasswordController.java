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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addHideErrorListeners();
        currentPasswordTextVisible.setVisible(false);
        newPasswordTextVisible.setVisible(false);
        confirmPasswordTextVisible.setVisible(false);
    }

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

    private void hideErrorLabels() {
        newPasswordErrorLabel.setText("");
        confirmPasswordErrorLabel.setText("");
        currentPasswordErrorLabel.setText("");
        notification.setText("");
    }

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

    private void clearFieldData() {
        currentPasswordTextField.clear();
        currentPasswordTextVisible.clear();
        newPasswordTextField.clear();
        newPasswordTextVisible.clear();
        confirmPasswordTextField.clear();
        confirmPasswordTextVisible.clear();
    }

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

    @FXML
    private void cancel() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }


}
