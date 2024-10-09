package librio.controllers.admin;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import librio.database.DatabaseConnection;
import librio.models.Gender;
import librio.models.Role;
import librio.models.User;

/**
 *
 * @author WINDOWS 10
 */
public class UserDetailsController implements Initializable {
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
        }
    }

    @FXML
    private void back() {
        closeWindow();
    }


    private void closeWindow() {
        // Đóng cửa sổ hiện tại
        Stage stage = (Stage) backButton.getScene().getWindow(); // Hoặc có thể sử dụng cancelButton
        stage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        setField();
//        genderList();
//        statusList();
    }

}
