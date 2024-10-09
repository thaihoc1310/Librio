package librio.controllers;

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
import javafx.scene.layout.AnchorPane;
import librio.database.DatabaseConnection;
import librio.models.User;

/**
 *
 * @author WINDOWS 10
 */
public class UserDetailsController implements Initializable {
    private User user;

    public void setUser(User user) {
        this.user = user;
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Tạo câu truy vấn SQL để lấy thông tin user
            String query = "SELECT * FROM users WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, user.getId()); // Truyền ID user vào câu truy vấn

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Gán dữ liệu từ database vào các TextField
                userIDTextField.setText(resultSet.getString("id"));
                emailTextField.setText(resultSet.getString("email"));
                nameTextField.setText(resultSet.getString("name"));
                phoneTextField.setText(resultSet.getString("phone_number"));
                addressTextField.setText(resultSet.getString("address"));
                genderTextField.setText(resultSet.getString("gender"));
                roleTextField.setText(resultSet.getString("role"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private TextField userIDTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private TextField nameTextField;

    @FXML
    private TextField phoneTextField;

    @FXML
    private TextField addressTextField;

    @FXML
    private TextField genderTextField;

    @FXML
    private TextField roleTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        setField();
//        genderList();
//        statusList();
    }

}
