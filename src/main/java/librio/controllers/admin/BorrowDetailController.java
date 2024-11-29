package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import librio.cache.ImageCache;
import librio.models.Book;
import librio.models.Borrow;
import librio.models.User;

import java.net.URL;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.getBookByIsbn;
import static librio.util.DatabaseUtil.getUserById;
import static librio.util.DesignUtil.loadDefaultBookImage;

public class BorrowDetailController implements Initializable {
    @FXML
    private Label bookIsbnLabel, bookTitleLabel, borrowDateLabel, borrowIdLabel, dueDateLabel,
            fineLabel, memberEmailLabel, returnDateLabel, statusLabel, memberNameLabel;
    @FXML
    private Button backButton;
    @FXML
    private ImageView bookImageView;

    private Borrow borrow;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setBorrow(Borrow borrow) {
        this.borrow = borrow;
        populateFields();
    }

    private void populateFields() {
        if (borrow != null) {
            Book borrowedBook = getBookByIsbn(borrow.getBookIsbn());
            User user = getUserById(borrow.getEmail());

            borrowIdLabel.setText(String.valueOf(borrow.getId()));
            memberEmailLabel.setText(user.getEmail());
            memberNameLabel.setText(user.getName());
            bookIsbnLabel.setText(borrow.getBookIsbn());
            bookTitleLabel.setText(borrowedBook.getTitle());
            borrowDateLabel.setText(borrow.getBorrowDate().toString());
            dueDateLabel.setText(borrow.getDueDate().toString());
            if (borrow.getReturnDate() != null) {
                returnDateLabel.setText(borrow.getReturnDate().toString());
            } else {
                returnDateLabel.setText("Not returned yet!");
            }
            statusLabel.setText(borrow.getStatus().toString());
            fineLabel.setText(String.valueOf(borrow.getFine()));

            if (borrowedBook.getImagePath() != null && !borrowedBook.getImagePath().isEmpty()) {
                String projectDir = System.getProperty("user.dir");
                String booksDir = projectDir + "/src/main/resources/images/book/";
                String path = booksDir + borrowedBook.getImagePath();

                Image image = ImageCache.getInstance().getImage(path, booksDir + "defaultBook.jpg");
                bookImageView.setImage(image);
            } else {
                loadDefaultBookImage(bookImageView);
            }
        }
    }

    @FXML
    void back() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

}
