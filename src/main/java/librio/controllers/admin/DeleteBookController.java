package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import librio.models.Book;
import librio.util.DatabaseUtil;


public class DeleteBookController {
    @FXML
    private Button deleteButton;

    private Book book;


    @FXML
    private void deleteUser() {
//        DatabaseUtil.deleteBook(book);
        closeWindow();
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    public void setBook(Book book) {
        this.book = book;
    }

    private void closeWindow() {
        Stage stage = (Stage) deleteButton.getScene().getWindow();
        stage.close();
    }

}
