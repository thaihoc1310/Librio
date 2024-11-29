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

import java.net.URL;
import java.util.ResourceBundle;

import static librio.util.DesignUtil.cropAndClipToCircle;

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

    public void setUser(User user) {
        this.user = user;
        populateFields();
    }

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

    @FXML
    private void back() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

}
