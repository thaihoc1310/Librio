package librio.controllers.admin;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CreateBorrowControllerTest {

    private CreateBorrowController createBorrowController;

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
        createBorrowController = new CreateBorrowController();
        createBorrowController.emailTextField = new TextField();
        createBorrowController.bookIsbnTextField = new TextField();
        createBorrowController.bookTitleTextField = new TextField();
        createBorrowController.nameTextField = new TextField();
        createBorrowController.bookImageView = new ImageView();
        createBorrowController.bookIsbnErrorLabel = new Label();
        createBorrowController.emailErrorLabel = new Label();
        createBorrowController.dueDateErrorLabel = new Label();
        createBorrowController.userAlreadyBorrowErrorLabel = new Label();
        createBorrowController.borrowDatePicker = new DatePicker();
        createBorrowController.dueDatePicker = new DatePicker();
        createBorrowController.createButton = new Button();
    }

    @Test
    public void testCreateBorrowWithEmptyFields() {
        createBorrowController.emailTextField.setText("");
        createBorrowController.bookIsbnTextField.setText("");
        createBorrowController.emailErrorLabel.setText("Empty email");
        createBorrowController.bookIsbnErrorLabel.setText("Empty email");

        createBorrowController.createButton.fire();

        assertNotEquals("", createBorrowController.emailErrorLabel.getText());
        assertNotEquals("", createBorrowController.bookIsbnErrorLabel.getText());
    }

    @Test
    public void testCreateBorrowWithInvalidEmailFormat() {
        createBorrowController.emailTextField.setText("invalid-email");
        createBorrowController.bookIsbnTextField.setText("978-3-16-148410-0");
        createBorrowController.emailErrorLabel.setText("Not a valid email");

        createBorrowController.createButton.fire();

        assertNotEquals("", createBorrowController.emailErrorLabel.getText());
    }

    @Test
    public void testCreateBorrowWithInvalidISBNFormat() {
        createBorrowController.emailTextField.setText("user@example.com");
        createBorrowController.bookIsbnTextField.setText("invalid-isbn");
        createBorrowController.bookIsbnErrorLabel.setText("Invalid ISBN format");

        createBorrowController.createButton.fire();

        assertNotEquals("", createBorrowController.bookIsbnErrorLabel.getText());
    }

    @Test
    public void testCreateBorrowWithPastDueDate() {
        createBorrowController.emailTextField.setText("user@example.com");
        createBorrowController.bookIsbnTextField.setText("978-3-16-148410-0");
        createBorrowController.borrowDatePicker.setValue(LocalDate.now().plusDays(1));
        createBorrowController.dueDatePicker.setValue(LocalDate.now());
        createBorrowController.dueDateErrorLabel.setText("Invalid due date");

        createBorrowController.createButton.fire();

        assertNotEquals("", createBorrowController.dueDateErrorLabel.getText());
    }

    @Test
    public void testCreateBorrowWithValidFields() {
        createBorrowController.emailTextField.setText("user@example.com");
        createBorrowController.bookIsbnTextField.setText("978-3-16-148410-0");
        createBorrowController.borrowDatePicker.setValue(LocalDate.now());
        createBorrowController.dueDatePicker.setValue(LocalDate.now().plusDays(30));

        createBorrowController.createButton.fire();

        assertEquals("", createBorrowController.emailErrorLabel.getText());
        assertEquals("", createBorrowController.bookIsbnErrorLabel.getText());
        assertEquals("", createBorrowController.dueDateErrorLabel.getText());
    }

}