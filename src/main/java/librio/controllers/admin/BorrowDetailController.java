package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import librio.models.Book;
import librio.models.Borrow;
import librio.models.User;
import librio.util.DatabaseUtil;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.*;
import static librio.util.DesignUtil.loadDefaultBookImage;

public class BorrowDetailController implements Initializable {
        private Borrow borrow;

        @FXML
        private Button backButton;

        @FXML
        private ImageView bookImageView;

        @FXML
        private Label bookIsbnLabel;

        @FXML
        private Label bookTitleLabel;

        @FXML
        private Label borrowDateLabel;

        @FXML
        private Label borrowIdLabel;

        @FXML
        private Label dueDateLabel;

        @FXML
        private Label fineLabel;

        @FXML
        private Label memberEmailLabel;

        @FXML
        private Label returnDateLabel;

        @FXML
        private Label statusLabel;

        @FXML
        private Label memberNameLabel;

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
            if(borrow.getReturnDate() != null) {
                returnDateLabel.setText(borrow.getReturnDate().toString());
            }else {
                returnDateLabel.setText("Not returned yet!");
            }
            statusLabel.setText(borrow.getStatus().toString());
            fineLabel.setText(String.valueOf(borrow.getFine()));


            if (borrowedBook.getImagePath() != null && !borrowedBook.getImagePath().isEmpty()) {
                String projectDir = System.getProperty("user.dir");
                String booksDir = projectDir + "/src/main/resources/images/book/";
                String path = booksDir + borrowedBook.getImagePath();
                File file = new File(path);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    bookImageView.setImage(image);
                } else {
                    // Sử dụng ảnh mặc định nếu không tìm thấy file ảnh sách
                    loadDefaultBookImage(bookImageView);
                }
            } else {
                // Sử dụng ảnh mặc định nếu imagePath là null hoặc rỗng
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


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

}
