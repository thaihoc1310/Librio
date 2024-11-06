package librio.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import librio.auth.Session;
import librio.models.Role;
import librio.models.User;

import java.io.IOException;

import static librio.util.DatabaseUtil.authenticate;

public class LoginController {
    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameField;

    @FXML
    private Label incorrectLoginInformation;

    @FXML
    private TextField passwordTextVisible;

    @FXML
    private ImageView openEyeImage;

    @FXML
    private ImageView closeEyeImage;

    @FXML
    private void initialize() {
        passwordTextVisible.setVisible(false);
        usernameField.setOnMouseClicked(event -> clearErrorMessage());
        passwordField.setOnMouseClicked(event -> clearErrorMessage());
    }

    @FXML
    private void showPassword() {
        passwordTextVisible.setText(passwordField.getText());
        passwordTextVisible.setVisible(true);
        passwordField.setVisible(false);
        openEyeImage.setVisible(false);
        closeEyeImage.setVisible(true);
        passwordTextVisible.requestFocus();
        passwordTextVisible.positionCaret(passwordField.getText().length());

    }

    @FXML
    private void hidePassword() {
        passwordField.setText(passwordTextVisible.getText());
        passwordField.setVisible(true);
        passwordTextVisible.setVisible(false);
        openEyeImage.setVisible(true);
        closeEyeImage.setVisible(false);
        passwordField.requestFocus();
        passwordField.positionCaret(passwordField.getText().length());
    }

    private void clearErrorMessage() {
        incorrectLoginInformation.setText("");
    }

    @FXML
    private void handleLogin() throws IOException {
        String email = usernameField.getText();
        String password = passwordField.getText();
        User loginUser = authenticate(email,password);
        if(loginUser!= null){
            Session session = Session.getInstance();
            session.setLoggedInUser(loginUser);
            checkAuthorization(loginUser);
        }else {
            //validator
            incorrectLoginInformation.setText("Incorrect login information!");
        }
    }

    private void checkAuthorization(User loginUser) throws IOException {
        Role userRole = loginUser.getRole();
        Stage currentStage = (Stage) loginButton.getScene().getWindow();
        Stage stage = new Stage();
        stage.setTitle("Librio");
        if(userRole.equals(Role.LIBRARIAN)){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdDashboard.fxml"));
            Parent adminDashboardRoot  = loader.load();
            stage.setScene(new Scene(adminDashboardRoot));
        }else if(userRole.equals(Role.MEMBER)){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/Book.fxml"));
            Parent adminDashboardRoot  = loader.load();
            stage.setScene(new Scene(adminDashboardRoot));
        }
        stage.show();
        currentStage.close();
    }
}


