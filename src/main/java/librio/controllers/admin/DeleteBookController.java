package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import librio.models.Book;
import librio.util.DatabaseUtil;

import java.io.File;


public class DeleteBookController {
    @FXML
    private Button deleteButton;

    private Book book;


    @FXML
    private void deleteUser() {
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

}
