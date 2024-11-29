package librio.controllers.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import librio.cache.ImageCache;
import librio.database.DatabaseConnection;
import librio.enums.Gender;
import librio.enums.Role;
import librio.models.User;
import librio.session.Session;

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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.isEmailExists;
import static librio.util.DesignUtil.cropAndClipToCircle;
import static librio.util.DesignUtil.setDatePickerFormat;


public class UpdateUserController implements Initializable {
    @FXML
    private Button updateUserButton;
    @FXML
    private TextField nameTextField, emailTextField, phoneNumberTextField;
    @FXML
    private ComboBox<Gender> genderComboBox;
    @FXML
    private ComboBox<Role> roleComboBox;
    @FXML
    private TextArea addressTextArea;
    @FXML
    private Label nameErrorLabel, emailErrorLabel, phoneNumberErrorLabel, roleErrorLabel, genderErrorLabel, birthOfDateErrorLabel;
    @FXML
    private DatePicker birthOfDatePicker;
    @FXML
    private ImageView avatarImageView;

    private String avatarFilePath;

    private String previousAvatarFilePath;

    private User user;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
        hideErrorLabels();
        addListeners();
        setDatePickerFormat(birthOfDatePicker);
    }

    public void setUser(User user) {
        this.user = user;
        populateFields();
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

            String projectDir = System.getProperty("user.dir");
            String avatarsDir = projectDir + "/src/main/resources/images/user/";
            String path = avatarsDir + user.getAvatar();

            Image image = ImageCache.getInstance().getImage(path, avatarsDir + "Male User.png");
            cropAndClipToCircle(image, avatarImageView, 55);
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
        String dateString = birthOfDatePicker.getEditor().getText();
        LocalDate birthOfDate = null;

        String dateRegex = "^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/\\d{4}$";
        boolean validation = false;

        if (!dateString.matches(dateRegex)) {
            birthOfDateErrorLabel.setText("Invalid date format!");
            validation = true;
        } else {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                birthOfDate = LocalDate.parse(dateString, formatter);

                if (birthOfDate.isAfter(LocalDate.now())) {
                    birthOfDateErrorLabel.setText("Birth of Date cannot be after now!");
                    validation = true;
                }
            } catch (DateTimeParseException e) {
                birthOfDateErrorLabel.setText("Invalid date!");
                validation = true;
            }
        }

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
        } else if (isEmailExists(email) && !email.equals(user.getEmail())) {
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

        if (role == null) {
            roleErrorLabel.setText("Role must be selected!");
            validation = true;
        }

        if (gender == null) {
            genderErrorLabel.setText("Gender must be selected!");
            validation = true;
        }

        if (validation) {
            return;
        }
        String query = "UPDATE users SET name = ?, email = ?, phone_number = ?, address = ?, " +
                "gender = ?, role = ?, avatar = ?, birth_of_date = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phoneNumber);
            statement.setString(4, address);
            statement.setString(5, gender.name());
            statement.setString(6, role.name());
            statement.setString(7, avatarFilePath != null ? avatarFilePath : user.getAvatar());
            assert birthOfDate != null;
            statement.setDate(8, Date.valueOf(birthOfDate));
            statement.setString(9, Session.getInstance().getLoggedInUser().getEmail());
            statement.setString(10, user.getId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                if (!role.equals(user.getRole())) {
                    if (user.getRole().equals(Role.MEMBER)) {
                        String deleteMemberQuery = "DELETE FROM Members WHERE id = ?";
                        try (PreparedStatement deleteMemberStatement = connection.prepareStatement(deleteMemberQuery)) {
                            deleteMemberStatement.setString(1, user.getId());
                            deleteMemberStatement.executeUpdate();
                        }

                        String insertLibrarianQuery = "INSERT INTO Librarians (id) VALUES (?)";
                        try (PreparedStatement insertLibrarianStatement = connection.prepareStatement(insertLibrarianQuery)) {
                            insertLibrarianStatement.setString(1, user.getId());
                            insertLibrarianStatement.executeUpdate();
                        }

                    } else if (user.getRole().equals(Role.LIBRARIAN)) {
                        String deleteLibrarianQuery = "DELETE FROM Librarians WHERE id = ?";
                        try (PreparedStatement deleteLibrarianStatement = connection.prepareStatement(deleteLibrarianQuery)) {
                            deleteLibrarianStatement.setString(1, user.getId());
                            deleteLibrarianStatement.executeUpdate();
                        }

                        String insertMemberQuery = "INSERT INTO Members (id, fine_amount, total_books_borrowed) VALUES (?, ?, ?)";
                        try (PreparedStatement insertMemberStatement = connection.prepareStatement(insertMemberQuery)) {
                            insertMemberStatement.setString(1, user.getId());
                            insertMemberStatement.setLong(2, 0);
                            insertMemberStatement.setLong(3, 0);
                            insertMemberStatement.executeUpdate();
                        }
                    }
                }

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
        nameTextField.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        emailTextField.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        phoneNumberTextField.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        birthOfDatePicker.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        addressTextArea.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        genderComboBox.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        roleComboBox.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
    }

    @FXML
    private void addAvatar() {
        hideErrorLabels();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose avatar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            avatarFilePath = System.currentTimeMillis() + "_" + selectedFile.getName();
            Image avatarImage = new Image(selectedFile.toURI().toString());
            cropAndClipToCircle(avatarImage, avatarImageView, 55);
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
}