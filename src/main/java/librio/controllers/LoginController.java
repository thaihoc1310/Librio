package librio.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import static librio.util.DatabaseUtil.authenticate;

public class LoginController {
    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameField;

    @FXML
    private void handleLogin() {
        String email = usernameField.getText();
        String password = passwordField.getText();
        if(authenticate(email,password)!= null){
            checkAuthorization();
        }else {

        }
    }

    private void checkAuthorization(){

    }

}
