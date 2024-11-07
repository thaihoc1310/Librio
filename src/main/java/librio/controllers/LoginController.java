package librio.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
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
    private ImageView closeEyeImage1;
    @FXML
    private ImageView closeEyeImage11;
    @FXML
    private ImageView openEyeImage1;
    @FXML
    private ImageView openEyeImage11;
    @FXML
    private ImageView openEyeImage;
    @FXML
    private ImageView closeEyeImage;
    @FXML
    private AnchorPane leftPane;
    @FXML
    private AnchorPane rightPane;
    @FXML
    private AnchorPane centerPane;
    @FXML
    private Label switchSignUp;
    @FXML
    private Label switchSignIn;

    @FXML
    private void initialize() {
        passwordTextVisible.setVisible(false);
        usernameField.setOnMouseClicked(event -> clearErrorMessage());
        passwordField.setOnMouseClicked(event -> clearErrorMessage());
        switchSignUp.setOnMouseClicked(event -> switchToSignUp());
        switchSignIn.setOnMouseClicked(event -> switchToSignIn());
    }

    @FXML
    private void switchToSignUp() {
        TranslateTransition leftPaneTranslate = new TranslateTransition(Duration.seconds(0.3), leftPane);
        leftPaneTranslate.setByX(-300);

        FadeTransition rightPaneFade = new FadeTransition(Duration.seconds(0.3), rightPane);
        rightPaneFade.setFromValue(1.0);
        rightPaneFade.setToValue(0.0);

        FadeTransition centerPaneFade = new FadeTransition(Duration.seconds(0.3), centerPane);
        centerPaneFade.setFromValue(0.0);
        centerPaneFade.setToValue(1.0);

        centerPane.setVisible(true);

        ParallelTransition parallelTransition = new ParallelTransition(leftPaneTranslate, rightPaneFade);

        SequentialTransition transition = new SequentialTransition(parallelTransition, centerPaneFade);

        transition.setOnFinished(e -> {
            leftPane.setVisible(false);
            rightPane.setVisible(false);
        });

        transition.play();
        incorrectLoginInformation.setText("");
    }

    @FXML
    private void switchToSignIn() {
        TranslateTransition leftPaneTranslate = new TranslateTransition(Duration.seconds(0.3), leftPane);
        leftPaneTranslate.setByX(300);

        FadeTransition rightPaneFade = new FadeTransition(Duration.seconds(0.3), rightPane);
        rightPaneFade.setFromValue(0.0);
        rightPaneFade.setToValue(1.0);

        FadeTransition centerPaneFade = new FadeTransition(Duration.seconds(0.3), centerPane);
        centerPaneFade.setFromValue(1.0);
        centerPaneFade.setToValue(0.0);


        ParallelTransition parallelTransition = new ParallelTransition(leftPaneTranslate, rightPaneFade);

        SequentialTransition transition = new SequentialTransition(centerPaneFade, parallelTransition);

        leftPane.setVisible(true);
        rightPane.setVisible(true);

        transition.setOnFinished(e -> {
            centerPane.setVisible(false);
        });

        transition.play();

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
        User loginUser = authenticate(email, password);
        if (loginUser != null) {
            Session session = Session.getInstance();
            session.setLoggedInUser(loginUser);
            checkAuthorization(loginUser);
        } else {
            incorrectLoginInformation.setText("Incorrect login information!");
        }
    }

    private void checkAuthorization(User loginUser) throws IOException {
        Role userRole = loginUser.getRole();
        Stage currentStage = (Stage) loginButton.getScene().getWindow();
        Stage stage = new Stage();
        stage.setTitle("Librio");
        if (userRole.equals(Role.LIBRARIAN)) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdDashboard.fxml"));
            Parent adminDashboardRoot = loader.load();
            stage.setScene(new Scene(adminDashboardRoot));
        } else if (userRole.equals(Role.MEMBER)) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/Homepage.fxml"));
            Parent adminDashboardRoot = loader.load();
            stage.setScene(new Scene(adminDashboardRoot));
        }
        stage.show();
        currentStage.close();
    }
}


