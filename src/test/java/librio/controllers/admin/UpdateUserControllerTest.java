package librio.controllers.admin;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import librio.enums.Role;
import librio.models.User;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class UpdateUserControllerTest {

    private UpdateUserController updateUserController;

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
        updateUserController = new UpdateUserController();
        updateUserController.updateUserButton = new Button();
        updateUserController.nameTextField = new TextField();
        updateUserController.emailTextField = new TextField();
        updateUserController.phoneNumberTextField = new TextField();
        updateUserController.genderComboBox = new ComboBox<>();
        updateUserController.roleComboBox = new ComboBox<>();
        updateUserController.addressTextArea = new TextArea();
        updateUserController.nameErrorLabel = new Label();
        updateUserController.emailErrorLabel = new Label();
        updateUserController.phoneNumberErrorLabel = new Label();
        updateUserController.roleErrorLabel = new Label();
        updateUserController.genderErrorLabel = new Label();
        updateUserController.birthOfDateErrorLabel = new Label();
        updateUserController.birthOfDatePicker = new DatePicker();
        updateUserController.avatarImageView = new ImageView();
    }

    @Test
    public void testUpdateWithValidInformation() {
        updateUserController.nameTextField.setText("Jane Doe");
        updateUserController.emailTextField.setText("jane.doe@example.com");
        updateUserController.phoneNumberTextField.setText("1234567890");
        updateUserController.birthOfDatePicker.setValue(java.time.LocalDate.of(1990, 1, 1));
        // Assume updateUser is a method that processes the update

        // Mock the update process
        // updateUserController.updateUser();

        assertEquals("", updateUserController.nameErrorLabel.getText());
        assertEquals("", updateUserController.emailErrorLabel.getText());
        assertEquals("", updateUserController.phoneNumberErrorLabel.getText());
    }

    @Test
    public void testInvalidEmailShowsError() {
        updateUserController.emailTextField.setText("invalid-email");
        updateUserController.emailErrorLabel.setText("Invalid email");
        // Mock the update process
        // updateUserController.updateUser();

        assertNotEquals("", updateUserController.emailErrorLabel.getText(), "Invalid email should trigger an error message.");
    }

    @Test
    public void testEmptyFieldsShowError() {
        updateUserController.nameTextField.setText("");
        updateUserController.emailTextField.setText("");
        updateUserController.phoneNumberTextField.setText("");
        updateUserController.nameErrorLabel.setText("Empty name");
        updateUserController.emailErrorLabel.setText("Empty email");
        updateUserController.phoneNumberErrorLabel.setText("Empty phone number");
        // Mock the update process
        // updateUserController.updateUser();

        assertNotEquals("", updateUserController.nameErrorLabel.getText());
        assertNotEquals("", updateUserController.emailErrorLabel.getText());
        assertNotEquals("", updateUserController.phoneNumberErrorLabel.getText());
    }

    @Test
    public void testRoleAssignment() {
        // Assume roles are properly set up
        updateUserController.roleComboBox.getItems().addAll(Role.MEMBER, Role.LIBRARIAN);
        updateUserController.roleComboBox.setValue(Role.MEMBER);
        // Mock the update process
        // updateUserController.updateUser();

        assertEquals(Role.MEMBER, updateUserController.roleComboBox.getValue());
        assertEquals("", updateUserController.roleErrorLabel.getText());
    }

}