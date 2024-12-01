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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import librio.cache.ImageCache;
import librio.controllers.auth.LogoutController;
import librio.database.DatabaseConnection;
import librio.enums.Gender;
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
import static librio.util.DesignUtil.*;

/**
 * The ProfileSettingsController class manages the user interface for editing
 * and updating user profile settings. This class allows users to modify
 * personal information such as their avatar, name, email, phone number, and
 * other related details. It also provides navigation to other scenes related
 * to user management and profile settings.
 */
public class ProfileSettingsController implements Initializable {
    private final User loggedInUser = Session.getInstance().getLoggedInUser();

    @FXML
    private ImageView deleteName, deleteEmail, deletePhoneNumber, avatar, avatarUser;
    @FXML
    private TextField addressTextField, emailTextField, memberIdTextField, nameTextField, phoneNumberTextField;
    @FXML
    private Button saveButton;
    @FXML
    private Label userNameLabel, dateOfBirthErrorLabel, emailErrorLabel, nameErrorLabel, phoneNumberErrorLabel, notification;
    @FXML
    private DatePicker birthOfDatePicker;
    @FXML
    private ComboBox<Gender> genderComboBox;
    @FXML
    private StackPane stackPaneRoot;
    @FXML
    private AnchorPane namePane, emailPane, phonePane;
    private String avatarFilePath, previousAvatarFilePath;

    /**
     * Initializes the controller with the given URL and resource bundle.
     *
     * This method performs several initialization tasks, including setting the
     * avatar and user name, initializing field data, and adding necessary listeners
     * for user interaction within the user interface.
     *
     * @param url the location to resolve relative paths for the root object, or null if unknown
     * @param resourceBundle the resources used to localize the root object, or null if not applicable
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setAvatarAndUserName(avatarUser, userNameLabel);
        initFieldData();
        addListeners();
    }

    /**
     * Allows the user to change their avatar by selecting an image file from their file system.
     * Opens a file chooser dialog for selecting an image with supported extensions
     * (PNG, JPG, JPEG). If a valid file is chosen, it updates the avatar display
     * to show the new image cropped and clipped to a circle of radius 75.
     * Updates the internal file path states to reflect the newly selected avatar image.
     */
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
            cropAndClipToCircle(avatarImage, avatar, 75);
            previousAvatarFilePath = selectedFile.getAbsolutePath();
        }
    }

    /**
     * Saves the user profile information by validating input fields and updating details
     * in the database. Upon successfully updating the user profile, it also updates the
     * avatar and provides a notification message.
     *
     * The method performs the following:
     * - Validates the inputs for name, email, phone number, and date of birth fields.
     * - Checks that the email is in the correct format and does not already exist in
     *   the database unless it belongs to the logged-in user.
     * - Ensures phone numbers are 10 digits long.
     * - Validates date of birth format and ensures the date is not in the future.
     * - If validation passes, updates the user's profile information in the database.
     * - Copies the new avatar image if provided and deletes the old one.
     * - Displays appropriate error messages for validation failures.
     * - Provides a success notification after successful profile update.
     */
    @FXML
    private void save() {
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        String phoneNumber = phoneNumberTextField.getText();
        Gender gender = genderComboBox.getValue();
        String address = addressTextField != null ? addressTextField.getText() : null;
        String dateString = birthOfDatePicker.getEditor().getText();
        LocalDate birthOfDate = null;

        String dateRegex = "^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/\\d{4}$";
        boolean validation = false;

        if (!dateString.matches(dateRegex)) {
            dateOfBirthErrorLabel.setText("Invalid date format!");
            validation = true;
        } else {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                birthOfDate = LocalDate.parse(dateString, formatter);

                if (birthOfDate.isAfter(LocalDate.now())) {
                    dateOfBirthErrorLabel.setText("Birth of Date cannot be after now!");
                    validation = true;
                }
            } catch (DateTimeParseException e) {
                dateOfBirthErrorLabel.setText("Invalid date!");
                validation = true;
            }
        }

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

        if (validation) {
            return;
        }

        loggedInUser.setName(name);
        loggedInUser.setEmail(email);
        loggedInUser.setPhoneNumber(phoneNumber);
        loggedInUser.setGender(gender);
        loggedInUser.setAddress(address);
        loggedInUser.setBirthOfDate(birthOfDate);
        if (avatarFilePath != null) {
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
                hideErrorLabels();
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
                    setAvatarAndUserName(avatarUser, userNameLabel);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds listeners to various UI components to manage UI behavior and interaction.
     *
     * Listeners are registered on several TextField components to handle mouse click events,
     * text property changes, and focus changes. These components include memberIdTextField,
     * nameTextField, emailTextField, and phoneNumberTextField. Mouse click events hide error
     * and notification labels, and for non-empty fields, show corresponding delete icons
     * and apply specific border styles.
     *
     * Text property change listeners manage the visibility of delete icons based on whether
     * the text fields are empty or not. Focus change listeners reset the visibility of delete
     * icons and clear the applied border styles upon losing focus.
     *
     * Additional mouse click listeners are registered on genderComboBox, birthOfDatePicker,
     * and addressTextField to hide error and notification labels.
     */
    private void addListeners() {
        memberIdTextField.setOnMouseClicked(event -> hideErrorAndNotificationLabels());

        nameTextField.setOnMouseClicked(event -> {
            hideErrorAndNotificationLabels();
            if (!nameTextField.getText().isEmpty()) {
                deleteName.setVisible(true);
                namePane.setStyle("-fx-border-style: solid solid solid solid;\n" +
                        "-fx-border-width: 0.3px  0.3px 1px 0.3px;");
            }
        });

        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            deleteName.setVisible(!newValue.isEmpty());
        });

        nameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                deleteName.setVisible(false);
                namePane.setStyle("");
            }
        });

        emailTextField.setOnMouseClicked(event -> {
            hideErrorAndNotificationLabels();
            if (!emailTextField.getText().isEmpty()) {
                deleteEmail.setVisible(true);
                emailPane.setStyle("-fx-border-style: solid solid solid solid;\n" +
                        "-fx-border-width: 0.3px  0.3px 1px 0.3px;");

            }
        });

        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            deleteEmail.setVisible(!newValue.isEmpty());
        });

        emailTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                deleteEmail.setVisible(false);
                emailPane.setStyle("");
            }
        });

        phoneNumberTextField.setOnMouseClicked(event -> {
            hideErrorAndNotificationLabels();
            if (!phoneNumberTextField.getText().isEmpty()) {
                deletePhoneNumber.setVisible(true);
                phonePane.setStyle("-fx-border-style: solid solid solid solid;\n" +
                        "-fx-border-width: 0.3px  0.3px 1px 0.3px;");
            }
        });

        phoneNumberTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            deletePhoneNumber.setVisible(!newValue.isEmpty());
        });

        phoneNumberTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                deletePhoneNumber.setVisible(false);
                phonePane.setStyle("");
            }
        });

        genderComboBox.setOnMouseClicked(event -> {
            hideErrorAndNotificationLabels();
        });
        birthOfDatePicker.setOnMouseClicked(event -> {
            hideErrorAndNotificationLabels();
        });
        birthOfDatePicker.getEditor().setOnMouseClicked(event -> {
            hideErrorAndNotificationLabels();
        });
        addressTextField.setOnMouseClicked(event -> {
            hideErrorAndNotificationLabels();
        });
    }

    /**
     * Resets the error labels and notification message in the user interface.
     *
     * This method invokes hideErrorLabels() to clear all text from error labels,
     * indicating no errors are present. It also clears the notification label,
     * setting its text to an empty string, to remove any notification messages
     * displayed to the user.
     */
    private void hideErrorAndNotificationLabels() {
        hideErrorLabels();
        notification.setText("");
    }

    /**
     * Clears the text of error labels related to user input fields and notifications.
     * This method is typically used to reset the state of the UI by removing any
     * validation error messages or notifications that may have been previously set.
     * It targets the labels for name, email, phone number, and date of birth errors,
     * as well as any general notifications.
     */
    private void hideErrorLabels() {
        nameErrorLabel.setText("");
        emailErrorLabel.setText("");
        phoneNumberErrorLabel.setText("");
        dateOfBirthErrorLabel.setText("");
        notification.setText("");
    }

    /**
     * Opens the advertisement dashboard scene.
     * This method switches the current scene to the advertisement dashboard FXML layout.
     * It is typically triggered by a user action, such as clicking a button associated
     * with navigating to the advertisement dashboard.
     */
    @FXML
    private void openAdDashboardScene() {
        switchScene(saveButton, "/fxml/admin/AdDashboard.fxml");
    }

    /**
     * Handles the event of opening the Manage Borrow scene.
     * This method is triggered by the UI and switches the current scene
     * to the ManageBorrow.fxml view. It uses the saveButton as the reference
     * component to obtain the current window and scene for the switch.
     */
    @FXML
    private void openManageBorrowScene() {
        switchScene(saveButton, "/fxml/admin/ManageBorrow.fxml");
    }

    /**
     * Opens the Manage Book scene in the application. This method is triggered
     * when the corresponding user interface action is invoked, typically a button click.
     * It utilizes the {@code switchScene} utility method to transition from the current
     * scene to the Manage Book scene specified by the FXML path.
     *
     * The method does not take any parameters directly, but relies on the
     * {@code saveButton} as a reference for the current stage's {@code Scene}.
     * As a result, it updates the scene's root to the new layout defined in
     * "/fxml/admin/ManageBook.fxml".
     */
    @FXML
    private void openManageBookScene() {
        switchScene(saveButton, "/fxml/admin/ManageBook.fxml");
    }

    /**
     * Opens the Manage User scene in the application. This method transitions the view
     * to the Manage User interface by utilizing the {@code switchScene} method.
     * It uses the {@code saveButton} to obtain the current stage and window context
     * for the scene switch operation.
     */
    @FXML
    private void openManageUserScene() {
        switchScene(saveButton, "/fxml/admin/ManageUser.fxml");
    }

    /**
     * Opens the log out scene in a new modal window. This method loads
     * the Logout.fxml file and sets up a new stage with specified dimensions
     * and styles. It then shows the new stage as a modal dialog that blocks
     * interaction with other windows until it is closed.
     *
     * The method also retrieves the current stage, sets it as the owner of
     * the new stage, and applies certain UI properties such as opacity levels
     * and window clipping for styling.
     *
     * In case of an IOException during the loading of the FXML file, it will
     * print the stack trace of the exception.
     */
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

    /**
     * Opens the change password scene by switching the current scene to the
     * ChangePassword.fxml layout. This method is triggered via a JavaFX FXML
     * control and makes use of the switchScene utility function to transition
     * to the specified layout.
     */
    @FXML
    private void openChangePasswordScene() {
        switchScene(saveButton, "/fxml/admin/ChangePassword.fxml");
    }

    /**
     * Initializes the profile fields with the current user's data. This method sets the
     * values for the text fields, combo boxes, date pickers, and loads the user's avatar.
     * It configures event handlers for delete buttons to clear text fields and formats
     * the date picker display.
     *
     * This method involves setting the various UI components such as:
     * - Populating a combo box with gender options from an enum.
     * - Retrieving and setting user details like email, name, phone number, address, gender, and birthdate.
     * - Loading and displaying the user's avatar image while providing a default image if the specified avatar is not found.
     * - Applying a circular crop effect to the avatar image for consistent display aesthetics.
     * - Configuring the date picker to display dates in a specific format.
     * - Assigning mouse click handlers to delete buttons to clear associated text fields.
     */
    private void initFieldData() {
        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        emailTextField.setText(loggedInUser.getEmail());
        nameTextField.setText(loggedInUser.getName());
        phoneNumberTextField.setText(loggedInUser.getPhoneNumber());
        addressTextField.setText(loggedInUser.getAddress());
        genderComboBox.setValue(loggedInUser.getGender());
        birthOfDatePicker.setValue(loggedInUser.getBirthOfDate());

        nameTextField.setText(loggedInUser.getName());
        emailTextField.setText(loggedInUser.getEmail());
        phoneNumberTextField.setText(loggedInUser.getPhoneNumber());
        addressTextField.setText(loggedInUser.getAddress() != null ? loggedInUser.getAddress() : "");
        birthOfDatePicker.setValue(loggedInUser.getBirthOfDate());
        genderComboBox.setValue(loggedInUser.getGender());
        memberIdTextField.setText(String.valueOf(loggedInUser.getId()));


        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + loggedInUser.getAvatar();

        Image image = ImageCache.getInstance().getImage(path, avatarsDir + "Male User.png");
        cropAndClipToCircle(image, avatar, 75);
        setDatePickerFormat(birthOfDatePicker);
        deleteName.setOnMouseClicked(e -> deleteTextField(nameTextField));
        deleteEmail.setOnMouseClicked(e -> deleteTextField(emailTextField));
        deletePhoneNumber.setOnMouseClicked(e -> deleteTextField(phoneNumberTextField));
    }

    /**
     * Clears the content of the specified TextField.
     *
     * @param textField the TextField to be cleared
     */
    private void deleteTextField(TextField textField) {
        textField.clear();
    }
}
