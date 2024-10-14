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
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static librio.controllers.admin.CreateUserController.cropAndClipToCircle;

public class UpdateUserController implements Initializable {

    @FXML
    private Button updateUserButton;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField phoneNumberTextField;
    @FXML
    private ComboBox<Gender> genderComboBox;
    @FXML
    private ComboBox<Role> roleComboBox;
    @FXML
    private TextArea addressTextArea;
    @FXML
    private Label nameErrorLabel;
    @FXML
    private Label emailErrorLabel;
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
    private ImageView avatarImageView;
    private String avatarFilePath;
    private String previousAvatarFilePath;

    private User user;
    private ManageUserController manageUserController;
    private int currentPage = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
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

    public void setCurrentPage(int currentPage){
        this.currentPage = currentPage;
    }


    private void populateFields() {
        if (user != null) {
            emailTextField.setText(user.getEmail());
            nameTextField.setText(user.getName());
            phoneNumberTextField.setText(user.getPhoneNumber());
            addressTextArea.setText(user.getAddress());
            genderComboBox.setValue(user.getGender());
            roleComboBox.setValue(user.getRole());
            birthOfDatePicker.setValue(user.getBirthOfDate());

            // Lấy đường dẫn ảnh từ project
            String projectDir = System.getProperty("user.dir");
            String avatarsDir = projectDir + "/src/main/resources/images/user/";
            String path = avatarsDir + user.getAvatar();

            // Chuyển đổi đường dẫn thành URL
            File file = new File(path);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString()); // Chuyển đổi file thành URL hợp lệ
                cropAndClipToCircle(image, avatarImageView, 75);
            } else {
                String defaultImage = avatarsDir + "Male User.png";
                File defaultImageFile = new File(defaultImage);
                Image image = new Image(defaultImageFile.toURI().toString()); // Chuyển đổi file thành URL hợp lệ
                cropAndClipToCircle(image, avatarImageView, 75);
            }
        }
    }

    @FXML
    private void updateUser() {
        String name = nameTextField.getText();
        String email = emailTextField.getText();
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

        if (email.isEmpty()) {
            emailErrorLabel.setText("Email cannot be empty");
            validation = true;
        } else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            emailErrorLabel.setText("Invalid email format");
            validation = true;
        } else if (isEmailExists(email) && !email.equals(user.getEmail())) {
            emailErrorLabel.setText("Email already exists");
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
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "UPDATE users SET name = ?, email = ?, phone_number = ?, address = ?, gender = ?, role = ?, avatar = ?, birth_of_date = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phoneNumber);
            statement.setString(4, address);
            statement.setString(5, gender.name());
            statement.setString(6, role.name());
            statement.setString(7, avatarFilePath);
            statement.setDate(8, Date.valueOf(birthOfDate));
            statement.setString(9, user.getId());

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
                    manageUserController.loadUsers(null,currentPage);
                }
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
}