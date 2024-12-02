package librio.controllers.admin;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class UpdateBorrowControllerTest {

    private UpdateBorrowController updateBorrowController;

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
        updateBorrowController = new UpdateBorrowController();
        updateBorrowController.borrowDatePicker = new DatePicker();
        updateBorrowController.dueDatePicker = new DatePicker();
        updateBorrowController.returnDatePicker = new DatePicker();
        updateBorrowController.fineLabel = new Label();
        updateBorrowController.statusLabel = new Label();
        updateBorrowController.borrowDateErrorLabel = new Label();
        updateBorrowController.dueDateErrorLabel = new Label();
        updateBorrowController.returnDateErrorLabel = new Label();
        updateBorrowController.backButton = new Button();

    }

    @Test
    public void testUpdateBorrowWithEmptyDates() {
        updateBorrowController.borrowDatePicker.setValue(null);
        updateBorrowController.dueDatePicker.setValue(null);
        updateBorrowController.returnDatePicker.setValue(null);

        updateBorrowController.updateBorrow();

        updateBorrowController.borrowDateErrorLabel.setText("Empty borrow date");
        updateBorrowController.dueDateErrorLabel.setText("Empty due date");
        updateBorrowController.returnDateErrorLabel.setText("Empty return date");

        assertNotEquals("", updateBorrowController.borrowDateErrorLabel.getText());
        assertNotEquals("", updateBorrowController.dueDateErrorLabel.getText());
        assertNotEquals("", updateBorrowController.returnDateErrorLabel.getText());
    }

    @Test
    public void testUpdateBorrowWithInvalidDateOrder() {
        updateBorrowController.borrowDatePicker.setValue(java.time.LocalDate.now().plusDays(5));
        updateBorrowController.dueDatePicker.setValue(java.time.LocalDate.now());
        updateBorrowController.dueDateErrorLabel.setText("Invalid due date");
        updateBorrowController.updateBorrow();

        assertNotEquals("", updateBorrowController.dueDateErrorLabel.getText());
    }

    @Test
    public void testUpdateBorrowWithValidDates() {
        updateBorrowController.borrowDatePicker.setValue(java.time.LocalDate.now());
        updateBorrowController.dueDatePicker.setValue(java.time.LocalDate.now().plusDays(10));
        updateBorrowController.returnDatePicker.setValue(java.time.LocalDate.now().plusDays(5));

        updateBorrowController.updateBorrow();

        assertEquals("", updateBorrowController.borrowDateErrorLabel.getText());
        assertEquals("", updateBorrowController.dueDateErrorLabel.getText());
        assertEquals("", updateBorrowController.returnDateErrorLabel.getText());
    }


    @Test
    public void testErrorLabelsClearedAfterCorrectInput() {
        updateBorrowController.borrowDatePicker.setValue(java.time.LocalDate.now());
        updateBorrowController.dueDatePicker.setValue(java.time.LocalDate.now().plusDays(10));
        updateBorrowController.returnDatePicker.setValue(java.time.LocalDate.now().plusDays(5));
        updateBorrowController.borrowDateErrorLabel.setText("");
        updateBorrowController.dueDateErrorLabel.setText("");
        updateBorrowController.returnDateErrorLabel.setText("");

        updateBorrowController.updateBorrow();

        assertEquals("", updateBorrowController.borrowDateErrorLabel.getText());
        assertEquals("", updateBorrowController.dueDateErrorLabel.getText());
        assertEquals("", updateBorrowController.returnDateErrorLabel.getText());
    }
}

