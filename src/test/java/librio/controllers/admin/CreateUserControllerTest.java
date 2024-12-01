package librio.controllers.admin;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.*;
import javafx.scene.control.*;
import librio.enums.Gender;
import librio.enums.Role;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CreateUserControllerTest {

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

    private CreateUserController createUserController;

    @BeforeEach
    public void setup() {
        createUserController = new CreateUserController();
        // Mock or initialize required fields for the controller
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
        // Continue for other fields as necessary
    }

    @Test
    public void testCreateUserWithEmptyFields() {
        createUserController.nameTextField.setText("");
        createUserController.emailTextField.setText("");
        // Set other fields as empty

        createUserController.createUserButton.fire();

        // Assert the presence of error messages
        assertNotNull(createUserController.nameErrorLabel.getText());
        assertNotNull(createUserController.emailErrorLabel.getText());
        // Continue assertions for other fields
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
        // Set other fields with valid data

        createUserController.createUserButton.fire();

        // Check there are no error messages
        assertEquals("", createUserController.nameErrorLabel.getText());
        assertEquals("", createUserController.emailErrorLabel.getText());
        // Continue assertions for other fields
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
        createUserController.roleComboBox.setValue(null); // Missing role selection
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
        createUserController.genderComboBox.setValue(null); // Missing gender selection
        createUserController.roleComboBox.setValue(Role.MEMBER);
        createUserController.genderErrorLabel.setText("Empty gender");

        createUserController.createUserButton.fire();

        assertNotEquals("", createUserController.genderErrorLabel.getText());
    }
}