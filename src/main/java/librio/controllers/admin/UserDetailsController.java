package librio.controllers.admin;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import librio.cache.ImageCache;
import librio.models.User;
import librio.session.Session;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static librio.util.DesignUtil.cropAndClipToCircle;

/**
 * The UserDetailsController class manages the user interface for displaying user details.
 * It implements the Initializable interface to initialize data and UI components.
 * The class is responsible for setting, updating, and handling the display of user information.
 */
public class UserDetailsController implements Initializable {
    @FXML
    private TextField emailTextField, nameTextField, phoneNumberTextField, userIDTextField,
            genderTextField, roleTextField, bodTextField;

    @FXML
    private TextArea addressTextArea;

    @FXML
    private ImageView avatarImageView;

    @FXML
    private Button backButton;

    private User user;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    /**
     * Sets the user details in the controller and populates the UI fields with the user's data.
     *
     * @param user the User object containing the details to be displayed in the UI
     */
    public void setUser(User user) {
        this.user = user;
        populateFields();
    }

    /**
     * Populates the UI fields with data from the user object, if the user is not null.
     * This method updates various text fields and an image view with the user's details.
     * It sets the text for user ID, email, name, phone number, address, gender, role,
     * and birth date based on the information contained in the user object.
     * Additionally, it loads and displays the user's avatar image, defaulting to a male user image
     * if the specified avatar is unavailable. The loaded image is cropped and clipped into a circle
     * for display in the specified image view.
     * The method makes use of an image cache to optimize image loading.
     */
    private void populateFields() {
        if (user != null) {
            userIDTextField.setText(user.getId());
            emailTextField.setText(user.getEmail());
            nameTextField.setText(user.getName());
            phoneNumberTextField.setText(user.getPhoneNumber());
            addressTextArea.setText(user.getAddress());
            genderTextField.setText(user.getGender().toString());
            roleTextField.setText(user.getRole().toString());
            bodTextField.setText(user.getBirthOfDate().toString());
            String projectDir = System.getProperty("user.dir");
            String avatarsDir = projectDir + "/src/main/resources/images/user/";
            String path = avatarsDir + user.getAvatar();
            Image image = ImageCache.getInstance().getImage(path, avatarsDir + "Male User.png");
            cropAndClipToCircle(image, avatarImageView, 55);
        }
    }

    /**
     * Handles the back action that is triggered by the user interface.
     * This method is linked to the UI event that closes the current window when invoked.
     */
    @FXML
    private void back() {
        closeWindow();
    }

    /**
     * Closes the current window associated with the back button's scene.
     * This method retrieves the window from the scene containing the back button
     * and then closes the window, effectively hiding or disposing of it.
     */
    private void closeWindow() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

}
