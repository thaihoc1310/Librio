package librio.controllers.auth;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import librio.models.User;
import librio.enums.Gender;
import librio.session.Session;
import librio.database.DatabaseConnection;
import librio.enums.Role;
import librio.util.DatabaseUtil;
import librio.util.EmailUtil;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static librio.util.DatabaseUtil.authenticate;
import static librio.util.DatabaseUtil.isEmailExists;

public class LoginController {

    @FXML
    private Button loginButton, nextButton, submitButton, signUpButton;
    @FXML
    private PasswordField passwordField, signUpPasswordField, signUpConfirmPasswordField, newPasswordField, confirmNewPasswordField;
    @FXML
    private TextField usernameField, passwordTextVisible, resetCode, signUpPasswordTextVisible, signUpConfirmPasswordTextVisible;
    @FXML
    private TextField emailTextField, emailTextField1, userNameTextField, phoneNumberTextField, newPasswordFieldVisible, confirmNewPasswordFieldVisible;
    @FXML
    private ImageView closeEyeImage1, openEyeImage1, closeEyeImage2, openEyeImage2, openEyeImage21, closeEyeImage21, openEyeImage11, closeEyeImage11;
    @FXML
    private ImageView openEyeImage, closeEyeImage;
    @FXML
    private Label incorrectLoginInformation, switchSignUp, switchSignIn, forgotPassword, switchSignIn1, switchSignIn2, sentCodeButton, emailErrorLabel, resetCodeErrorLabel, confirmPasswordErrorLabel, passwordErrorLabel, nameErrorLabel, emailErrorLabel1, genderAndbirthDateErrorLabel, phoneNumberErrorLabel, confirmPasswordErrorLabel1, passwordErrorLabel1;
    @FXML
    private AnchorPane leftPane, rightPane, centerPane, sendCodePane, changePassWordPane;
    @FXML
    private ComboBox<Gender> genderComboBox;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private ProgressIndicator loadingIndicator;

    private ExecutorService executor;
    private String generatedCode;
    private User user;

    @FXML
    private void initialize() {
        executor = Executors.newFixedThreadPool(2);
        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));

        passwordTextVisible.setVisible(false);
        signUpPasswordTextVisible.setVisible(false);
        signUpConfirmPasswordTextVisible.setVisible(false);
        newPasswordFieldVisible.setVisible(false);
        confirmNewPasswordFieldVisible.setVisible(false);


        usernameField.setOnMouseClicked(event -> clearErrorMessage());
        passwordField.setOnMouseClicked(event -> clearErrorMessage());
        passwordTextVisible.setOnMouseClicked(event -> clearErrorMessage());
        switchSignUp.setOnMouseClicked(event -> switchToSignUpAndForgotPassword(centerPane));
        switchSignIn.setOnMouseClicked(event -> switchToSignIn(centerPane));
        switchSignIn1.setOnMouseClicked(event -> switchToSignIn(sendCodePane));
        switchSignIn2.setOnMouseClicked(event -> switchToSignIn(changePassWordPane));
        forgotPassword.setOnMouseClicked(event -> switchToSignUpAndForgotPassword(sendCodePane));
        setUpEyePassword();
    }

    @FXML
    private void switchToSignUpAndForgotPassword(AnchorPane pane) {
        TranslateTransition leftPaneTranslate = new TranslateTransition(Duration.seconds(0.3), leftPane);
        leftPaneTranslate.setByX(-300);

        FadeTransition rightPaneFade = new FadeTransition(Duration.seconds(0.3), rightPane);
        rightPaneFade.setFromValue(1.0);
        rightPaneFade.setToValue(0.0);

        FadeTransition paneFade = new FadeTransition(Duration.seconds(0.3), pane);
        paneFade.setFromValue(0.0);
        paneFade.setToValue(1.0);

        pane.setVisible(true);

        ParallelTransition parallelTransition = new ParallelTransition(leftPaneTranslate, rightPaneFade);

        SequentialTransition transition = new SequentialTransition(parallelTransition, paneFade);

        transition.setOnFinished(e -> {
            leftPane.setVisible(false);
            rightPane.setVisible(false);
        });

        transition.play();
        incorrectLoginInformation.setText("");
        clearFieldData();
    }

    @FXML
    private void switchToSignIn(AnchorPane pane) {
        TranslateTransition leftPaneTranslate = new TranslateTransition(Duration.seconds(0.3), leftPane);
        leftPaneTranslate.setByX(300);

        FadeTransition rightPaneFade = new FadeTransition(Duration.seconds(0.3), rightPane);
        rightPaneFade.setFromValue(0.0);
        rightPaneFade.setToValue(1.0);

        FadeTransition paneFade = new FadeTransition(Duration.seconds(0.3), pane);
        paneFade.setFromValue(1.0);
        paneFade.setToValue(0.0);

        ParallelTransition parallelTransition = new ParallelTransition(leftPaneTranslate, rightPaneFade);

        SequentialTransition transition = new SequentialTransition(paneFade, parallelTransition);

        leftPane.setVisible(true);
        rightPane.setVisible(true);

        transition.setOnFinished(e -> {
            pane.setVisible(false);
        });

        transition.play();
        hideSignUpErrorLabels();
        clearFieldData();
        hideForgotPasswordErrorLabels();
        clearForgotPasswordFieldData();
    }

    @FXML
    private void handleLogin() throws IOException {
        String email = usernameField.getText();
        String password = passwordField.isVisible() ? passwordField.getText() : passwordTextVisible.getText();
        User loginUser = authenticate(email.trim(), password);
        if (loginUser != null) {
            Session session = Session.getInstance();
            session.setLoggedInUser(loginUser);
            checkAuthorization(loginUser);
            DatabaseUtil.startAutoUpdate();
        } else {
            incorrectLoginInformation.setText("Incorrect login information!");
        }
    }

    @FXML
    private void handleSignUp() throws IOException {
        String name = userNameTextField.getText();
        String email = emailTextField.getText();
        String phoneNumber = phoneNumberTextField.getText();
        String signUpPassword = signUpPasswordField.isVisible() ?  signUpPasswordField.getText() : signUpPasswordTextVisible.getText();
        String signUpConfirmPassword = signUpConfirmPasswordField.isVisible() ? signUpConfirmPasswordField.getText() : signUpConfirmPasswordTextVisible.getText();
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
        }else if(signUpPassword.length() < 6){
            passwordErrorLabel.setText("Password must be at least 6 characters!");
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
                switchToSignIn(centerPane);
            }

        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openForgotPasswordScene() {
        sendCodePane.setVisible(true);
        FadeTransition sendCodePaneFade = new FadeTransition(Duration.seconds(0.3), sendCodePane);
        sendCodePaneFade.setFromValue(0.0);
        sendCodePaneFade.setToValue(1.0);
        sendCodePaneFade.play();

        rightPane.setVisible(false);
        leftPane.setVisible(false);
        centerPane.setVisible(false);
    }

    @FXML
    private void sendResetCode() {
        resetCode.clear();
        resetCodeErrorLabel.setText("");
        String email = emailTextField1.getText().trim();
        if (email.isEmpty()) {
            emailErrorLabel1.setText("Email cannot be empty!");
            return;
        } else if (!DatabaseUtil.isEmailExists(email)) {
            emailErrorLabel1.setText("Email does not exist!");
            return;
        }
        user = DatabaseUtil.getUserByEmail(email);
        generatedCode = generateResetCode();
        EmailUtil.sendResetCode(email, generatedCode);
    }



    @FXML
    private void validateResetCode() {
        String code = resetCode.getText().trim();
        if (code.isEmpty()) {
            resetCodeErrorLabel.setText("Reset code cannot be empty!");
            return;
        } else if (!code.equals(generatedCode)) {
            resetCodeErrorLabel.setText("Invalid reset code!");
            return;
        }
        switchToChangePassWord();
        hideForgotPasswordErrorLabels();
        clearForgotPasswordFieldData();
    }

    private void switchToChangePassWord() {
        FadeTransition sendCodePaneFade = new FadeTransition(Duration.seconds(0.3), sendCodePane);
        sendCodePaneFade.setFromValue(1.0);
        sendCodePaneFade.setToValue(0.0);

        FadeTransition changePassWordPaneFade = new FadeTransition(Duration.seconds(0.3), changePassWordPane);
        changePassWordPaneFade.setFromValue(0.0);
        changePassWordPaneFade.setToValue(1.0);

        changePassWordPane.setVisible(true);

        SequentialTransition transition = new SequentialTransition(sendCodePaneFade, changePassWordPaneFade);

        transition.setOnFinished(e -> {
            sendCodePane.setVisible(false);
        });

        transition.play();
    }

    @FXML
    private void updatePassword() {
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmNewPasswordField.getText().trim();

        if (newPassword.isEmpty()) {
            passwordErrorLabel1.setText("Password cannot be empty!");
            return;
        } else if (confirmPassword.isEmpty()) {
            confirmPasswordErrorLabel1.setText("Confirm password cannot be empty!");
            return;
        } else if (!newPassword.equals(confirmPassword)) {
            confirmPasswordErrorLabel1.setText("Passwords do not match!");
            return;
        }
        DatabaseUtil.updateUserPassword(user.getId(), newPassword);

        switchToSignIn(changePassWordPane);
    }

    private String generateResetCode() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(999999);
        return String.format("%06d", num);
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

    private void checkAuthorization(User loginUser) throws IOException {
        Role userRole = loginUser.getRole();
        Stage currentStage = (Stage) loginButton.getScene().getWindow();
        Stage stage = new Stage();
        stage.setTitle("Librio");

        loadingIndicator.setVisible(true);
        Task<Parent> loadHomePageTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                if (userRole.equals(Role.LIBRARIAN)) {
                    return new FXMLLoader(getClass().getResource("/fxml/admin/AdDashboard.fxml")).load();
                } else if (userRole.equals(Role.MEMBER)) {
                    return new FXMLLoader(getClass().getResource("/fxml/member/Homepage.fxml")).load();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Parent homepageRoot = getValue();
                Platform.runLater(() -> {
                    stage.setScene(new Scene(homepageRoot));
                    stage.show();
                    loadingIndicator.setVisible(false);
                    currentStage.close();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> loadingIndicator.setVisible(false));
                getException().printStackTrace();
            }
        };

        executor.submit(loadHomePageTask);
    }


    @FXML
    private void hideForgotPasswordErrorLabels() {
        resetCodeErrorLabel.setText("");
        emailErrorLabel1.setText("");
        passwordErrorLabel1.setText("");
        confirmPasswordErrorLabel1.setText("");
    }

    private void clearForgotPasswordFieldData(){
        emailTextField1.clear();
        resetCode.clear();
        confirmNewPasswordField.clear();
        newPasswordField.clear();
    }

    @FXML
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

    private void setUpEyePassword() {
        openEyeImage.toFront();
        closeEyeImage.toFront();
        openEyeImage1.toFront();
        closeEyeImage1.toFront();
        openEyeImage2.toFront();
        closeEyeImage2.toFront();
        openEyeImage21.toFront();
        closeEyeImage21.toFront();
        openEyeImage11.toFront();
        closeEyeImage11.toFront();

        openEyeImage.setOnMouseClicked(event -> showPassword(passwordField, passwordTextVisible, openEyeImage, closeEyeImage));
        closeEyeImage.setOnMouseClicked(event -> hidePassword(passwordField, passwordTextVisible, openEyeImage, closeEyeImage));
        openEyeImage1.setOnMouseClicked(event -> showPassword(signUpPasswordField, signUpPasswordTextVisible, openEyeImage1, closeEyeImage1));
        closeEyeImage1.setOnMouseClicked(event -> hidePassword(signUpPasswordField, signUpPasswordTextVisible, openEyeImage1, closeEyeImage1));
        openEyeImage2.setOnMouseClicked(event -> showPassword(signUpConfirmPasswordField, signUpConfirmPasswordTextVisible, openEyeImage2, closeEyeImage2));
        closeEyeImage2.setOnMouseClicked(event -> hidePassword(signUpConfirmPasswordField, signUpConfirmPasswordTextVisible, openEyeImage2, closeEyeImage2));
        openEyeImage21.setOnMouseClicked(event -> showPassword(confirmNewPasswordField, confirmNewPasswordFieldVisible, openEyeImage21, closeEyeImage21));
        closeEyeImage21.setOnMouseClicked(event -> hidePassword(confirmNewPasswordField, confirmNewPasswordFieldVisible, openEyeImage21, closeEyeImage21));
        openEyeImage11.setOnMouseClicked(event -> showPassword(newPasswordField, newPasswordFieldVisible, openEyeImage11, closeEyeImage11));
        closeEyeImage11.setOnMouseClicked(event -> hidePassword(newPasswordField, newPasswordFieldVisible, openEyeImage11, closeEyeImage11));
    }
}