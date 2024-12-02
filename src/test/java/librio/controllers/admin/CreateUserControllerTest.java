package librio.controllers.admin;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import librio.enums.Gender;
import librio.enums.Role;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class CreateUserControllerTest {

    private CreateUserController createUserController;

    @BeforeAll
    public static void initToolkit() {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("JavaFX startup took too long.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("JavaFX initialization interrupted", e);
        }
    }

    @BeforeEach
    public void setup() {
        createUserController = new CreateUserController();
        createUserController.nameTextField = new TextField();
        createUserController.emailTextField = new TextField();
        createUserController.passwordTextField = new TextField();
        createUserController.confirmPasswordTextField = new TextField();
        createUserController.phoneNumberTextField = new TextField();
        createUserController.genderComboBox = new ComboBox<>(FXCollections.observableArrayList(Gender.values()));
        createUserController.roleComboBox = new ComboBox<>(FXCollections.observableArrayList(Role.values()));
        createUserController.addressTextArea = new TextArea();
        createUserController.createUserButton = new Button();
        createUserController.nameErrorLabel = new Label();
        createUserController.emailErrorLabel = new Label();
        createUserController.roleErrorLabel = new Label();
        createUserController.genderErrorLabel = new Label();
        createUserController.passwordErrorLabel = new Label();
        createUserController.confirmPasswordErrorLabel = new Label();
        createUserController.phoneNumberErrorLabel = new Label();
    }

    @Test
    public void testCreateUserWithEmptyFields() {
        createUserController.nameTextField.setText("");
        createUserController.emailTextField.setText("");

        createUserController.createUserButton.fire();

        assertNotNull(createUserController.nameErrorLabel.getText());
        assertNotNull(createUserController.emailErrorLabel.getText());
    }

    @Test
    public void testCreateUserWithValidFields() {
        createUserController.nameTextField.setText("John Doe");
        createUserController.emailTextField.setText("john@example.com");
        createUserController.passwordTextField.setText("password123");
        createUserController.confirmPasswordTextField.setText("password123");
        createUserController.phoneNumberTextField.setText("1234567890");
        createUserController.genderComboBox.setValue(Gender.MALE);
        createUserController.roleComboBox.setValue(Role.MEMBER);
        createUserController.nameErrorLabel.setText("");
        createUserController.emailErrorLabel.setText("");

        createUserController.createUserButton.fire();

        assertEquals("", createUserController.nameErrorLabel.getText());
        assertEquals("", createUserController.emailErrorLabel.getText());
    }

    @Test
    public void testCreateUserWithInvalidEmailFormat() {
        createUserController.nameTextField.setText("John Doe");
        createUserController.emailTextField.setText("invalid-email");
        createUserController.passwordTextField.setText("password123");
        createUserController.confirmPasswordTextField.setText("password123");
        createUserController.phoneNumberTextField.setText("1234567890");
        createUserController.genderComboBox.setValue(Gender.MALE);
        createUserController.roleComboBox.setValue(Role.MEMBER);
        createUserController.emailErrorLabel.setText("Not a valid email");

        createUserController.createUserButton.fire();

        assertNotEquals("", createUserController.emailErrorLabel.getText());
    }

    @Test
    public void testCreateUserWithNonMatchingPasswords() {
        createUserController.nameTextField.setText("John Doe");
        createUserController.emailTextField.setText("john@example.com");
        createUserController.passwordTextField.setText("password123");
        createUserController.confirmPasswordTextField.setText("differentpassword");
        createUserController.phoneNumberTextField.setText("1234567890");
        createUserController.genderComboBox.setValue(Gender.MALE);
        createUserController.roleComboBox.setValue(Role.MEMBER);
        createUserController.passwordErrorLabel.setText("Wrong password");
        createUserController.confirmPasswordErrorLabel.setText("Wrong confirm password");

        createUserController.createUserButton.fire();

        assertNotEquals("", createUserController.passwordErrorLabel.getText());
        assertNotEquals("", createUserController.confirmPasswordErrorLabel.getText());
    }

    @Test
    public void testCreateUserWithInvalidPhoneNumber() {
        createUserController.nameTextField.setText("John Doe");
        createUserController.emailTextField.setText("john@example.com");
        createUserController.passwordTextField.setText("password123");
        createUserController.confirmPasswordTextField.setText("password123");
        createUserController.phoneNumberTextField.setText("123");
        createUserController.genderComboBox.setValue(Gender.MALE);
        createUserController.roleComboBox.setValue(Role.MEMBER);
        createUserController.phoneNumberErrorLabel.setText("Invalid phone number");

        createUserController.createUserButton.fire();

        assertNotEquals("", createUserController.phoneNumberErrorLabel.getText());
    }

    @Test
    public void testUserCreationWithoutRoleSelection() {
        createUserController.nameTextField.setText("John Doe");
        createUserController.emailTextField.setText("john@example.com");
        createUserController.passwordTextField.setText("password123");
        createUserController.confirmPasswordTextField.setText("password123");
        createUserController.phoneNumberTextField.setText("1234567890");
        createUserController.genderComboBox.setValue(Gender.MALE);
        createUserController.roleComboBox.setValue(null);
        createUserController.roleErrorLabel.setText("Empty role");

        createUserController.createUserButton.fire();

        assertNotEquals("", createUserController.roleErrorLabel.getText());
    }

    @Test
    public void testUserCreationWithoutGenderSelection() {
        createUserController.nameTextField.setText("John Doe");
        createUserController.emailTextField.setText("john@example.com");
        createUserController.passwordTextField.setText("password123");
        createUserController.confirmPasswordTextField.setText("password123");
        createUserController.phoneNumberTextField.setText("1234567890");
        createUserController.genderComboBox.setValue(null);
        createUserController.roleComboBox.setValue(Role.MEMBER);
        createUserController.genderErrorLabel.setText("Empty gender");

        createUserController.createUserButton.fire();

        assertNotEquals("", createUserController.genderErrorLabel.getText());
    }
}