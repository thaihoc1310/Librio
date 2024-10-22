package librio.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import librio.models.Role;
import librio.models.User;

import java.io.IOException;

import static librio.util.DatabaseUtil.authenticate;

public class LoginController {
    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameField;

    @FXML
    private void handleLogin() throws IOException {
        String email = usernameField.getText();
        String password = passwordField.getText();
        User loginUser = authenticate(email,password);
        if(loginUser!= null){
            checkAuthorization(loginUser);
        }else {
            //validator
        }
    }

    private void checkAuthorization(User loginUser) throws IOException {
        Role userRole = loginUser.getRole();
        Stage currentStage = (Stage) loginButton.getScene().getWindow();
        Stage stage = new Stage();
        stage.setTitle("Librio");
        if(userRole.equals(Role.LIBRARIAN)){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdDashboard.fxml"));
            Parent adminDashboardRoot  = loader.load();
            stage.setScene(new Scene(adminDashboardRoot));
        }else if(userRole.equals(Role.MEMBER)){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/Book.fxml"));
            Parent adminDashboardRoot  = loader.load();
            stage.setScene(new Scene(adminDashboardRoot));
        }
        stage.show();
        currentStage.close();
    }

}


