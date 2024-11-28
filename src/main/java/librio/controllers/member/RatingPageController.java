package librio.controllers.member;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import librio.cache.ImageCache;
import librio.session.Session;
import librio.database.DatabaseConnection;
import librio.models.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import static librio.util.DatabaseUtil.updateBookAverageRating;

/**
 * The `RatingPageController` class is responsible for managing and initializing the user interface for providing
 * ratings and comments on a borrowed book. It includes methods for handling the display and interaction with star ratings,
 * submitting feedback to the database, and configuring the controller's UI elements with book details.
 */
public class RatingPageController {

    @FXML
    private Label authorNameLabel;

    @FXML
    private ImageView bookImage;

    @FXML
    private TextArea commentBox;

    @FXML
    private HBox starBox;

    @FXML
    private Button submitButton;

    @FXML
    private Label titleLabel;

    private Book book;
    private int borrowId;

    private final int starCount = 5;
    private int selectedStars = 0;

    private final Image emptyStar = new Image(getClass().getResource("/icons/MemberIcon/Star_notfill.png").toExternalForm());
    private final Image fullStar = new Image(getClass().getResource("/icons/MemberIcon/Star.png").toExternalForm());

    /**
     * Initializes the UI components related to star ratings within the RatingPageController.
     * This method creates and adds a graphical representation of stars for the purpose of
     * rating display and selection. It iteratively sets up each star pane using the
     * createStarPane method and places them into the starBox container.
     * The method ensures that the starBox component gains focus when the application is run.
     */
    @FXML
    public void initialize() {
        for (int i = 0; i < starCount; i++) {
            Pane starPane = createStarPane(i);
            starBox.getChildren().add(starPane);
        }
        Platform.runLater(() -> {
            starBox.requestFocus();
        });
    }

    /**
     * Creates a Pane containing a star ImageView for the purpose of displaying and interacting with star ratings.
     * This method initializes the appearance of the star and sets up event handlers for mouse interactions to
     * update and select star ratings.
     *
     * @param index the index of the star in the rating scale starting from zero
     * @return a Pane object containing the configured star ImageView and interactive behavior
     */
    private Pane createStarPane(int index) {

        ImageView star = new ImageView(emptyStar);
        star.setFitHeight(20);
        star.setFitWidth(20);
        star.setX(10);
        Pane starPane = new Pane(star);
        starPane.setPrefSize(45, 20);
        starPane.setStyle("-fx-cursor: hand;");

        starPane.setOnMouseEntered(event -> {
            updateStars(index + 1, fullStar);
        });

        starPane.setOnMouseExited(event -> {
            updateStars(selectedStars, fullStar);
        });

        starPane.setOnMouseClicked(event -> {
            selectedStars = index + 1;
            updateStars(selectedStars, fullStar);
        });

        return starPane;
    }

    /**
     * Updates the star rating display based on the specified star count.
     * This method updates the visual representation of stars in the
     * starBox component, setting each star to either a full or empty
     * image based on the provided starCount.
     *
     * @param starCount the number of full stars to display
     * @param fullStar  the Image object representing a full star
     */
    private void updateStars(int starCount, Image fullStar) {
        for (int i = 0; i < starBox.getChildren().size(); i++) {
            Pane starPane = (Pane) starBox.getChildren().get(i);
            ImageView star = (ImageView) starPane.getChildren().getFirst();
            if (i < starCount) {
                star.setImage(fullStar);
            } else {
                star.setImage(new Image(getClass().getResource("/icons/MemberIcon/Star_notfill.png").toExternalForm())); // Đổi ngôi sao rỗng
            }
        }
    }

    /**
     * Handles the action of confirming and submitting feedback for a specific book borrowing event.
     * This method performs the following tasks:
     * - Establishes a connection to the database.
     * - Inserts a new feedback record into the database with details including
     * the member's ID, book ID, borrow ID, rating, comments, creator's email,
     * and the current timestamp.
     * - If the feedback is successfully inserted, updates the average rating
     * for the book.
     * - Closes the current stage.
     * <p>
     * If a SQL exception occurs during database interaction, the exception
     * stack trace will be printed.
     */
    @FXML
    private void confirmAction() {
        String query = "INSERT INTO feedbacks (member_id, book_id, borrow_id, rating, about, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, Session.getInstance().getLoggedInUser().getId());
            statement.setInt(2, book.getId());
            statement.setInt(3, borrowId);
            statement.setInt(4, selectedStars);
            statement.setString(5, commentBox.getText());
            statement.setString(6, Session.getInstance().getLoggedInUser().getEmail());
            statement.setString(7, LocalDateTime.now().toString());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                updateBookAverageRating(book.getIsbn());
            }
            closeStage();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Closes the current stage window for the application.
     * This method retrieves the current stage from the JavaFX scene graph
     * by obtaining the window from the authorNameLabel's scene and calls
     * the close method on the stage to terminate the window.
     */
    @FXML
    private void closeStage() {
        Stage stage = (Stage) authorNameLabel.getScene().getWindow();
        stage.close();
    }

    /**
     * Sets the book and borrow ID for the current context and updates the UI elements accordingly.
     * This method configures the labels and image related to the book and assigns the borrow ID.
     *
     * @param book the Book object containing information such as title, author, and image path
     * @param borrowId the unique identifier for the borrowing event associated with the book
     */
    public void setBookAndBorrowId(Book book, int borrowId) {
        this.book = book;
        this.borrowId = borrowId;

        titleLabel.setText(book.getTitle());
        authorNameLabel.setText(book.getAuthor());

        String projectDir = System.getProperty("user.dir");
        String booksDir = projectDir + "/src/main/resources/images/book/";
        String path = booksDir + book.getImagePath();
        Image image = ImageCache.getInstance().getImage(path, booksDir + "Male User.png");
        bookImage.setImage(image);
    }
}
