package librio.controllers.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import librio.models.Gender;
import librio.models.Role;
import librio.models.User;
import librio.database.DatabaseConnection;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UpdateUserController implements Initializable {

    @FXML
    private TextField emailTextField;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField phoneNumberTextField;
    @FXML
    private ComboBox<Gender> genderComboBox;
    @FXML
    private ComboBox<Role> roleComboBox;
    @FXML
    private TextArea addressTextArea;
    @FXML
    private Button updateUserButton;
    @FXML
    private Button cancelButton;

    private User user;
    private ManageUserController manageUserController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
        genderComboBox.setEditable(false);
        roleComboBox.setEditable(false);
    }

    public void setUser(User user) {
        this.user = user;
        populateFields();
    }

    public void setManageUserController(ManageUserController manageUserController) {
        this.manageUserController = manageUserController;
    }

    private void populateFields() {
        if (user != null) {
            emailTextField.setText(user.getEmail());
            nameTextField.setText(user.getName());
            phoneNumberTextField.setText(user.getPhoneNumber());
            addressTextArea.setText(user.getAddress());
            genderComboBox.setValue(user.getGender());
            roleComboBox.setValue(user.getRole());
        }
    }

    @FXML
    private void updateUser() {
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        String phoneNumber = phoneNumberTextField.getText();
        Gender gender = Gender.valueOf(String.valueOf(genderComboBox.getValue()));
        Role role = Role.valueOf(String.valueOf(roleComboBox.getValue()));
        String address = addressTextArea.getText();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "UPDATE users SET name = ?, email = ?, phone_number = ?, address = ?, gender = ?, role = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phoneNumber);
            statement.setString(4, address);
            statement.setString(5, gender.name());
            statement.setString(6, role.name());
            statement.setString(7, user.getId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("User updated successfully!");
                if (manageUserController != null) {
                    manageUserController.loadUsersFromDatabase();
                }
                closeStage();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelUpdateUser() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) updateUserButton.getScene().getWindow();
        stage.close();
    }
}