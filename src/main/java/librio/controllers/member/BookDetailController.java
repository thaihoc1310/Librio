package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
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

    // Phương thức khởi tạo để nhận thông tin sách và hiển thị
    public void setBookDetails(Book book) {

        title.setText(book.getTitle());
        author.setText(book.getAuthor());
        // Hiển thị năm, số trang và nhà xuất bản
        yearPagesAndPublisher.setText(book.getYearPublished() + "    " + book.getNumberOfPages() + " pages (" + book.getPublisher() + ")");
        isbn.setText("ISBN: " + book.getIsbn());
        bookCoverImage.setImage(new Image(book.getImagePath()));
        copies.setText("Copies: " + book.getQuantityCopy());
        description.setText(book.getDescription());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        exitButton.setCursor(Cursor.HAND);
    }
}
