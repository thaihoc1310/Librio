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
import librio.cache.ImageCache;
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
import static librio.util.DesignUtil.cropAndClipToCircle;
import static librio.util.DesignUtil.setDatePickerFormat;

/**
 * The EditProfileController class manages the user interface and logic for editing
 * a user's profile within the application. It includes functionality for updating
 * user information, changing the avatar, and handling profile-related actions such as
 * saving changes and opening auxiliary dialog stages.
 *
 * This controller handles various UI components such as text fields, buttons, and combo boxes
 * to facilitate user interaction in modifying their personal information.
 *
 * Fields:
 * - loggedInUser: Represents the currently logged-in user whose profile is being edited.
 * - addressTextArea: Text area for inputting and displaying the user's address.
 * - emailTextField: Text field for inputting and validating the user's email address.
 * - memberIdTextField: Displays the user's unique member ID.
 * - nameTextField: Text field for entering the user's full name.
 * - phoneNumberTextField: Text field for inputting and validating the user's phone number.
 * - saveButton: Button to trigger the save operation for the edited profile.
 * - avatar: ImageView component representing the user's profile picture.
 * - birthOfDatePicker: Date picker for selecting and displaying the user's birth date.
 * - dateOfBirthErrorLabel: Label for displaying errors related to the birth date input.
 * - emailErrorLabel: Label for displaying errors related to the email input.
 * - nameErrorLabel: Label for displaying errors related to the name input.
 * - phoneNumberErrorLabel: Label for displaying errors related to the phone number input.
 * - notification: Label for displaying success or error notifications.
 * - genderComboBox: Combo box for selecting and displaying the user's gender.
 * - profilePane: AnchorPane containing the profile editing UI components.
 * - avatarFilePath: Path to the current avatar file.
 * - previousAvatarFilePath: Path to the previous avatar file for restoration purposes.
 *
 * Methods:
 * - initialize(URL url, ResourceBundle resourceBundle): Initializes the controller with necessary user information and UI component setup.
 * - changeAvatar(): Handles changing the user's avatar by opening a file chooser and processing the selected image file.
 * - save(): Saves modified user profile information into the database after validation.
 * - setMemberInformation(): Populates member information fields with data from the logged-in user.
 * - addListeners(): Adds event listeners to UI components to handle interactions and manage visibility of error/notification labels.
 * - hideErrorLabels(): Resets the error labels for user input fields.
 * - hideErrorAndNotificationLabels(): Hides error messages and notifications in the form.
 * - openChangePasswordScene(): Opens the change password scene using the "Password.fxml" layout.
 * - openDeleteAvatarStage(): Opens a modal stage for deleting the user's avatar with visual effects.
 * - cancel(): Closes the current stage associated with the profilePane.
 */
public class EditProfileController implements Initializable {
    private final User loggedInUser = Session.getInstance().getLoggedInUser();

    @FXML
    private TextField addressTextArea, emailTextField, memberIdTextField, nameTextField, phoneNumberTextField;
    @FXML
    private Button saveButton;
    @FXML
    private ImageView avatar;
    @FXML
    private DatePicker birthOfDatePicker;
    @FXML
    private Label dateOfBirthErrorLabel, emailErrorLabel, nameErrorLabel, phoneNumberErrorLabel, notification;
    @FXML
    private ComboBox<Gender> genderComboBox;
    @FXML
    private AnchorPane profilePane;

    private String avatarFilePath;

    private String previousAvatarFilePath;

    /**
     * Initializes the EditProfileController with necessary user information and UI component setup.
     *
     * @param url the location used to resolve relative paths for the root object, or null if the location is not known
     * @param resourceBundle the resources used to localize the root object, or null if no localization is required
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (loggedInUser == null) {
            return;
        }
        hideErrorLabels();
        addListeners();
        setMemberInformation();
    }

    /**
     * Handles the action of changing the user's avatar by opening a file chooser dialog.
     * Allows the user to select an image file with specified extensions (PNG, JPG, JPEG)
     * as the new avatar, and processes the file if selected.
     *
     * Once a file is selected, the method generates a unique file path for the avatar,
     * creates an Image from the selected file, and applies a circular crop and clip to the avatar image view.
     * Updates the previous avatar file path with the selected file's absolute path.
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
            cropAndClipToCircle(avatarImage, avatar, 50);
            previousAvatarFilePath = selectedFile.getAbsolutePath();
        }
    }

    /**
     * Saves the modified user profile information into the database.
     *
     * This method performs several validation checks on the user input fields including
     * name, email, phone number, gender, address, and birth date. If any validation rule
     * is violated, appropriate error messages are displayed and the method exits without
     * saving changes.
     *
     * If all inputs are valid, the method updates the logged-in user's details and executes
     * an SQL update statement to store these changes in the database. In case of a successful
     * update, it also handles the file operations related to changing the user's avatar.
     *
     * - Validates and sets the user's name, email, phone number, gender, address, and birth date.
     * - Ensures the email format is correct and checks for duplicates against existing records.
     * - Confirms that the phone number is a 10-digit numeric value.
     * - Parses and verifies the birth date ensuring it is in a valid format and not set in the future.
     * - Updates the user's profile in the database and manages avatar file changes.
     * - Displays a success notification if the profile is updated without errors.
     *
     * If an error occurs during database operations or file handling, appropriate exceptions are caught
     * and managed.
     */
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
            cancel();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Populates the member information fields with data from the logged-in user.
     *
     * This method fetches and displays the user's avatar, name, email, phone number,
     * address, birth date, gender, and ID in the respective UI components.
     * It supports showing a default avatar if the user's avatar is not available.
     *
     * It processes the avatar image to ensure it is displayed in a circular format
     * and sets the date picker with the appropriate format.
     *
     * Field assignments are as follows:
     * - Avatar: Retrieves and processes the image using an image cache. Defaults to "Male User.png" if unavailable.
     * - Name: Sets the logged-in user's name in the name text field.
     * - Email: Sets the logged-in user's email in the email text field.
     * - Phone Number: Sets the logged-in user's phone number in the phone number text field.
     * - Address: Sets the logged-in user's address in the address text area, or an empty string if null.
     * - Birth Date: Sets the logged-in user's birth date in the date picker.
     * - Gender: Populates the gender combo box with available gender options and sets the current value.
     * - Member ID: Sets the logged-in user's ID in the member ID text field.
     * - Formats the date picker to use the specified date format.
     */
    private void setMemberInformation() {
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + loggedInUser.getAvatar();
        File file = new File(path);
        if(!file.exists()){
            path = avatarsDir + "Male User.png";
        }
        Image image = ImageCache.getInstance().getImage(path, path);
        cropAndClipToCircle(image, avatar, 50);
        nameTextField.setText(loggedInUser.getName());
        emailTextField.setText(loggedInUser.getEmail());
        phoneNumberTextField.setText(loggedInUser.getPhoneNumber());
        addressTextArea.setText(loggedInUser.getAddress() != null ? loggedInUser.getAddress() : "");
        birthOfDatePicker.setValue(loggedInUser.getBirthOfDate());
        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        genderComboBox.setValue(loggedInUser.getGender());
        memberIdTextField.setText(String.valueOf(loggedInUser.getId()));
        setDatePickerFormat(birthOfDatePicker);
    }

    /**
     * Adds event listeners to various UI components in the edit profile form.
     *
     * These listeners are set on mouse click for each specified component, triggering
     * the `hideErrorAndNotificationLabels` method. This ensures that error and notification labels
     * are hidden when the user interacts with any of these components: memberIdTextField, nameTextField,
     * emailTextField, phoneNumberTextField, genderComboBox, birthOfDatePicker, birthOfDatePicker editor,
     * and addressTextArea.
     */
    private void addListeners() {
        memberIdTextField.setOnMouseClicked(event -> hideErrorAndNotificationLabels());
        nameTextField.setOnMouseClicked(event -> hideErrorAndNotificationLabels());
        emailTextField.setOnMouseClicked(event -> hideErrorAndNotificationLabels());
        phoneNumberTextField.setOnMouseClicked(event -> hideErrorAndNotificationLabels());
        genderComboBox.setOnMouseClicked(event -> hideErrorAndNotificationLabels());
        birthOfDatePicker.setOnMouseClicked(event -> hideErrorAndNotificationLabels());
        birthOfDatePicker.getEditor().setOnMouseClicked(event -> hideErrorAndNotificationLabels());
        addressTextArea.setOnMouseClicked(event -> hideErrorAndNotificationLabels());
    }

    /**
     * Resets the error labels associated with user input fields by clearing their text.
     *
     * This method is used to clear any existing error messages associated with the
     * name, email, phone number, and date of birth input fields.
     * It ensures that the labels for these fields do not display any prior validation
     * errors, thus preparing them for a new validation process.
     */
    private void hideErrorLabels() {
        nameErrorLabel.setText("");
        emailErrorLabel.setText("");
        phoneNumberErrorLabel.setText("");
        dateOfBirthErrorLabel.setText("");
    }

    /**
     * Hides the error and notification labels in the edit profile form.
     *
     * This method clears the text of any error messages by invoking the
     * hideErrorLabels method, which resets error labels associated with user input fields.
     * Additionally, it clears the notification text, effectively removing any
     * displayed notifications from the user interface. This method is typically
     * called when the user interacts with specific UI components in the form.
     */
    private void hideErrorAndNotificationLabels() {
        hideErrorLabels();
        notification.setText("");
    }

    /**
     * Opens the change password scene by loading the FXML file associated with the password
     * change interface and setting it as the root of the current scene.
     *
     * This method uses the FXMLLoader to load the layout defined in the "Password.fxml" file
     * located in the "/fxml/member/" directory. Once the layout is loaded, it replaces the
     * current scene's root with the new layout, effectively navigating the user to the change
     * password interface.
     *
     * If an IOException occurs during the loading of the FXML file, the method will catch the
     * exception and print its stack trace to help with debugging.
     */
    @FXML
    private void openChangePasswordScene() {
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

    /**
     * Opens a modal stage that handles the deletion of the user's avatar.
     *
     * This method accomplishes the following:
     * - Loads the DeleteAvatar.fxml layout using FXMLLoader.
     * - Adjusts the brightness of the current stage to provide a visual effect indicating
     *   that the main window is inactive while the delete avatar stage is displayed.
     * - Sets up a new stage to display the delete avatar interface, making it non-resizable
     *   and having it inherit the modality from the current window, ensuring that it acts as a modal dialog.
     * - Initializes the DeleteAvatarController with the current avatar.
     * - Sets up event handling to restore the original brightness of the current stage once
     *   the delete avatar stage is closed.
     *
     * The method includes exception handling for IOExceptions that may be thrown during the
     */
    @FXML
    private void openDeleteAvatarStage() {
        try {
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

    /**
     * Closes the current stage associated with the profilePane.
     *
     * This method retrieves the current window (stage) from the profilePane's scene
     * and closes it, effectively terminating the current view or dialog. It is typically
     * used when a cancel action is performed, such as when a user chooses to exit without
     * saving changes.
     */
    @FXML
    private void cancel() {
        Stage stage = (Stage) profilePane.getScene().getWindow();
        stage.close();
    }
}

