package librio.controllers.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import librio.database.DatabaseConnection;
import librio.models.Gender;
import librio.models.Role;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;
import static librio.util.DatabaseUtil.isEmailExists;
import static librio.util.DesignUtil.cropAndClipToCircle;

public class CreateUserController implements Initializable {
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private TextField confirmPasswordTextField;
    @FXML
    private TextField phoneNumberTextField;
    @FXML
    private ComboBox<Gender> genderComboBox;
    @FXML
    private ComboBox<Role> roleComboBox;
    @FXML
    private TextArea addressTextArea;
    @FXML
    private Button createUserButton;
    @FXML
    private Label nameErrorLabel;
    @FXML
    private Label emailErrorLabel;
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Label confirmPasswordErrorLabel;
    @FXML
    private Label phoneNumberErrorLabel;
    @FXML
    private Label roleErrorLabel;
    @FXML
    private Label genderErrorLabel;
    @FXML
    private DatePicker birthOfDatePicker;
    @FXML
    private Label birthOfDateErrorLabel;
    @FXML
    private ImageView avatarImageView;  // ImageView để hiển thị ảnh đại diện
    private String avatarFilePath;
    private String previousAvatarFilePath;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
        hideErrorLabels();
        addListeners();
    }


    @FXML
    private void createUser() {
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        String password = passwordTextField.getText();
        String confirmPassword = confirmPasswordTextField.getText();
        String phoneNumber = phoneNumberTextField.getText();
        Gender gender = genderComboBox.getValue();
        Role role = roleComboBox.getValue();
        String address = addressTextArea.getText();
        LocalDate birthOfDate = birthOfDatePicker.getValue();
        boolean validation = false;

        if(name.isEmpty()){
            nameErrorLabel.setText("Name cannot be empty");
            validation = true;
        }

        if(password.isEmpty()){
            passwordErrorLabel.setText("Password cannot be empty");
            validation = true;
        }

        if (email.isEmpty()) {
            emailErrorLabel.setText("Email cannot be empty");
            validation = true;
        } else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            emailErrorLabel.setText("Invalid email format");
            validation = true;
        } else if (isEmailExists(email)) {
            emailErrorLabel.setText("Email already exists");
            validation = true;
        }

        if (confirmPassword.isEmpty() || !confirmPassword.equals(password)) {
            confirmPasswordErrorLabel.setText("Passwords do not match");
            validation = true;
        }

        if (phoneNumber.isEmpty()) {
            phoneNumberErrorLabel.setText("Phone number cannot be empty");
            validation = true;
        } else if (!phoneNumber.matches("\\d{10}")) {
            phoneNumberErrorLabel.setText("Phone number must be 10 digits");
            validation = true;
        }

        if(role == null){
            roleErrorLabel.setText("Role must be selected");
            validation = true;
        }

        if(gender == null){
            genderErrorLabel.setText("Gender must be selected");
            validation = true;
        }

        if(birthOfDate == null){
            birthOfDateErrorLabel.setText("Birth of Date must be selected");
        }

        if(validation) {
            return;
        }
        String query = "INSERT INTO users (name, email, password, phone_number, address, gender, role, avatar, birth_of_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, phoneNumber);
            statement.setString(5, address);
            statement.setString(6, gender.name());
            statement.setString(7, role.name());
            statement.setString(8, avatarFilePath);
            assert birthOfDate != null;
            statement.setDate(9, Date.valueOf(birthOfDate));

            int rowsInserted = statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);
                if(role.equals(Role.MEMBER)){
                    String insertMemberQuery = "INSERT INTO Members (id, email, fine_amount, total_books_borrowed) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement memberStatement = connection.prepareStatement(insertMemberQuery)) {
                        memberStatement.setInt(1, userId);
                        memberStatement.setString(2, email);
                        memberStatement.setLong(3, 0);
                        memberStatement.setLong(4, 0);
                        memberStatement.executeUpdate();
                    }
                }
                else if(role.equals(Role.LIBRARIAN)){
                    String insertLibrarianQuery = "INSERT INTO Librarians (id, email) VALUES (?, ?)";
                    try (PreparedStatement librarianStatement = connection.prepareStatement(insertLibrarianQuery)) {
                        librarianStatement.setInt(1, userId); // userId là id của user vừa tạo
                        librarianStatement.setString(2, email);
                        librarianStatement.executeUpdate();
                    }
                }
                String projectDir = System.getProperty("user.dir");
                String avatarsDir = projectDir + "/src/main/resources/images/user/";
                if(previousAvatarFilePath != null){
                    Files.copy(Paths.get(previousAvatarFilePath), Paths.get(avatarsDir + avatarFilePath));
                }
                clearInputFields();
                closeStage();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void hideErrorLabels() {
        nameErrorLabel.setText("");
        emailErrorLabel.setText("");
        passwordErrorLabel.setText("");
        confirmPasswordErrorLabel.setText("");
        phoneNumberErrorLabel.setText("");
        roleErrorLabel.setText("");
        genderErrorLabel.setText("");
        birthOfDateErrorLabel.setText("");
    }

    private void addListeners() {
        // Name validation
        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                nameErrorLabel.setText("Name cannot be empty");
            } else {
                nameErrorLabel.setText("");
            }
        });

        // Email validation
        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                emailErrorLabel.setText("Email cannot be empty");
            } else if (!newValue.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
                emailErrorLabel.setText("Invalid email format");
            } else {
                emailErrorLabel.setText("");
            }
        });

        // Password validation
        passwordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                passwordErrorLabel.setText("Password cannot be empty");
            } else {
                passwordErrorLabel.setText("");
            }
        });

        // Confirm password validation
        confirmPasswordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty() || !newValue.equals(passwordTextField.getText())) {
                confirmPasswordErrorLabel.setText("Passwords do not match");
            } else {
                confirmPasswordErrorLabel.setText("");
            }
        });

        // Phone number validation
        phoneNumberTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                phoneNumberErrorLabel.setText("Phone number cannot be empty");
            } else if (!newValue.matches("\\d{10}")) {
                phoneNumberErrorLabel.setText("Phone number must be 10 digits");
            } else {
                phoneNumberErrorLabel.setText("");
            }
        });

        // Role validation
        roleComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                roleErrorLabel.setText("Role must be selected");
            } else {
                roleErrorLabel.setText("");
            }
        });

        genderComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                genderErrorLabel.setText("Gender must be selected");
            } else {
                genderErrorLabel.setText("");
            }
        });

        birthOfDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                birthOfDateErrorLabel.setText("Birth of Date must be selected");
            } else {
                birthOfDateErrorLabel.setText("");
            }
        });
    }


    @FXML
    private void addAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose avatar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            avatarFilePath = System.currentTimeMillis() + "_" + selectedFile.getName();
            Image avatarImage = new Image(selectedFile.toURI().toString());
            cropAndClipToCircle(avatarImage, avatarImageView, 75);
            previousAvatarFilePath = selectedFile.getAbsolutePath();
        }

    }

    @FXML
    private void cancelCreateUser() {
        clearInputFields();
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) createUserButton.getScene().getWindow();
        stage.close();
    }

    private void clearInputFields() {
        nameTextField.clear();
        emailTextField.clear();
        passwordTextField.clear();
        confirmPasswordTextField.clear();
        phoneNumberTextField.clear();
        addressTextArea.clear();
        genderComboBox.getSelectionModel().clearSelection();
        roleComboBox.getSelectionModel().clearSelection();
        birthOfDatePicker.setValue(null);

    }
}

