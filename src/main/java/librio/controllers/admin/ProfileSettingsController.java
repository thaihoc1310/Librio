package librio.controllers.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import librio.controllers.LogoutController;
import librio.auth.Session;
import librio.database.DatabaseConnection;
import librio.models.Gender;
import librio.models.User;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.isEmailExists;
import static librio.util.DesignUtil.cropAndClipToCircle;

public class ProfileSettingsController implements Initializable {

    private User loggedInUser =  Session.getInstance().getLoggedInUser();

    @FXML
    private TextArea addressTextArea;

    @FXML
    private Button saveButton;

    @FXML
    private ImageView avatar;
    private String avatarFilePath;
    private String previousAvatarFilePath;

    @FXML
    private Label userNameLabel;
    @FXML
    private StackPane stackPaneRoot;
    @FXML
    private ImageView avatarUser;

    @FXML
    private DatePicker birthOfDatePicker;

    @FXML
    private Label dateOfBirthErrorLabel;

    @FXML
    private Label emailErrorLabel;

    @FXML
    private TextField emailTextField;

    @FXML
    private ComboBox<Gender> genderComboBox;

    @FXML
    private TextField memberIdTextField;

    @FXML
    private Label nameErrorLabel;

    @FXML
    private TextField nameTextField;

    @FXML
    private Label phoneNumberErrorLabel;

    @FXML
    private TextField phoneNumberTextField;

    @FXML
    private Label notification;

    @FXML
    void changeAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose avatar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            avatarFilePath = System.currentTimeMillis() + "_" + selectedFile.getName();
            Image avatarImage = new Image(selectedFile.toURI().toString());
            cropAndClipToCircle(avatarImage, avatar, 90);
            previousAvatarFilePath = selectedFile.getAbsolutePath();
        }
    }

    @FXML
    private void save() {
        if (loggedInUser == null) {
            return;
        }
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        String phoneNumber = phoneNumberTextField.getText();
        Gender gender = genderComboBox.getValue();
        String address = addressTextArea != null ? addressTextArea.getText() : null;
        LocalDate birthOfDate = birthOfDatePicker.getValue();
        boolean validation = false;

        if (name.isEmpty()) {
            nameErrorLabel.setText("Name cannot be empty");
            validation = true;
        }

        if (email.isEmpty()) {
            emailErrorLabel.setText("Email cannot be empty");
            validation = true;
        } else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            emailErrorLabel.setText("Invalid email format");
            validation = true;
        } else if (isEmailExists(email) && !email.equals(loggedInUser.getEmail())) {
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

        if (birthOfDate.isAfter(LocalDate.now())) {
            dateOfBirthErrorLabel.setText("Birth of Date must not be after today");
        }

        if (validation) {
            return;
        }

        loggedInUser.setName(name);
        loggedInUser.setEmail(email);
        loggedInUser.setPhoneNumber(phoneNumber);
        loggedInUser.setGender(gender);
        loggedInUser.setAddress(address);
        loggedInUser.setBirthOfDate(birthOfDate);
        if(avatarFilePath != null){
            loggedInUser.setAvatar(avatarFilePath);
        }

        String query = "UPDATE users SET name = ?, email = ?, phone_number = ?, address = ?, gender = ?, avatar = ?, birth_of_date = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phoneNumber);
            statement.setString(4, address);
            statement.setString(5, gender.name());
            statement.setString(6, avatarFilePath != null ? avatarFilePath : loggedInUser.getAvatar());
            assert birthOfDate != null;
            statement.setDate(7, Date.valueOf(birthOfDate));
            statement.setString(8, loggedInUser.getId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                notification.setText("Profile updated successfully!");
                if (previousAvatarFilePath != null && avatarFilePath != null) {
                    String projectDir = System.getProperty("user.dir");
                    String avatarsDir = projectDir + "/src/main/resources/images/user/";
                    if (loggedInUser.getAvatar() != null && !loggedInUser.getAvatar().isEmpty()) {
                        File oldFile = new File(avatarsDir + loggedInUser.getAvatar());
                        if (oldFile.exists()) {
                            boolean deleted = oldFile.delete();
                            if (!deleted) {
                                System.out.println("Không thể xóa tệp ảnh cũ: " + oldFile.getAbsolutePath());
                            }
                        }
                    }
                    Files.copy(Paths.get(previousAvatarFilePath), Paths.get(avatarsDir + avatarFilePath));
                    setAvatarAndUserName();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(loggedInUser == null) {
            return;
        }
        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        setAvatarAndUserName();
        emailTextField.setText(loggedInUser.getEmail());
        nameTextField.setText(loggedInUser.getName());
        phoneNumberTextField.setText(loggedInUser.getPhoneNumber());
        addressTextArea.setText(loggedInUser.getAddress());
        genderComboBox.setValue(loggedInUser.getGender());
        birthOfDatePicker.setValue(loggedInUser.getBirthOfDate());

        // Lấy đường dẫn ảnh từ project
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + loggedInUser.getAvatar();

        // Chuyển đổi đường dẫn thành URL
        File file = new File(path);
        if (file.exists()) {
            Image image = new Image(file.toURI().toString()); // Chuyển đổi file thành URL hợp lệ
            cropAndClipToCircle(image, avatar, 90);
        } else {
            String defaultImage = avatarsDir + "Male User.png";
            File defaultImageFile = new File(defaultImage);
            Image image = new Image(defaultImageFile.toURI().toString()); // Chuyển đổi file thành URL hợp lệ
            cropAndClipToCircle(image, avatar, 90);
        }

        nameTextField.setText(loggedInUser.getName());
        emailTextField.setText(loggedInUser.getEmail());
        phoneNumberTextField.setText(loggedInUser.getPhoneNumber());
        addressTextArea.setText(loggedInUser.getAddress() != null ? loggedInUser.getAddress() : "");
        birthOfDatePicker.setValue(loggedInUser.getBirthOfDate());
        genderComboBox.setValue(loggedInUser.getGender());
        memberIdTextField.setText(String.valueOf(loggedInUser.getId()));
        hideErrorLabels();
        addListeners();
    }


    private void addListeners() {
        // Ẩn notification khi click vào 1 textField nào đó
        memberIdTextField.setOnMouseClicked(event -> notification.setText(""));
        nameTextField.setOnMouseClicked(event -> notification.setText(""));
        emailTextField.setOnMouseClicked(event -> notification.setText(""));
        phoneNumberTextField.setOnMouseClicked(event -> notification.setText(""));
        genderComboBox.setOnMouseClicked(event -> notification.setText(""));
        birthOfDatePicker.setOnMouseClicked(event -> notification.setText(""));
        addressTextArea.setOnMouseClicked(event -> notification.setText(""));

        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.trim().isEmpty()) {
                nameErrorLabel.setText("Name must not be empty!");
            }else{
                nameErrorLabel.setText("");
            }
        });

        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                emailErrorLabel.setText("Email cannot be empty");
            } else if (!newValue.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
                emailErrorLabel.setText("Invalid email format");
            }  else {
                emailErrorLabel.setText("");
            }
        });

        phoneNumberTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                phoneNumberErrorLabel.setText("Phone number cannot be empty");
            } else if (!newValue.matches("\\d{10}")) {
                phoneNumberErrorLabel.setText("Phone number must be 10 digits");
            } else {
                phoneNumberErrorLabel.setText("");
            }
        });

        birthOfDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.isAfter(LocalDate.now())) {
                dateOfBirthErrorLabel.setText("Date of birth must be after now");
            }else{
                dateOfBirthErrorLabel.setText("");
            }
        });
    }

    private void hideErrorLabels() {
        nameErrorLabel.setText("");
        emailErrorLabel.setText("");
        phoneNumberErrorLabel.setText("");
        dateOfBirthErrorLabel.setText("");
    }

    public void setAvatarAndUserName(){
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + loggedInUser.getAvatar();

        File file = new File(path);
        if (file.exists()) {
            Image image = new Image(file.toURI().toString());
            cropAndClipToCircle(image, avatarUser, 38.5);
            cropAndClipToCircle(image, avatar, 100);
        } else {
            String defaultImage = avatarsDir + "Male User.png";
            File defaultImageFile = new File(defaultImage);
            Image image = new Image(defaultImageFile.toURI().toString());
            cropAndClipToCircle(image, avatarUser, 38.5);
            cropAndClipToCircle(image, avatar, 100);
        }
        userNameLabel.setText(loggedInUser.getName());
    }

    @FXML
    private void openAdDashboardScene() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdDashboard.fxml"));
            Parent adminDashboardRoot  = loader.load();

            Stage currentStage = (Stage) saveButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(adminDashboardRoot);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    private void openManageBorrowScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageBorrow.fxml"));
            Parent manageBorrowRoot = loader.load();

            Stage currentStage = (Stage) saveButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBorrowRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openManageBookScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageBook.fxml"));
            Parent manageBookRoot = loader.load();

            Stage currentStage = (Stage) saveButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBookRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openManageUserScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageUser.fxml"));
            Parent manageBookRoot = loader.load();

            Stage currentStage = (Stage) saveButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBookRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openLogOutScene() {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Logout.fxml"));
            Parent root = loader.load();
            stackPaneRoot.setOpacity(0.45);
            Stage currentStage = (Stage) saveButton.getScene().getWindow();

            LogoutController logoutController = loader.getController();
            logoutController.setOwnerStage(currentStage);
            logoutController.setStackPaneRoot(stackPaneRoot);
            // Tạo stage mới cho scene
            Stage stage = new Stage();
            stage.setTitle("Logout");
            stage.setScene(new Scene(root));
            Rectangle clip = new Rectangle();
            clip.setWidth(424);
            clip.setHeight(204);
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            root.setClip(clip);
            stage.setResizable(false);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initOwner(currentStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnShown(event -> {
                stage.setX(currentStage.getX() + (currentStage.getWidth() - stage.getWidth()) / 2);
                stage.setY(currentStage.getY() + (currentStage.getHeight() - stage.getHeight()) / 2);
            });

            // Hiển thị scene
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openChangePasswordScene(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ChangePassword.fxml"));
            Parent manageBorrowRoot = loader.load();

            Stage currentStage = (Stage) saveButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBorrowRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
