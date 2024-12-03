package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import librio.cache.ImageCache;
import librio.database.DatabaseConnection;
import librio.models.Book;
import librio.models.Feedback;
import librio.models.User;
import librio.session.Session;
import librio.util.DatabaseUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static librio.util.DatabaseUtil.checkIfUserBorrowedBook;
import static librio.util.DesignUtil.*;

/**
 * Controller class responsible for managing the book details UI in the application.
 * This class handles the display and interaction of a book's details including
 * metadata, cover image, description, QR code, and user feedback.
 *
 * Fields:
 * - DESCRIPTION_LIMIT: Maximum character limit for the book's description when truncated.
 * - author: Label to display the author's name.
 * - categoryLabel: Label to display the book's category.
 * - isbnLabel: Label to display the book's ISBN.
 * - languageLabel: Label for displaying the language in which the book is written.
 * - pageCountLabel: Label for displaying the page count of the book.
 * - publishedLabel: Label for displaying the publication year.
 * - publisherLabel: Label to display the book's publisher.
 * - bookCoverImage: Image view to show the book's cover.
 * - qrCodeImageView: Image view to display the QR code for the book.
 * - bookDetailsPane: Pane containing the book detail components.
 * - confirmButton: Button to confirm borrowing or indicate book status.
 * - descriptionText: Text area for displaying the book description.
 * - moreLessLabel: Label to toggle between more or less of the description.
 * - title: Title of the book displayed in the UI.
 * - feedbackContainer: Container for displaying user feedback about the book.
 * - starBox: Box to display star ratings of the book.
 * - isExpanded: Flag indicating if the book description is expanded.
 * - fullDescription: Full description text of the book.
 * - book: Book object containing all details for the UI.
 * - feedbackList: List to hold feedback entries.
 */
public class BookDetailController implements Initializable {
    private static final int DESCRIPTION_LIMIT = 500;

    @FXML
    private Label author, categoryLabel, isbnLabel, languageLabel, pageCountLabel, publishedLabel, publisherLabel;
    @FXML
    private ImageView bookCoverImage, qrCodeImageView;
    @FXML
    private ScrollPane bookDetailsPane;
    @FXML
    private Button confirmButton;
    @FXML
    private Text descriptionText, moreLessLabel, title;
    @FXML
    private VBox feedbackContainer;
    @FXML
    private HBox starBox;

    private boolean isExpanded = false;

    private String fullDescription;

    private Book book;

    private final List<Feedback> feedbackList = new ArrayList<>();


    /**
     * Initializes the controller class. This method is automatically called after the fxml file
     * has been loaded. It sets the initial text for the moreLessLabel and binds a click event
     * to toggle the description view.
     *
     * @param location The location used to resolve relative paths for the root object, or null
     *                 if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root
     *                  object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        moreLessLabel.setText("more");
        moreLessLabel.setOnMouseClicked(event -> toggleDescription());
    }

    /**
     * Sets the book object to the controller and updates the UI components.
     *
     * @param book the Book object containing details to be displayed in the UI
     */
    public void setBook(Book book) {
        this.book = book;
        setBookDetails();
        loadFeedbacksFromDatabase();
        displayRating();
    }

    /**
     * Displays the average rating of the book as a series of star icons in the UI.
     * Fills the stars based on the book's average rating and adds a numeric representation
     * of the rating alongside the total number of borrows.
     *
     * The method visualizes the whole part of the rating with full stars, the fractional part
     * with a partially filled star, and the remainder with empty stars up to a total of five stars.
     * The rating is also displayed as text with the format "rating (totalBorrows)".
     *
     * It uses images of stars and their dimensions are set to 15x15 pixels.
     * If the rating includes a decimal, a partial star image is clipped to represent the fraction.
     */
    public void displayRating() {
        double rating = book.getAverageOfRating();
        int fullStars = (int) rating;
        double decimalPart = rating - fullStars;

        for (int i = 1; i <= 5; i++) {
            StackPane starPane = new StackPane();

            ImageView fullStar = new ImageView(new Image(getClass().getResource("/icons/MemberIcon/Star.png").toExternalForm()));
            fullStar.setFitHeight(15);
            fullStar.setFitWidth(15);

            ImageView emptyStar = new ImageView(new Image(getClass().getResource("/icons/MemberIcon/Star_notfill.png").toExternalForm()));
            emptyStar.setFitHeight(15);
            emptyStar.setFitWidth(15);

            if (i <= fullStars) {
                starPane.getChildren().add(fullStar);
            } else if (i == fullStars + 1 && decimalPart > 0) {
                starPane.getChildren().addAll(emptyStar, fullStar);
                Rectangle clip = new Rectangle(15 * decimalPart, 15);
                fullStar.setClip(clip);
            } else {
                starPane.getChildren().add(emptyStar);
            }
            starBox.getChildren().add(starPane);
        }
        starBox.getStyleClass().add("star-box");

        Label ratingLabel = new Label("  " + rating + " (" + getTotalBorrows() + ")");
        ratingLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #4C2113; -fx-font-weight: bold;");
        ratingLabel.setAlignment(Pos.CENTER_LEFT);

        starBox.getChildren().add(ratingLabel);
    }

    /**
     * Updates the user interface components with the details of the current book.
     * This method populates text labels with the book's metadata such as title, author,
     * ISBN, publisher, page count, published year, category, and language. Additionally,
     * it manages the display of the book's description with a character limit; if the
     * description exceeds this limit, it truncates the text and enables a control to
     * toggle the full view.
     *
     * The method also retrieves and displays the corresponding book cover image using
     * the ImageCache, and adapts its dimensions while maintaining a specific aspect
     * ratio. Furthermore, a QR code related to the book is generated and displayed,
     * and the confirm button's state is set accordingly.
     */
    public void setBookDetails() {
        title.setText(book.getTitle());
        author.setText("   " + book.getAuthor());
        isbnLabel.setText("ISBN:     " + book.getIsbn());
        publisherLabel.setText("Publisher:     " + book.getPublisher());
        pageCountLabel.setText("Page count:     " + book.getNumberOfPages());
        publishedLabel.setText("Published:     " + book.getYearPublished());
        categoryLabel.setText("Category:     " + book.getCategory());
        languageLabel.setText("Language:     " + book.getLanguage());
        fullDescription = book.getDescription();
        if (fullDescription.length() > DESCRIPTION_LIMIT) {
            descriptionText.setText(fullDescription.substring(0, DESCRIPTION_LIMIT) + "...");
            moreLessLabel.setVisible(true);
        } else {
            descriptionText.setText(fullDescription);
            moreLessLabel.setVisible(false);
        }

        String projectDir = System.getProperty("user.dir");
        String booksDir = projectDir + "/src/main/resources/images/book/";
        String path = booksDir + book.getImagePath();

        File file = new File(path);
        if(!file.exists()){
            path = booksDir + "defaultBook.jpg";
        }
        Image image = ImageCache.getInstance().getImage(path, path);
        cropToAspectRatio(image, bookCoverImage, 217, 315);
        generateAndDisplayQRCode(qrCodeImageView,book);
        setConfirmButton();
    }

    /**
     * Configures the confirm button based on the availability of the book and
     * the borrowing status of the logged-in user. This method checks if there
     * are available copies of the book and whether the user has already borrowed
     * it, and updates the button's text, color, and enabled state accordingly.
     * If no copies are available, the button is labeled "Out of stock" and
     * disabled. If the user has already borrowed the book, the button displays
     * "Borrowing" and is also disabled. Otherwise, the button is set to "Borrow,"
     * indicating the user can proceed to borrow the book.
     */
    private void setConfirmButton() {
        int availableCopy = book.getAvailableCopy();
        boolean isAlreadyBorrowed = checkIfUserBorrowedBook(Session.getInstance().getLoggedInUser(), book);
        if (availableCopy == 0) {
            updateBorrowButton("Out of stock", "#9e4b3e", false);
        } else if (isAlreadyBorrowed) {
            updateBorrowButton("Borrowing", "#b57a3e", false);
        } else {
            confirmButton.setText("Borrow");
        }
    }

    /**
     * Updates the visual appearance and functionality of the borrow button in the user interface
     * based on the provided parameters.
     *
     * @param text the text to be displayed on the confirm button
     * @param color the background color of the confirm button in a CSS-compatible format
     * @param isEnabled a flag indicating whether the confirm button should be enabled and clickable
     */
    private void updateBorrowButton(String text, String color, boolean isEnabled) {
        confirmButton.setText(text);
        confirmButton.setStyle("-fx-background-color: " + color);
        confirmButton.setDisable(!isEnabled);
        confirmButton.setCursor(isEnabled ? Cursor.HAND : Cursor.DEFAULT);
    }

    /**
     * Toggles the display of the book description between a truncated version
     * and the full text. If the description is currently expanded, it sets
     * the text to a truncated version ending with ellipsis, followed by " more"
     * in the moreLessLabel. If the description is not expanded, it displays
     * the full text and changes the moreLessLabel text to " less". It also
     * toggles the isExpanded state to track the current view state.
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
     * Handles the action of canceling the current book detail view.
     * This method is typically invoked when the user opts to close
     * the book detail interface without making changes or proceeding
     * with further actions. It leverages the `closeStage()` method
     * to close the current window or stage, effectively dismissing
     * the book detail view from the user's display.
     */
    @FXML
    private void cancelBookDetail() {
        closeStage();
    }

    /**
     * Loads feedback entries from the database for a specific book and updates the user interface
     * to display the feedback in a structured format. This method clears existing feedback data and
     * configures the display container for feedback items.
     *
     * The feedback data is retrieved using a query that selects feedback related to the current book
     * from the database based on its ID. For each feedback entry retrieved, a user interface component
     * is created, which includes the user's avatar image, name, rating displayed as stars, date of feedback,
     * and the feedback comment. The feedback is shown in a stylized format within the feedback container.
     * If no feedback is found, an appropriate message is displayed indicating the absence of feedback for
     * the book.
     *
     * This method handles SQL exceptions by printing the stack trace.
     */
    private void loadFeedbacksFromDatabase() {
        feedbackList.clear();
        feedbackContainer.setSpacing(15);
        feedbackContainer.setStyle("-fx-padding: 10 10 10 10");
        String query = "SELECT id, book_id, member_id, rating, about, created_at FROM feedbacks WHERE book_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, String.valueOf(book.getId()));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String memberId = resultSet.getString("member_id");
                int rating = resultSet.getInt("rating");
                String about = resultSet.getString("about");
                String createdAt = resultSet.getString("created_at");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime localDateTime = LocalDateTime.parse(createdAt, formatter);
                Instant createdAtInstant = localDateTime.toInstant(ZoneOffset.UTC);


                Feedback feedback1 = new Feedback(id, String.valueOf(book.getId()), memberId, rating, about, createdAtInstant);
                feedbackList.add(feedback1);

                HBox feedbackBox = new HBox();
                feedbackBox.setSpacing(15);
                feedbackBox.setStyle("-fx-padding: 10; -fx-background-color: #F4F4F4; -fx-border-color: #E0E0E0; -fx-border-radius: 5; -fx-background-radius: 5;");
                feedbackBox.setAlignment(Pos.TOP_LEFT);

                User user = DatabaseUtil.getUserById(memberId);

                String projectDir = System.getProperty("user.dir");
                String avatarsDir = projectDir + "/src/main/resources/images/user/";
                String path = avatarsDir + user.getAvatar();
                ImageView avatar = new ImageView();
                avatar.setFitWidth(50);
                avatar.setFitHeight(50);

                File file = new File(path);
                if(!file.exists()){
                    path = avatarsDir + "Male User.png";
                }
                Image image = ImageCache.getInstance().getImage(path, path);
                cropAndClipToCircle(image, avatar, 25);


                VBox detailsBox = new VBox();
                detailsBox.setSpacing(5);

                Text borrowerNameText = new Text(user.getName());
                borrowerNameText.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

                StringBuilder stars = new StringBuilder();
                StringBuilder stars2 = new StringBuilder();

                for (int i = 0; i < rating; i++) {
                    stars.append("★");
                }

                for (int i = rating; i < 5; i++) {
                    stars2.append("★");
                }

                Text ratingText = new Text("Rating: " + stars);
                ratingText.setStyle("-fx-font-size: 12; -fx-fill: #FFB700;");

                Text ratingTextNotFill = new Text(stars2.toString());
                ratingTextNotFill.setStyle("-fx-font-size: 12; -fx-fill: #acacac;");

                HBox ratingBox = new HBox();
                ratingBox.setSpacing(0);
                ratingBox.getChildren().addAll(ratingText, ratingTextNotFill);


                LocalDateTime formattedDate = LocalDateTime.ofInstant(createdAtInstant, ZoneOffset.UTC);
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                Text dateText = new Text("Return at: " + formattedDate.format(dateFormatter));
                dateText.setStyle("-fx-font-size: 12; -fx-fill: #666666;");

                Text commentText = new Text(about);
                commentText.setStyle("-fx-font-size: 13; -fx-fill: #333333;");

                detailsBox.getChildren().addAll(borrowerNameText, ratingBox, dateText, commentText);
                feedbackBox.getChildren().addAll(avatar, detailsBox);
                feedbackContainer.getChildren().add(feedbackBox);
            }

            if (feedbackList.isEmpty()) {
                Label noBooksLabel = new Label("No rating provided for this book");
                noBooksLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #4C2113; -fx-font-weight: bold;");
                noBooksLabel.setAlignment(Pos.CENTER);

                VBox container = new VBox(noBooksLabel);
                container.setAlignment(Pos.CENTER);
                container.setPrefHeight(feedbackContainer.getHeight() - 25);
                container.setStyle("-fx-padding: 20;");
                feedbackContainer.getChildren().addAll(container);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the current stage or window associated with the book detail view.
     * This method retrieves the current window using the `title` control's scene
     * and invokes the `close` operation on the corresponding stage. It is typically
     * used to dismiss the book detail interface.
     */
    private void closeStage() {
        Stage stage = (Stage) title.getScene().getWindow();
        stage.close();
    }

    /**
     * Retrieves the total number of times the current book has been borrowed from the database.
     *
     * @return the total count of borrows for the book identified by its ISBN.
     */
    private int getTotalBorrows() {
        int total = 0;
        String query = "select count(id) from borrows where book_isbn = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, String.valueOf(book.getIsbn()));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                total = resultSet.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * Opens the borrow confirmation pane to proceed with borrowing a book. This method loads
     * and displays a new window with the borrow confirmation interface, effectively pausing
     * interaction with the underlying stage until the borrow confirmation window is closed.
     *
     * The method applies a temporary dimming effect to the current stage to highlight the
     * confirmation pane. It handles positioning the new stage centrally relative to the
     * current window and employs a modal approach to block input to other windows until
     * the user closes the confirmation pane.
     *
     * If an error occurs while loading the FXML resource, the method catches and logs
     * the IOException.
     */
    @FXML
    private void openBorrowConfirmationPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/ConfirmBorrow.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) author.getScene().getWindow();
            ConfirmBorrow confirmBorrow = loader.getController();
            confirmBorrow.setBook(book);
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(currentStage);
            stage.setOnShown(event -> {
                stage.setX(currentStage.getX() + (currentStage.getWidth() - stage.getWidth()) / 2);
                stage.setY(currentStage.getY() + (currentStage.getHeight() - stage.getHeight()) / 2);
            });

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);
            });
            stage.showAndWait();
            setConfirmButton();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
