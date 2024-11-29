package librio.controllers.admin;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import librio.cache.ImageCache;
import librio.models.Book;

import java.net.URL;
import java.util.ResourceBundle;

import static librio.util.DesignUtil.generateAndDisplayQRCode;
import static librio.util.DesignUtil.loadDefaultBookImage;

public class BookDetailController implements Initializable {
    private static final int DESCRIPTION_LIMIT = 500;

    private Book book;
    @FXML
    private Label bookIdLabel;
    @FXML
    private Label bookTitleLabel;
    @FXML
    private Label authorLabel;
    @FXML
    private Label isbnLabel;
    @FXML
    private Label categoryLabel;
    @FXML
    private Label averageOfRatingLabel;
    @FXML
    private Label quantityOfAvailableCopyLabel;
    @FXML
    private Label publisherLabel;
    @FXML
    private Label yearPublishedLabel;
    @FXML
    private Label languageLabel;
    @FXML
    private Label numberOfPagesLabel;
    @FXML
    private Text descriptionText;
    @FXML
    private ImageView bookImageView;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ImageView qrCodeImageView;
    @FXML
    private Text moreLessLabel;
    @FXML
    private AnchorPane bookDetailsPane;

    private boolean isExpanded = false;

    private String fullDescription;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        moreLessLabel.setText("more");
        moreLessLabel.setOnMouseClicked(event -> toggleDescription());
        initScroll();

    }

    public void setBook(Book book) {
        this.book = book;
        populateFields();
    }

    private void populateFields() {
        if (book != null) {
            bookTitleLabel.setText(book.getTitle());
            authorLabel.setText(book.getAuthor());
            isbnLabel.setText(book.getIsbn());
            bookIdLabel.setText("ID :   " + book.getId());
            categoryLabel.setText("Category :   " + book.getCategory());
            quantityOfAvailableCopyLabel.setText("Quantity of available copy :   " + book.getAvailableCopy());
            averageOfRatingLabel.setText("Average of rating :   " + book.getAverageOfRating());
            publisherLabel.setText("Publisher :   " + book.getPublisher());
            yearPublishedLabel.setText("Year published :   " + book.getYearPublished());
            languageLabel.setText("Language :   " + book.getLanguage());
            numberOfPagesLabel.setText("Number of pages :   " + book.getNumberOfPages());
            fullDescription = book.getDescription();
            if (fullDescription.length() > DESCRIPTION_LIMIT) {
                bookDetailsPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
                bookDetailsPane.setMinHeight(Region.USE_COMPUTED_SIZE);
                descriptionText.setText(fullDescription.substring(0, DESCRIPTION_LIMIT) + "...");
                moreLessLabel.setVisible(true);
            } else {
                descriptionText.setText(fullDescription);
                moreLessLabel.setVisible(false);
            }

            if (book.getImagePath() != null && !book.getImagePath().isEmpty()) {
                String projectDir = System.getProperty("user.dir");
                String booksDir = projectDir + "/src/main/resources/images/book/";
                String path = booksDir + book.getImagePath();

                Image image = ImageCache.getInstance().getImage(path, booksDir + "defaultBook.jpg");
                bookImageView.setImage(image);

            } else {
                loadDefaultBookImage(bookImageView);
            }
            generateAndDisplayQRCode(qrCodeImageView, book);
        }
    }

    private void toggleDescription() {
        if (isExpanded) {
            descriptionText.setText(fullDescription.substring(0, DESCRIPTION_LIMIT) + "...");
            moreLessLabel.setText(" more");
        } else {
            descriptionText.setText(fullDescription);
            moreLessLabel.setText(" less");
        }
        isExpanded = !isExpanded;
    }

    @FXML
    private void back() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) isbnLabel.getScene().getWindow();
        stage.close();
    }

    private void initScroll() {
        scrollPane.setOnScroll(event -> {
            Node thumb = scrollPane.lookup(".thumb");

            if (thumb != null) {

                thumb.getStyleClass().add("scrolling");

                new Timeline(new KeyFrame(Duration.millis(2000), e -> {
                    thumb.getStyleClass().remove("scrolling");
                })).play();
            }
        });
    }

}
