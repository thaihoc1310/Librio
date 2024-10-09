package librio.controllers.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
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
    private ImageView avatarImageView;  // ImageView để hiển thị ảnh đại diện
    private String avatarFilePath;
    private String previousAvatarFilePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
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
        Gender gender = Gender.valueOf(String.valueOf(genderComboBox.getValue()));
        Role role = Role.valueOf(String.valueOf(roleComboBox.getValue()));
        String address = addressTextArea.getText();

        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match!");
            return;
        }

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
                System.out.println("A new user was inserted successfully!");
                //navigate
                if (manageUserController != null) {
                    manageUserController.loadUsersFromDatabase();
                }
                clearInputFields();

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            try {
                // Đường dẫn đến thư mục lưu ảnh trong dự án
                String projectDir = System.getProperty("user.dir");
                String avatarsDir = projectDir + "/src/main/resources/images/user/";
                if (avatarFilePath != null) {
                    File oldAvatarFile = new File(projectDir + avatarFilePath);
                    if (oldAvatarFile.exists()) {
                        oldAvatarFile.delete(); // Xóa ảnh cũ
                        System.out.println("Đã xóa ảnh cũ: " + avatarFilePath);
                    }
                }
                // Tạo tên file mới để tránh trùng lặp, có thể sử dụng tên file hoặc UUID
                String newFileName = System.currentTimeMillis() + "_" + selectedFile.getName();

                // Sao chép ảnh vào thư mục avatars
                Files.copy(selectedFile.toPath(), Paths.get(avatarsDir + newFileName));

                // Lưu đường dẫn ảnh
                avatarFilePath = "/images/user/" + newFileName;
                System.out.println("Ảnh đã được lưu tại: " + avatarFilePath);
                Image avatarImage = new Image(selectedFile.toURI().toString());
                avatarImageView.setImage(avatarImage);  // Gán ảnh vào ImageView
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void cancelCreateUser() {
        clearInputFields();
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
        avatarFilePath = null;
    }


}

