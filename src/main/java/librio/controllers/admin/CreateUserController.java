package librio.controllers.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import librio.models.Gender;
import librio.models.Role;
import librio.database.DatabaseConnection;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CreateUserController implements Initializable {

    @FXML
    private TextField nameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private TextField confirmPasswordTextField;
    @FXML
    private TextField phoneNumberTextField;
    @FXML
    private ComboBox<Gender> genderComboBox;
    @FXML
    private ComboBox<Role> roleComboBox;
    @FXML
    private TextArea addressTextArea;
    @FXML
    private Button createUserButton;
    @FXML
    private Button cancelButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
    }

    @FXML
    private void createUser() {
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        String password = passwordTextField.getText();
        String confirmPassword = confirmPasswordTextField.getText();
        String phoneNumber = phoneNumberTextField.getText();
        Gender gender = Gender.valueOf(String.valueOf(genderComboBox.getValue()));
        Role role = Role.valueOf(String.valueOf(roleComboBox.getValue()));
        String address = addressTextArea.getText();

        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match!");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO users (name, email, password, phone_number, address, gender, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, phoneNumber);
            statement.setString(5, address);
            statement.setString(6, gender.name());
            statement.setString(7, role.name());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("A new user was inserted successfully!");
                //navigate
                clearInputFields();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelCreateUser() {
        clearInputFields();
    }

    private void clearInputFields() {
        nameTextField.clear();
        emailTextField.clear();
        passwordTextField.clear();
        confirmPasswordTextField.clear();
        phoneNumberTextField.clear();
        addressTextArea.clear();
        genderComboBox.getSelectionModel().clearSelection();
        roleComboBox.getSelectionModel().clearSelection();
    }
}