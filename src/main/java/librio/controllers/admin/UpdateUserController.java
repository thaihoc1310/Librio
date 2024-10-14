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
import librio.models.Gender;
import librio.models.Role;
import librio.models.User;
import librio.database.DatabaseConnection;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UpdateUserController implements Initializable {

    @FXML
    private TextField emailTextField;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField phoneNumberTextField;
    @FXML
    private ComboBox<Gender> genderComboBox;
    @FXML
    private ComboBox<Role> roleComboBox;
    @FXML
    private TextArea addressTextArea;
    @FXML
    private Button updateUserButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label emailErrorLabel;
    @FXML
    private Label nameErrorLabel;
    @FXML
    private Label phoneNumberErrorLabel;
    @FXML
    private Label addressErrorLabel;
    @FXML
    private ImageView avatarImageView;
    private String avatarFilePath;
    private String previousAvatarFilePath;

    private User user;
    private ManageUserController manageUserController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
        genderComboBox.setEditable(false);
        roleComboBox.setEditable(false);
        hideErrorLabels();
        addListeners();
    }

    public void setUser(User user) {
        this.user = user;
        populateFields();
    }

    public void setManageUserController(ManageUserController manageUserController) {
        this.manageUserController = manageUserController;
    }

    private void populateFields() {
        if (user != null) {
            emailTextField.setText(user.getEmail());
            nameTextField.setText(user.getName());
            phoneNumberTextField.setText(user.getPhoneNumber());
            addressTextArea.setText(user.getAddress());
            genderComboBox.setValue(user.getGender());
            roleComboBox.setValue(user.getRole());

            String projectDir = System.getProperty("user.dir");
            String avatarsDir = projectDir + "/src/main/resources/images/user/";
            String path = avatarsDir + user.getAvatar();

            File file = new File(path);
            if (file.exists()) {
                Image avatarImage = new Image(file.toURI().toString());
                cropAndClipToCircle(avatarImage, avatarImageView, 50);
            } else {
                System.out.println("File ảnh không tồn tại: " + path);
            }
        }
    }

    @FXML
    private void updateUser() {
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        String phoneNumber = phoneNumberTextField.getText();
        Gender gender = Gender.valueOf(String.valueOf(genderComboBox.getValue()));
        Role role = Role.valueOf(String.valueOf(roleComboBox.getValue()));
        String address = addressTextArea.getText();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "UPDATE users SET name = ?, email = ?, phone_number = ?, address = ?, gender = ?, role = ?, avatar = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phoneNumber);
            statement.setString(4, address);
            statement.setString(5, gender.name());
            statement.setString(6, role.name());
            statement.setString(7, avatarFilePath);
            statement.setString(8, user.getId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                if (previousAvatarFilePath != null && avatarFilePath != null) {
                    String projectDir = System.getProperty("user.dir");
                    String avatarsDir = projectDir + "/src/main/resources/images/user/";
                    if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                        File oldFile = new File(avatarsDir + user.getAvatar());
                        if (oldFile.exists()) {
                            boolean deleted = oldFile.delete();
                            if (!deleted) {
                                System.out.println("Không thể xóa tệp ảnh cũ: " + oldFile.getAbsolutePath());
                            }
                        }
                    }
                    Files.copy(Paths.get(previousAvatarFilePath), Paths.get(avatarsDir + avatarFilePath));
                }
                if (manageUserController != null) {
                    manageUserController.loadUsersFromDatabase();
                }
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
    private void cancelUpdateUser() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) updateUserButton.getScene().getWindow();
        stage.close();
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

    private void hideErrorLabels() {
        nameErrorLabel.setText("");
        emailErrorLabel.setText("");
        phoneNumberErrorLabel.setText("");
        addressErrorLabel.setText("");
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

        //Address validation
        addressTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                addressErrorLabel.setText("Address cannot be empty");
            }else{
                addressErrorLabel.setText("");
            }
        });
    }
}