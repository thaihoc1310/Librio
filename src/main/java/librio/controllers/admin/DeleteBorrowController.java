package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import librio.models.Borrow;
import librio.util.DatabaseUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class DeleteBorrowController implements Initializable {
    private Borrow borrow;

    @FXML
    private Button cancelButton;

    @FXML
    private Button deleteButton;

    @FXML
    void cancel() {
        closeWindow();
    }
    @FXML
    void deleteBorrow() {
        DatabaseUtil.deleteBorrow(borrow);
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) deleteButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setBorrow(Borrow borrow) {
        this.borrow = borrow;
    }
}
