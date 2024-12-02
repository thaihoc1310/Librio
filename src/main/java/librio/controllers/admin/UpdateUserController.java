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


/**
 * The UpdateUserController class is responsible for handling the user interface and logic
 * associated with updating user information in an application. It provides users with the
 * ability to update profile information such as their name, email, phone number, address,
 * gender, role, birth date, and avatar image. This class ensures the data is correctly
 * validated and updates are propagated to the database.
 *
 * Implements Initializable interface to perform the initialization logic after the root
 * element has been fully processed.
 */
public class UpdateUserController implements Initializable {
    @FXML
    protected Button updateUserButton;
    @FXML
    protected TextField nameTextField;
    @FXML
    protected TextField emailTextField;
    @FXML
    protected TextField phoneNumberTextField;
    @FXML
    protected ComboBox<Gender> genderComboBox;
    @FXML
    protected ComboBox<Role> roleComboBox;
    @FXML
    protected TextArea addressTextArea;
    @FXML
    protected Label nameErrorLabel;
    @FXML
    protected Label emailErrorLabel;
    @FXML
    protected Label phoneNumberErrorLabel;
    @FXML
    protected Label roleErrorLabel;
    @FXML
    protected Label genderErrorLabel;
    @FXML
    protected Label birthOfDateErrorLabel;
    @FXML
    protected DatePicker birthOfDatePicker;
    @FXML
    protected ImageView avatarImageView;

    private String avatarFilePath;

    private String previousAvatarFilePath;

    private User user;

    /**
     * Initializes the controller after its root element has been completely processed.
     * This method sets up the ComboBoxes for gender and role with their respective values,
     * hides error labels, adds necessary listeners for UI components, and sets
     * the date picker format for the birth date.
     *
     * @param location The location used to resolve relative paths for the root object,
     * or null if the location is not known.
     * @param resources The resources used to localize the root object,
     * or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
        hideErrorLabels();
        addListeners();
        setDatePickerFormat(birthOfDatePicker);
    }

    /**
     * Sets the current user to be managed by the controller and populates the UI fields
     * with the user's data.
     *
     * @param user the User object containing the information to be displayed
     */
    public void setUser(User user) {
        this.user = user;
        populateFields();
    }

    /**
     * Populates the UI fields with the current user's data. This method updates text fields,
     * combo boxes, and date pickers with information retrieved from the user object, such as
     * email, name, phone number, address, gender, role, birthday, and avatar. If the user is
     * not set, this method has no effect.
     *
     * The method also handles the loading and displaying of the user's avatar image. It
     * constructs the complete file path for the avatar, retrieves the image using an
     * image cache, and manipulates the image to fit within a circular boundary.
     *
     * Note: This method should only be called if the user object is initialized.
     */
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

    /**
     * Updates the user information in the database based on the current state of the UI form fields.
     * This method first validates the input fields to ensure they are correctly filled according to
     * specified rules, such as checking non-empty fields and ensuring correct formats for email,
     * phone number, and date of birth. If validation fails, corresponding error messages are displayed
     * on the UI and the update process is halted.
     *
     * If the inputs are valid, it attempts to update the user's information in the database.
     * The method also handles special cases, such as changes in user roles (between MEMBER and LIBRARIAN),
     * updating their roles in respective tables, and managing avatar file changes by deleting old files
     * and copying new ones.
     *
     * Handles potential SQL exceptions and IO exceptions during database updates and file operations, respectively.
     */
    @FXML
    protected void updateUser() {
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

    /**
     * Clears all error labels associated with the user input fields in the UI.
     *
     * This method sets the text of each error label to an empty string, effectively
     * hiding any error messages currently displayed. It is typically used to reset
     * the UI state before performing validations or updates.
     */
    private void hideErrorLabels() {
        nameErrorLabel.setText("");
        emailErrorLabel.setText("");
        phoneNumberErrorLabel.setText("");
        roleErrorLabel.setText("");
        genderErrorLabel.setText("");
        birthOfDateErrorLabel.setText("");
    }

    /**
     * Adds mouse click event listeners to various user interface components.
     * When any of the text fields, date picker, text area, or combo boxes are clicked,
     * this method is triggered to hide error labels related to user input fields.
     * This interaction helps ensure that error messages from any invalid inputs
     * are cleared once the user focuses on these components for correction.
     */
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

    /**
     * Opens a file chooser dialog to allow the user to select an image file to be used as an avatar.
     * Supported image formats are PNG, JPG, and JPEG. Once a file is selected, the image is processed
     * and displayed in a circular format within an ImageView component.
     *
     * This method performs the following steps:
     * 1. Hides any error labels that might be visible on the UI.
     * 2. Opens a file chooser dialog for the user to select an image file.
     * 3. If a valid file is selected, constructs a unique file path for the avatar using
     *    the current system time and the file's original name.
     * 4. Loads the selected image and applies circular cropping and clipping before displaying it
     *    in the designated ImageView.
     * 5. Updates the previous avatar file path with the absolute path of the newly selected image.
     */
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

    /**
     * Cancels the update user operation and closes the current window.
     *
     * This method is typically used to dismiss the update user interface without
     * saving any changes made by the user. It ensures that any modifications to the
     * user data are not persisted, returning the system to its prior state.
     *
     * The method works by invoking the closeStage() method, which handles the
     * actual logic of closing the window.
     */
    @FXML
    private void cancelUpdateUser() {
        closeStage();
    }

    /**
     * Closes the current stage associated with the update user interface.
     *
     * This method retrieves the window (stage) in which the updateUserButton resides
     * and closes it. It is typically used to terminate the current window after
     * an update operation has been completed or cancelled.
     */
    private void closeStage() {
        Stage stage = (Stage) updateUserButton.getScene().getWindow();
        stage.close();
    }
}