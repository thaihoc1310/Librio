package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import librio.models.Borrow;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

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
        private Label memberIdLabel;

        @FXML
        private Label returnDateLabel;

        @FXML
        private Label statusLabel;

        public void setBorrow(Borrow borrow) {
            this.borrow = borrow;
        }

    private void populateFields() {
        if (borrow != null) {
            borrowIdLabel.setText(borrow.getId());
            memberIdLabel.setText(borrow.getMemberId());
            bookIsbnLabel.setText(borrow.getBookIsbn());

            borrowDateLabel.setText(borrow.getBorrowDate().toString());
            dueDateLabel.setText(borrow.getDueDate().toString());
            bookTitleLabel.setText();


            if (borrow.getImagePath() != null && !borrow.getImagePath().isEmpty()) {
                String projectDir = System.getProperty("user.dir");
                String booksDir = projectDir + "/src/main/resources/images/book/";
                String path = booksDir + borrow.getImagePath();
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
