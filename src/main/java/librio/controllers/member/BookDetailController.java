package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import librio.models.Book;

import java.net.URL;
import java.util.ResourceBundle;

public class BookDetailController implements Initializable {

    @FXML
    private Text title;

    @FXML
    private Label author;

    @FXML
    private Label yearPagesAndPublisher;

    @FXML
    private Label isbn;

    @FXML
    private Label copies;

    @FXML
    private ImageView bookCoverImage;

    @FXML
    private Text description;

    @FXML
    private Button borrowButton;

    @FXML
    private ImageView exitButton;

    @FXML
    private Button backButton; // Nút back thứ hai (nếu có)

    // Biến lưu hành động quay lại
    private Runnable onBackAction;

    // Phương thức khởi tạo để nhận thông tin sách và hiển thị
    public void setBookDetails(Book book) {

        title.setText(book.getTitle());
        author.setText(book.getAuthor());

        // Hiển thị năm, số trang và nhà xuất bản
        yearPagesAndPublisher.setText(book.getYearPublished() + "    " + book.getNumberOfPages() + " pages (" + book.getPublisher() + ")");

        isbn.setText("ISBN: " + book.getIsbn());

        // Kiểm tra và đặt ảnh bìa sách
        try {
            bookCoverImage.setImage(new Image(book.getImagePath()));
        } catch (Exception e) {
            System.out.println("Không thể tải ảnh, sử dụng ảnh mặc định.");
            bookCoverImage.setImage(new Image(getClass().getResource("/images/book/defaultBook.jpg").toExternalForm()));
        }

        copies.setText("Copies: " + book.getQuantityCopy());
        description.setText(book.getDescription());
    }

    // Đặt hành động quay lại
    public void setOnBackAction(Runnable onBackAction) {
        this.onBackAction = onBackAction;
    }

    // Phương thức quay lại màn hình danh sách sách
    @FXML
    private void openBookScene() {
        if (onBackAction != null) {
            onBackAction.run();  // Quay lại Scene trước đó
        } else {
            System.out.println("Hành động quay lại chưa được thiết lập.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Đặt hành động cho exitButton
        exitButton.setCursor(Cursor.HAND);
        exitButton.setOnMouseClicked(event -> openBookScene());  // Sử dụng cho ImageView

        // Đặt hành động cho backButton (nếu có nút "Back")
        if (backButton != null) {
            backButton.setOnAction(event -> openBookScene());
        }
    }
}
