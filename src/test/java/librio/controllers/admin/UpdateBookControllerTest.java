package librio.controllers.admin;

import org.junit.jupiter.api.*;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class UpdateBookControllerTest {

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

    private UpdateBookController updateBookController;

    @BeforeEach
    public void setup() {
        updateBookController = new UpdateBookController();
        updateBookController.authorTextField = new TextField();
        updateBookController.categoryTextField = new TextField();
        updateBookController.isbnTextField = new TextField();
        updateBookController.languageTextField = new TextField();
        updateBookController.numberOfPagesTextField = new TextField();
        updateBookController.publisherTextField = new TextField();
        updateBookController.quantityOfCopyTextField = new TextField();
        updateBookController.yearPublishedTextField = new TextField();
        updateBookController.bookTitleTextField = new TextArea();
        updateBookController.descriptionTextArea = new TextArea();
        updateBookController.bookImageView = new ImageView();

        // Error Labels
        updateBookController.authorErrorLabel = new Label();
        updateBookController.bookTitleErrorLabel = new Label();
        updateBookController.categoryErrorLabel = new Label();
        updateBookController.isbnErrorLabel = new Label();
        updateBookController.languageErrorLabel = new Label();
        updateBookController.numberOfPagesErrorLabel = new Label();
        updateBookController.publisherErrorLabel = new Label();
        updateBookController.quantityOfCopyErrorLabel = new Label();

        updateBookController.cancelButton = new Button();
    }

    @Test
    public void testUpdateBookWithEmptyFields() {
        updateBookController.bookTitleTextField.setText("");
        updateBookController.authorTextField.setText("");
        updateBookController.isbnTextField.setText("");
        updateBookController.publisherTextField.setText("");
        updateBookController.isbnErrorLabel.setText("Empty ISBN");
        updateBookController.bookTitleErrorLabel.setText("Empty book title");
        updateBookController.authorErrorLabel.setText("Empty author");
        updateBookController.publisherErrorLabel.setText("Empty publisher");

        updateBookController.updateBook();

        assertNotEquals("", updateBookController.bookTitleErrorLabel.getText());
        assertNotEquals("", updateBookController.authorErrorLabel.getText());
        assertNotEquals("", updateBookController.isbnErrorLabel.getText());
        assertNotEquals("", updateBookController.publisherErrorLabel.getText());
    }

    @Test
    public void testUpdateBookWithInvalidISBNFormat() {
        updateBookController.bookTitleTextField.setText("Valid Title");
        updateBookController.authorTextField.setText("Valid Author");
        updateBookController.isbnTextField.setText("invalid-isbn");
        updateBookController.publisherTextField.setText("Valid Publisher");
        updateBookController.isbnErrorLabel.setText("Invalid ISBN format");

        updateBookController.updateBook();

        assertNotEquals("", updateBookController.isbnErrorLabel.getText());
    }

    @Test
    public void testUpdateBookWithValidFields() {
        updateBookController.bookTitleTextField.setText("Valid Title");
        updateBookController.authorTextField.setText("Valid Author");
        updateBookController.isbnTextField.setText("978-3-16-148410-0");
        updateBookController.publisherTextField.setText("Valid Publisher");
        updateBookController.numberOfPagesTextField.setText("100");
        updateBookController.languageTextField.setText("English");
        updateBookController.categoryTextField.setText("Fiction");

        updateBookController.updateBook();

        assertEquals("", updateBookController.bookTitleErrorLabel.getText());
        assertEquals("", updateBookController.authorErrorLabel.getText());
        assertEquals("", updateBookController.isbnErrorLabel.getText());
        assertEquals("", updateBookController.publisherErrorLabel.getText());
    }

    @Test
    public void testUpdateBookWhenFieldsHaveWhitespaceOnly() {
        updateBookController.bookTitleTextField.setText("   ");
        updateBookController.authorTextField.setText("   ");
        updateBookController.isbnTextField.setText("   ");
        updateBookController.publisherTextField.setText("   ");
        updateBookController.bookTitleErrorLabel.setText("Empty book title");
        updateBookController.authorErrorLabel.setText("Empty author");
        updateBookController.isbnErrorLabel.setText("Empty ISBN");
        updateBookController.publisherErrorLabel.setText("Empty publisher");

        updateBookController.updateBook();

        assertNotEquals("", updateBookController.bookTitleErrorLabel.getText());
        assertNotEquals("", updateBookController.authorErrorLabel.getText());
        assertNotEquals("", updateBookController.isbnErrorLabel.getText());
        assertNotEquals("", updateBookController.publisherErrorLabel.getText());
    }

    @Test
    public void testErrorLabelsClearedAfterSuccessfulUpdate() {
        updateBookController.bookTitleTextField.setText("Valid Title");
        updateBookController.authorTextField.setText("Valid Author");
        updateBookController.isbnTextField.setText("978-3-16-148410-0");
        updateBookController.publisherTextField.setText("Valid Publisher");
        updateBookController.numberOfPagesTextField.setText("100");
        updateBookController.languageTextField.setText("English");
        updateBookController.categoryTextField.setText("Fiction");

        updateBookController.updateBook(); // Execute a valid update

        assertEquals("", updateBookController.bookTitleErrorLabel.getText());
        assertEquals("", updateBookController.authorErrorLabel.getText());
        assertEquals("", updateBookController.isbnErrorLabel.getText());
        assertEquals("", updateBookController.publisherErrorLabel.getText());
    }
}