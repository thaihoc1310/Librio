package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
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
    private Label isbn;
    @FXML
    private Label year;
    @FXML
    private Label publisher;
    @FXML
    private Text description;
    @FXML
    private ImageView bookCoverImage;
    @FXML
    private AnchorPane bookDetailsPane;

    private Book book;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setBook(Book book){
        this.book = book;
        setBookDetails();
    }

    public void setBookDetails() {
        title.setText(book.getTitle());
        author.setText(book.getAuthor());
        year.setText("Published:    "+book.getYearPublished());
        isbn.setText("ISBN:   " + book.getIsbn());
        publisher.setText("Publisher:   " + book.getPublisher());
        description.setText(book.getDescription());
        try {
            bookCoverImage.setImage(new Image(book.getImagePath()));
        } catch (Exception e) {
            bookCoverImage.setImage(new Image(getClass().getResource("/images/book/defaultBook.jpg").toExternalForm()));
        }


    }

    @FXML
    private void cancelBookDetail(){
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) title.getScene().getWindow();
        stage.close();
    }
}
