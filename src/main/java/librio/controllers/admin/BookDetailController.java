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
import librio.session.Session;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static librio.util.DesignUtil.generateAndDisplayQRCode;
import static librio.util.DesignUtil.loadDefaultBookImage;

/**
 * The BookDetailController class provides a detailed view of a book's information in a UI.
 * It implements the Initializable interface to handle the initialization of various UI components
 * associated with displaying detailed information about a book.
 */
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

    /**
     * Initializes the controller after its root element has been completely processed.
     *
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the resources are not specified.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        moreLessLabel.setText("more");
        moreLessLabel.setOnMouseClicked(event -> toggleDescription());
        initScroll();

    }

    /**
     * Sets the book for the BookDetailController and populates the fields
     * related to the book details.
     *
     * @param book the Book object to be set and displayed in the controller
     */
    public void setBook(Book book) {
        this.book = book;
        populateFields();
    }

    /**
     * Populates various UI components with the details of a book.
     *
     * This method updates the text and visibility of labels, text fields,
     * and image views in the UI to reflect the properties of the book instance
     * associated with this controller.
     *
     * The method performs the following actions:
     * - Sets the text of labels with the book's title, author, ISBN, ID, category,
     *   available copy quantity, average rating, publisher, year published, language,
     *   and number of pages.
     * - Conditionally truncates and displays the book's description if it
     *   exceeds a predefined character limit, showing either the full text or
     *   a shortened version with an ellipsis.
     * - Determines and sets the book's image by loading it from the file system.
     *   If no image is available, a default image is used.
     * - Generates and displays a QR code representing a URL related to the book.
     *
     * This method assumes the 'book' field is not null and that various UI
     * components have been initialized. It does not return any value.
     */
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
                File file = new File(path);
                if(!file.exists()){
                    path = booksDir + "defaultBook.jpg";
                }
                Image image = ImageCache.getInstance().getImage(path, path);
                bookImageView.setImage(image);

            } else {
                loadDefaultBookImage(bookImageView);
            }
            generateAndDisplayQRCode(qrCodeImageView, book);
        }
    }

    /**
     * Toggles the display of the book description between a truncated version and the full text.
     *
     * If the description is currently expanded, it truncates the description to a predefined limit
     * and appends ellipsis, while changing the label text to indicate more details can be shown.
     * Otherwise, it displays the full description and changes the label text to indicate less details
     * can be shown. The expanded state is then toggled.
     */
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

    /**
     * Handles the action of navigating back from the current view.
     * This method will close the current window when triggered.
     */
    @FXML
    private void back() {
        closeWindow();
    }

    /**
     * Closes the current window associated with the scene containing the isbnLabel.
     * This method retrieves the window from the current scene and closes it,
     * effectively terminating any user interactions with that particular window.
     */
    private void closeWindow() {
        Stage stage = (Stage) isbnLabel.getScene().getWindow();
        stage.close();
    }

    /**
     * Initializes scroll behavior for the scrollPane by adding a scrolling effect to the thumb node.
     * When the scroll event occurs, a "scrolling" style class is temporarily added to the scroll
     * thumb, which is removed after a delay of 2000 milliseconds.
     */
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
