package librio.controllers.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import librio.database.DatabaseConnection;
import librio.enums.Gender;
import librio.enums.Role;
import librio.session.Session;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.isEmailExists;
import static librio.util.DesignUtil.cropAndClipToCircle;
import static librio.util.DesignUtil.setDatePickerFormat;

/**
 * The CreateUserController class is responsible for managing the user
 * interface components involved in creating a new user within the application.
 * It contains methods for handling user input, validating information, and
 * interacting with the database to register new users. The class implements
 * the Initializable interface to perform necessary initializations upon loading.
 */
public class CreateUserController implements Initializable {
    @FXML
    protected TextField nameTextField;
    @FXML
    protected TextField emailTextField;
    @FXML
    protected TextField passwordTextField;
    @FXML
    protected TextField confirmPasswordTextField;
    @FXML
    protected TextField phoneNumberTextField;
    @FXML
    protected ComboBox<Gender> genderComboBox;
    @FXML
    protected ComboBox<Role> roleComboBox;
    @FXML
    protected TextArea addressTextArea;
    @FXML
    protected Button createUserButton;
    @FXML
    protected Label nameErrorLabel;
    @FXML
    protected Label emailErrorLabel;
    @FXML
    protected Label passwordErrorLabel;
    @FXML
    protected Label confirmPasswordErrorLabel;
    @FXML
    protected Label phoneNumberErrorLabel;
    @FXML
    protected Label roleErrorLabel;
    @FXML
    protected Label genderErrorLabel;
    @FXML
    private Label birthOfDateErrorLabel;
    @FXML
    private DatePicker birthOfDatePicker;
    @FXML
    private ImageView avatarImageView;

    private String avatarFilePath;

    private String previousAvatarFilePath;

    /**
     * Initializes the CreateUserController by setting up the necessary UI components
     * and their initial configurations. This includes setting items for combo boxes,
     * hiding error labels, adding event listeners, and formatting the date picker.
     *
     * @param location  The location used to resolve relative paths for the root object,
     *                  or null if the location is not known.
     * @param resources The resources used to localize the root object,
     *                  or null if the root object was not localized.
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
     * Handles the creation of a new user account by validating input fields,
     * checking for existing email entries, and inserting the new user data
     * into the database. The method also assigns roles and manages additional
     * database entries based on user roles such as MEMBER or LIBRARIAN. It
     * displays appropriate error messages if validation fails and handles
     * any SQL or IO exceptions that may occur during the process.
     *
     * Input fields validated include:
     * - Name
     * - Email
     * - Password and Confirm Password
     * - Phone Number
     * - Gender
     * - Role
     * - Birth Date
     *
     * Notes:
     * - Email must be unique and in a valid format.
     * - Password must be a minimum of 6 characters and match the confirmation field.
     * - Date of birth must be in a correct format and not in the future.
     * - The phone number should be exactly 10 digits.
     * - Gender and Role must be selected.
     *
     * If validation is successful, the user's information is inserted into the 'users'
     * table. Depending on the role, additional entries are created in the 'Members'
     * or 'Librarians' tables. If a previous avatar file path is provided, the avatar
     * is copied to the designated directory.
     */
    @FXML
    protected void createUser() {
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        String password = passwordTextField.getText();
        String confirmPassword = confirmPasswordTextField.getText();
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
        } else if (isEmailExists(email)) {
            emailErrorLabel.setText("Email already exists!");
            validation = true;
        }

        if (password.isEmpty()) {
            passwordErrorLabel.setText("Password cannot be empty!");
            validation = true;
        } else if (password.length() < 6) {
            passwordErrorLabel.setText("Password must be at least 6 chars!");
            validation = true;
        }

        if (confirmPassword.isEmpty() || !confirmPassword.equals(password)) {
            confirmPasswordErrorLabel.setText("Passwords do not match!");
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

        String query = "INSERT INTO users (name, email, password, phone_number, address, gender, role, avatar, birth_of_date, created_by, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, phoneNumber);
            statement.setString(5, address);
            statement.setString(6, gender.name());
            statement.setString(7, role.name());
            statement.setString(8, avatarFilePath);
            statement.setDate(9, Date.valueOf(birthOfDate));
            statement.setString(10, Session.getInstance().getLoggedInUser().getEmail());

            int rowsInserted = statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);
                if (role.equals(Role.MEMBER)) {
                    String insertMemberQuery = "INSERT INTO Members (id, fine_amount, total_books_borrowed) VALUES (?, ?, ?)";
                    try (PreparedStatement memberStatement = connection.prepareStatement(insertMemberQuery)) {
                        memberStatement.setInt(1, userId);
                        memberStatement.setLong(2, 0);
                        memberStatement.setLong(3, 0);
                        memberStatement.executeUpdate();
                    }
                } else if (role.equals(Role.LIBRARIAN)) {
                    String insertLibrarianQuery = "INSERT INTO Librarians (id) VALUES (?)";
                    try (PreparedStatement librarianStatement = connection.prepareStatement(insertLibrarianQuery)) {
                        librarianStatement.setInt(1, userId); // userId là id của user vừa tạo
                        librarianStatement.executeUpdate();
                    }
                }
                String projectDir = System.getProperty("user.dir");
                String avatarsDir = projectDir + "/src/main/resources/images/user/";
                if (previousAvatarFilePath != null) {
                    Files.copy(Paths.get(previousAvatarFilePath), Paths.get(avatarsDir + avatarFilePath));
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

    /**
     * Clears the text of all error labels related to user input fields.
     *
     * This method resets the error messages associated with the following
     * input fields by setting the text of their corresponding error labels
     * to an empty string:
     * - Name
     * - Email
     * - Password
     * - Confirm Password
     * - Phone Number
     * - Role
     * - Gender
     * - Date of Birth
     *
     * This is typically used to hide error messages after handling input
     * validation or when resetting the form.
     */
    private void hideErrorLabels() {
        nameErrorLabel.setText("");
        emailErrorLabel.setText("");
        passwordErrorLabel.setText("");
        confirmPasswordErrorLabel.setText("");
        phoneNumberErrorLabel.setText("");
        roleErrorLabel.setText("");
        genderErrorLabel.setText("");
        birthOfDateErrorLabel.setText("");
    }

    /**
     * Adds mouse click event listeners to various input fields and controls.
     * Each listener, when triggered, will invoke the `hideErrorLabels` method
     * to clear any displayed error messages.
     *
     * This method targets the following elements:
     * - nameTextField
     * - emailTextField
     * - passwordTextField
     * - confirmPasswordTextField
     * - phoneNumberTextField
     * - addressTextArea
     * - birthOfDatePicker
     * - birthOfDatePicker's editor
     * - genderComboBox
     * - roleComboBox
     */
    private void addListeners() {
        // Name validation
        nameTextField.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        emailTextField.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        passwordTextField.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        confirmPasswordTextField.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        phoneNumberTextField.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        addressTextArea.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        birthOfDatePicker.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        birthOfDatePicker.getEditor().setOnMouseClicked(event -> {
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
     * Handles the action of adding an avatar image for the user.
     * This method opens a file chooser dialog allowing the user to select an image file
     * with extensions .png, .jpg, or .jpeg. Upon selection, the chosen image is processed
     * to be displayed as a circular cropped image in the designated avatar image view.
     * It also updates the file paths for the selected avatar and the previous avatar.
     *
     * The method begins by hiding any existing error labels, then initializes the file chooser
     * with the title "Choose avatar" and sets the appropriate image file extension filters.
     * If the user selects a file, the method generates a unique file path by appending the
     * current timestamp to the file name. It then processes the selected image by converting
     * its URI to an Image object, and calls the cropAndClipToCircle method to crop the image
     * to a circle with a specified radius before displaying it in the avatarImageView.
     * The method also stores the absolute path of the selected file to keep track of the previous avatar file.
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
            cropAndClipToCircle(avatarImage, avatarImageView, 75);
            previousAvatarFilePath = selectedFile.getAbsolutePath();
        }
    }

    /**
     * Handles the cancellation of the user creation process.
     * This method clears all input fields to reset the form and closes the stage
     * associated with the 'Create User' functionality, effectively aborting the
     * user creation operation.
     */
    @FXML
    private void cancelCreateUser() {
        clearInputFields();
        closeStage();
    }

    /**
     * Closes the current stage of the application. This is typically used to close
     * the window containing the UI elements managed by this controller.
     */
    private void closeStage() {
        Stage stage = (Stage) createUserButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Clears the input fields in the user creation form.
     *
     * This method resets all text fields, combo boxes, and other input
     * elements to their default state. Specifically, it performs the
     * following operations:
     * - Clears the text from the name, email, password, confirm password,
     *   phone number text fields, and address text area.
     * - Clears the selection in the gender and role combo boxes.
     * - Sets the birth date picker value to null.
     */
    private void clearInputFields() {
        nameTextField.clear();
        emailTextField.clear();
        passwordTextField.clear();
        confirmPasswordTextField.clear();
        phoneNumberTextField.clear();
        addressTextArea.clear();
        genderComboBox.getSelectionModel().clearSelection();
        roleComboBox.getSelectionModel().clearSelection();
        birthOfDatePicker.setValue(null);

    }
}
