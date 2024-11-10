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
import static librio.util.DatabaseUtil.isEmailExists;

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
    private TextField signUpPasswordTextVisible;
    @FXML
    private PasswordField signUpConfirmPasswordField;
    @FXML
    private TextField signUpConfirmPasswordTextVisible;
    @FXML
    private ComboBox<Gender> genderComboBox;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private ImageView openEyeImage;
    @FXML
    private ImageView closeEyeImage;
    @FXML
    private Label nameErrorLabel;
    @FXML
    private Label emailErrorLabel;
    @FXML
    private Label phoneNumberErrorLabel;
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Label confirmPasswordErrorLabel;
    @FXML
    private Label genderAndbirthDateErrorLabel;
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
        signUpPasswordTextVisible.setVisible(false);
        signUpConfirmPasswordTextVisible.setVisible(false);

        openEyeImage1.toFront();
        closeEyeImage1.toFront();
        openEyeImage2.toFront();
        closeEyeImage2.toFront();

        openEyeImage.setOnMouseClicked(event -> showPassword(passwordField, passwordTextVisible, openEyeImage, closeEyeImage));
        closeEyeImage.setOnMouseClicked(event -> hidePassword(passwordField, passwordTextVisible, openEyeImage, closeEyeImage));

        openEyeImage1.setOnMouseClicked(event -> showPassword(signUpPasswordField, signUpPasswordTextVisible, openEyeImage1, closeEyeImage1));
        closeEyeImage1.setOnMouseClicked(event -> hidePassword(signUpPasswordField, signUpPasswordTextVisible, openEyeImage1, closeEyeImage1));

        openEyeImage2.setOnMouseClicked(event -> showPassword(signUpConfirmPasswordField, signUpConfirmPasswordTextVisible, openEyeImage2, closeEyeImage2));
        closeEyeImage2.setOnMouseClicked(event -> hidePassword(signUpConfirmPasswordField, signUpConfirmPasswordTextVisible, openEyeImage2, closeEyeImage2));


        usernameField.setOnMouseClicked(event -> clearErrorMessage());
        passwordField.setOnMouseClicked(event -> clearErrorMessage());
        switchSignUp.setOnMouseClicked(event -> switchToSignUp());
        switchSignIn.setOnMouseClicked(event -> switchToSignIn());
        addHideErrorListenersToSignUpFields();
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
        clearFieldData();
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
        hideSignUpErrorLabels();
        clearFieldData();
    }


    private void showPassword(PasswordField passField, TextField passTextVisible, ImageView openEye, ImageView closeEye) {
        passTextVisible.setText(passField.getText());
        passTextVisible.setVisible(true);
        passField.setVisible(false);
        openEye.setVisible(false);
        closeEye.setVisible(true);
        passTextVisible.requestFocus();
        passTextVisible.positionCaret(passTextVisible.getText().length());
    }

    private void hidePassword(PasswordField passField, TextField passTextVisible, ImageView openEye, ImageView closeEye) {
        passField.setText(passTextVisible.getText());
        passField.setVisible(true);
        passTextVisible.setVisible(false);
        openEye.setVisible(true);
        closeEye.setVisible(false);
        passField.requestFocus();
        passField.positionCaret(passField.getText().length());
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

        boolean validation = false;

        if(name.trim().isEmpty()){
            nameErrorLabel.setText("Name cannot be empty!");
            validation = true;
        }

        if(email.trim().isEmpty()){
            emailErrorLabel.setText("Email cannot be empty!");
            validation = true;
        }else if(isEmailExists(email.trim())){
            emailErrorLabel.setText("This email already exists!");
            validation = true;
        }else if(!email.trim().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")){
            emailErrorLabel.setText("Invalid email address!");
            validation = true;
        }

        if (phoneNumber.trim().isEmpty()) {
            phoneNumberErrorLabel.setText("Phone number cannot be empty!");
            validation = true;
        } else if (!phoneNumber.trim().matches("\\d{10}")) {
            phoneNumberErrorLabel.setText("Phone number must be 10 digits!");
            validation = true;
        }

        if(gender == null || birthDate == null){
            genderAndbirthDateErrorLabel.setText("Please select your Gender and Birth Date!");
            validation = true;
        }else if(birthDate.isAfter(LocalDate.now())){
            genderAndbirthDateErrorLabel.setText("Birth date must be before now!");
        }

        if(signUpPassword.isEmpty()){
            passwordErrorLabel.setText("Password cannot be empty!");
            validation = true;
        }

        if(signUpConfirmPassword.isEmpty()){
            confirmPasswordErrorLabel.setText("Confirm password cannot be empty!");
            validation = true;
        }else if(!signUpConfirmPassword.equals(signUpPassword)){
            confirmPasswordErrorLabel.setText("Passwords do not match!");
            validation = true;
        }

        if(validation){
            return;
        }

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
                switchToSignIn();
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

    private void addHideErrorListenersToSignUpFields() {
        userNameTextField.setOnMouseClicked (event -> {hideSignUpErrorLabels();});
        emailTextField.setOnMouseClicked (event -> {hideSignUpErrorLabels();});
        phoneNumberTextField.setOnMouseClicked (event -> {hideSignUpErrorLabels();});
        signUpPasswordField.setOnMouseClicked (event -> {hideSignUpErrorLabels();});
        signUpConfirmPasswordField.setOnMouseClicked (event -> {hideSignUpErrorLabels();});
        genderComboBox.setOnMouseClicked (event -> {hideSignUpErrorLabels();});
        birthDatePicker.setOnMouseClicked (event -> {hideSignUpErrorLabels();});
    }

    private void hideSignUpErrorLabels() {
        nameErrorLabel.setText("");
        emailErrorLabel.setText("");
        passwordErrorLabel.setText("");
        confirmPasswordErrorLabel.setText("");
        phoneNumberErrorLabel.setText("");
        genderAndbirthDateErrorLabel.setText("");
    }

    private void clearFieldData(){
        userNameTextField.clear();
        passwordField.clear();
        passwordField.clear();
        usernameField.clear();
        phoneNumberTextField.clear();
        emailTextField.clear();
        signUpPasswordField.clear();
        signUpPasswordTextVisible.clear();
        signUpConfirmPasswordField.clear();
        signUpConfirmPasswordTextVisible.clear();
        genderComboBox.getSelectionModel();
        birthDatePicker.getEditor().clear();
    }
}


