package librio.controllers.admin;

import org.junit.jupiter.api.*;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class CreateBookControllerTest {

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

    private CreateBookController createBookController;

    @BeforeEach
    public void setup() {
        createBookController = new CreateBookController();
        createBookController.bookTitleTextField = new TextArea();
        createBookController.authorTextField = new TextField();
        createBookController.isbnTextField = new TextField();
        createBookController.publisherTextField = new TextField();
        createBookController.bookImageView = new ImageView();
        createBookController.bookTitleErrorLabel = new Label();
        createBookController.authorErrorLabel = new Label();
        createBookController.isbnErrorLabel = new Label();
        createBookController.publisherErrorLabel = new Label();
        createBookController.createBookButton = new Button();
    }

    @Test
    public void testCreateBookWithEmptyFields() {
        createBookController.bookTitleTextField.setText("");
        createBookController.authorTextField.setText("");
        createBookController.isbnTextField.setText("");
        createBookController.publisherTextField.setText("");
        createBookController.isbnErrorLabel.setText("Empty ISBN");
        createBookController.bookTitleErrorLabel.setText("Empty book title");
        createBookController.authorErrorLabel.setText("Empty author");
        createBookController.publisherErrorLabel.setText("Empty publisher");

        createBookController.createBookButton.fire();

        assertNotEquals("", createBookController.bookTitleErrorLabel.getText());
        assertNotEquals("", createBookController.authorErrorLabel.getText());
        assertNotEquals("", createBookController.isbnErrorLabel.getText());
        assertNotEquals("", createBookController.publisherErrorLabel.getText());
    }

    @Test
    public void testCreateBookWithInvalidISBNFormat() {
        createBookController.bookTitleTextField.setText("Valid Title");
        createBookController.authorTextField.setText("Valid Author");
        createBookController.isbnTextField.setText("invalid-isbn");
        createBookController.publisherTextField.setText("Valid Publisher");
        createBookController.isbnErrorLabel.setText("Invalid ISBN format");

        createBookController.createBookButton.fire();

        assertNotEquals("", createBookController.isbnErrorLabel.getText());
    }

    @Test
    public void testCreateBookWithValidFields() {
        createBookController.bookTitleTextField.setText("Valid Title");
        createBookController.authorTextField.setText("Valid Author");
        createBookController.isbnTextField.setText("978-3-16-148410-0");
        createBookController.publisherTextField.setText("Valid Publisher");

        createBookController.createBookButton.fire();

        assertEquals("", createBookController.bookTitleErrorLabel.getText());
        assertEquals("", createBookController.authorErrorLabel.getText());
        assertEquals("", createBookController.isbnErrorLabel.getText());
        assertEquals("", createBookController.publisherErrorLabel.getText());
    }

    // Additional tests: Handle invalid file paths for book cover images, duplicate ISBN, etc.
}