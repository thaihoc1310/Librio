package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class IsbnNotAvailableController {
    @FXML
    private AnchorPane isbnPane;

    @FXML
    private void confirmCancel() {
        Stage stage = (Stage) isbnPane.getScene().getWindow();
        stage.close();
    }
}