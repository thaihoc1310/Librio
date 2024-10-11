package librio.controllers.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Circle;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CreateUserController implements Initializable {
    private ManageUserController manageUserController;
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
    private TextField avatarTextField;
    @FXML
    private TextArea addressTextArea;
    @FXML
    private Button createUserButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button addAvatarButton;
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

    public void setManageUserController(ManageUserController manageUserController) {
        this.manageUserController = manageUserController;
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

        boolean validation = false;

        if(name.isEmpty()){
            nameErrorLabel.setText("Name cannot be empty");
            validation = true;
        }

        if(password.isEmpty()){
            passwordErrorLabel.setText("Password cannot be empty");
            validation = true;
        }

        if(isEmailExists(email)) {
            emailErrorLabel.setText("Email already exists");
            validation = true;
        }

        if(email.isEmpty()){
            emailErrorLabel.setText("Email cannot be empty");
            validation = true;
        }

        if(confirmPassword.isEmpty()){
            confirmPasswordErrorLabel.setText("Passwords do not match");
            validation = true;
        }

        if(phoneNumber.isEmpty()){
            phoneNumberErrorLabel.setText("Phone number cannot be empty");
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

        if(validation) return;
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO users (name, email, password, phone_number, address, gender, role, avatar) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, name);
                statement.setString(2, email);
                statement.setString(3, password);
                statement.setString(4, phoneNumber);
                statement.setString(5, address);
                statement.setString(6, gender.name());
                statement.setString(7, role.name());
                statement.setString(8, avatarFilePath);

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    String projectDir = System.getProperty("user.dir");
                    String avatarsDir = projectDir + "/src/main/resources/images/user/";
                    if(previousAvatarFilePath != null){
                        Files.copy(Paths.get(previousAvatarFilePath), Paths.get(avatarsDir + avatarFilePath));
                    }
                    if (manageUserController != null) {
                        manageUserController.loadUsersFromDatabase();
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
            cropAndClipToCircle(avatarImage, avatarImageView, 50);
            previousAvatarFilePath = selectedFile.getAbsolutePath();
        }

    }

    private boolean isEmailExists(String email) {
        boolean exists = false;
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                exists = resultSet.getInt(1) > 0;
                //resultSet.getInt => get result of count(*)
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exists;
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

    }

    public static void cropAndClipToCircle(Image avatarImage, ImageView avatarImageView, double radius) {
        // Lấy chiều rộng và chiều cao của ảnh
        double width = avatarImage.getWidth();
        double height = avatarImage.getHeight();

        // Tính toán kích thước để cắt ảnh thành hình vuông
        double cropSize = Math.min(width, height);  // Chọn kích thước nhỏ hơn giữa width và height

        // Tính toán tọa độ bắt đầu để cắt hình vuông từ trung tâm của ảnh
        double x = (width - cropSize) / 2;
        double y = (height - cropSize) / 2;

        // Cắt ảnh thành hình vuông
        PixelReader reader = avatarImage.getPixelReader();
        WritableImage squareImage = new WritableImage(reader, (int) x, (int) y, (int) cropSize, (int) cropSize);

        // Hiển thị ảnh đã cắt trong ImageView
        avatarImageView.setImage(squareImage);
        avatarImageView.setPreserveRatio(true);

        // Tạo clip hình tròn với bán kính được cung cấp và tâm tại (radius, radius)
        Circle clip = new Circle(radius, radius, radius);
        avatarImageView.setClip(clip);  // Thiết lập clip hình tròn cho ImageView
    }

}

