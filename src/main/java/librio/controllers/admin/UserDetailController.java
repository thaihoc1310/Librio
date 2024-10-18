package librio.controllers.admin;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import librio.models.User;

import static librio.util.DesignUtil.cropAndClipToCircle;

/**
 *
 * @author WINDOWS 10
 */
public class UserDetailController implements Initializable {
    private User user;

    @FXML
    private TextField emailTextField;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField phoneNumberTextField;
    @FXML
    private TextArea addressTextArea;
    @FXML
    private TextField userIDTextField;
    @FXML
    private TextField genderTextField;
    @FXML
    private TextField roleTextField;
    @FXML
    private ImageView avatarImageView;

    @FXML
    private Button backButton;
    private ManageUserController manageUserController;


    public void setUser(User user) {
        this.user = user;
        populateFields();
    }

    public void setManageUserController(ManageUserController manageUserController) {
        this.manageUserController = manageUserController;
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

            // Lấy đường dẫn ảnh từ project
            String projectDir = System.getProperty("user.dir");
            String avatarsDir = projectDir + "/src/main/resources/images/user/";
            String path = avatarsDir + user.getAvatar();

            // Chuyển đổi đường dẫn thành URL
            File file = new File(path);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString()); // Chuyển đổi file thành URL hợp lệ
                cropAndClipToCircle(image, avatarImageView, 70);
            } else {
                String defaultImage = avatarsDir + "Male User.png";
                File defaultImageFile = new File(defaultImage);
                Image image = new Image(defaultImageFile.toURI().toString()); // Chuyển đổi file thành URL hợp lệ
                cropAndClipToCircle(image, avatarImageView, 70);
            }
        }
    }

    @FXML
    private void back() {
        closeWindow();
    }


    private void closeWindow() {
        // Đóng cửa sổ hiện tại
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

//        setField();
//        genderList();
//        statusList();
    }

}
