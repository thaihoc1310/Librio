package librio.controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import librio.database.DatabaseConnection;
import librio.models.User;

public class DeleteUserController implements Initializable {
    private User user;

    @FXML
    private Button deleteButton;
    @FXML
    private Button cancelButton;

    @FXML
    private void deleteUser(ActionEvent event) {

    }

    @FXML
    private void cancel(ActionEvent event) {
        closeWindow();
    }

    public void setUser(User user) {
        this.user = user;
    }

    private void closeWindow() {
        // Đóng cửa sổ hiện tại
        Stage stage = (Stage) deleteButton.getScene().getWindow(); // Hoặc có thể sử dụng cancelButton
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
