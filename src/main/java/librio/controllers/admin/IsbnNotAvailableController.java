package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * The IsbnNotAvailableController class is a JavaFX controller responsible for
 * handling events and interactions within the 'ISBN Not Available' user interface.
 * It manages the behavior of the UI component represented by the FXML file it's
 * associated with, specifically focusing on the action of closing the window.
 */
public class IsbnNotAvailableController {
    @FXML
    private AnchorPane isbnPane;

    /**
     * Closes the current window. This method is typically invoked to cancel
     * an action and dismiss the associated user interface. It is wired to
     * a user interface element through the JavaFX framework using the FXML
     * annotation.
     */
    @FXML
    private void confirmCancel() {
        Stage stage = (Stage) isbnPane.getScene().getWindow();
        stage.close();
    }
}