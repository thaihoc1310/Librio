package librio.controllers.member;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
<<<<<<< HEAD
import librio.session.Session;
=======
import librio.auth.Session;
import librio.cache.ImageCache;
import librio.controllers.admin.DeleteBookController;
>>>>>>> a51587d5162db85c9387a7d90f67b5da45fb9183
import librio.database.DatabaseConnection;
import librio.enums.Gender;
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

public class EditProfileController implements Initializable {

    private User loggedInUser =  Session.getInstance().getLoggedInUser();

    @FXML
    private TextArea addressTextArea;

    @FXML
    private Button saveButton;

    private String avatarFilePath;
    private String previousAvatarFilePath;

    @FXML
    private ImageView avatar;

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
    private AnchorPane profilePane;

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
            cropAndClipToCircle(avatarImage, avatar, 50);
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
            nameErrorLabel.setText("Name cannot be empty!");
            validation = true;
        }

        if (email.isEmpty()) {
            emailErrorLabel.setText("Email cannot be empty!");
            validation = true;
        } else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            emailErrorLabel.setText("Invalid email format!");
            validation = true;
        } else if (isEmailExists(email) && !email.equals(loggedInUser.getEmail())) {
            emailErrorLabel.setText("Email already exists!");
            validation = true;
        }

        if (phoneNumber.isEmpty()) {
            phoneNumberErrorLabel.setText("Phone number cannot be empty!");
            validation = true;
        } else if (!phoneNumber.matches("\\d{10}")) {
            phoneNumberErrorLabel.setText("Phone number must be 10 digits!");
            validation = true;
        }

        if (birthOfDate.isAfter(LocalDate.now())) {
            dateOfBirthErrorLabel.setText("Birth date cannot be after now!");
            validation = true;
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

        String query = "UPDATE users SET name = ?, email = ?, phone_number = ?, address = ?, gender = ?, avatar = ?, birth_of_date = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

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
            statement.setString(8, loggedInUser.getEmail());
            statement.setString(9, loggedInUser.getId());

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

        Image image = ImageCache.getInstance().getImage(path,avatarsDir + "Male User.png");
        cropAndClipToCircle(image, avatar, 50);

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
        memberIdTextField.setOnMouseClicked(event -> notification.setText(""));
        nameTextField.setOnMouseClicked(event -> notification.setText(""));
        emailTextField.setOnMouseClicked(event -> notification.setText(""));
        phoneNumberTextField.setOnMouseClicked(event -> notification.setText(""));
        genderComboBox.setOnMouseClicked(event -> notification.setText(""));
        birthOfDatePicker.setOnMouseClicked(event -> notification.setText(""));
        addressTextArea.setOnMouseClicked(event -> notification.setText(""));
    }

    private void hideErrorLabels() {
        nameErrorLabel.setText("");
        emailErrorLabel.setText("");
        phoneNumberErrorLabel.setText("");
        dateOfBirthErrorLabel.setText("");
    }

    @FXML
    private void openChangePasswordScene(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/Password.fxml"));
            Parent manageBorrowRoot = loader.load();

            Stage currentStage = (Stage) saveButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBorrowRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openDeleteAvatarStage() {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/DeleteAvatar.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) profilePane.getScene().getWindow();
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            DeleteAvatarController deleteAvatarController = loader.getController();
            deleteAvatarController.setAvatar(avatar);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(profilePane.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);
            });
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

