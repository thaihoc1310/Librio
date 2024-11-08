package librio.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import librio.auth.Session;
import librio.database.DatabaseConnection;
import librio.models.Gender;
import librio.models.Role;
import librio.models.User;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

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
    private ImageView openEyeImage1;
    @FXML
    private ImageView closeEyeImage2;
    @FXML
    private ImageView openEyeImage2;
    @FXML
    private TextField userNameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField phoneNumberTextField;
    @FXML
    private PasswordField signUpPasswordField;
    @FXML
    private PasswordField signUpConfirmPasswordField;
    @FXML
    private ComboBox<Gender> genderComboBox;
    @FXML
    private DatePicker birthDatePicker;
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
        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        passwordTextVisible.setVisible(false);
        usernameField.setOnMouseClicked(event -> clearErrorMessage());
        passwordField.setOnMouseClicked(event -> clearErrorMessage());
        switchSignUp.setOnMouseClicked(event -> switchToSignUp());
        switchSignIn.setOnMouseClicked(event -> switchToSignIn());
        //addListeners();
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

    @FXML
    private void handleSignUp() throws IOException {
        String name = userNameTextField.getText();
        String email = emailTextField.getText();
        String phoneNumber = phoneNumberTextField.getText();
        String signUpPassword = signUpPasswordField.getText();
        String signUpConfirmPassword = signUpConfirmPasswordField.getText();
        Gender gender = genderComboBox.getValue();
        LocalDate birthDate = birthDatePicker.getValue();

        String query = "INSERT INTO users (name, email, password, phone_number, gender, birth_of_date, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, signUpPassword);
            statement.setString(4, phoneNumber);
            statement.setString(5, gender.name());
            assert birthDate != null;
            statement.setDate(6, Date.valueOf(birthDate));
            statement.setString(7, email);

            int rowsInserted = statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);

                String insertMemberQuery = "INSERT INTO Members (id, fine_amount, total_books_borrowed) VALUES (?, ?, ?)";
                try (PreparedStatement memberStatement = connection.prepareStatement(insertMemberQuery)) {
                    memberStatement.setInt(1, userId); // userId là id của user vừa tạo
                    memberStatement.setLong(2, 0); // Fine amount bắt đầu từ 0
                    memberStatement.setLong(3, 0); // Total books borrowed bắt đầu từ 0
                    memberStatement.executeUpdate();
                }
            }

        }catch (SQLException e) {
            e.printStackTrace();
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

    private void addListeners() {

        genderComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                //genderErrorLabel.setText("Gender must be selected");
            } else {
                //.setText("");
            }
        });
    }
}


