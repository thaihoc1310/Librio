package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import librio.models.Book;
import librio.util.DatabaseUtil;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


public class DeleteBookController implements Initializable {
    @FXML
    private Button deleteButton;
    @FXML
    private Label errorLabel;

    private Book book;


    @FXML
    private void deleteBook() {
        if(DatabaseUtil.checkIfBookIsBorrowed(book)){
            errorLabel.setVisible(true);
            return;
        }
        DatabaseUtil.deleteBook(book);
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/book/";
        if (book.getImagePath() != null && !book.getImagePath().isEmpty() && !book.getImagePath().equals("defaultBook.jpg")) {
            File oldFile = new File(avatarsDir + book.getImagePath());
            if (oldFile.exists()) {
                boolean deleted = oldFile.delete();
                if (!deleted) {
                    System.out.println("Không thể xóa tệp ảnh cũ: " + oldFile.getAbsolutePath());
                }
            }
        }
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        errorLabel.setVisible(false);
    }
}
