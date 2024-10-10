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
                String projectDir = System.getProperty("user.dir");
                String avatarsDir = projectDir + "/src/main/resources/images/user/";
                Files.copy(Paths.get(previousAvatarFilePath), Paths.get(avatarsDir + avatarFilePath));
                //navigate
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
}

