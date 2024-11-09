package librio.controllers.member;

import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import librio.database.DatabaseConnection;
import librio.models.Book;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HomePageController implements Initializable {

    @FXML
    private ScrollPane mainScroll;

    @FXML
    private ImageView avatar;

    @FXML
    private ComboBox<?> filterBox;

    @FXML
    private Button leftMainBannerButton;

    @FXML
    private Button leftToprateButton;

    @FXML
    private Button leftScrollButton2;

    @FXML
    private ImageView mainBanner0;

    @FXML
    private ImageView mainBanner1;

    @FXML
    private ImageView mainBanner2;

    @FXML
    private ScrollPane mainBannerScroll;

    @FXML
    private HBox mainBannerContainer;

    @FXML
    private Button rightMainBannerButton;

    @FXML
    private Button rightToprateButton;

    @FXML
    private Button rightScrollButton2;

    @FXML
    private ImageView searchButton;

    @FXML
    private TextField searchTextField;

    @FXML
    private HBox topRateContainer;

    @FXML
    private ScrollPane topRateScroll;

    private Timeline autoScrollTimeline;

    private List<Book> topRateList = new ArrayList<>();

    private int currentRatingBookIndex = 0;
    private static final int TOTAL_BOOKS = 18;
    private static final int BOOKS_PER_PAGE = 6;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        leftMainBannerButton.setOnMouseClicked(event -> scrollMainBanner(-1));
        rightMainBannerButton.setOnMouseClicked(event -> scrollMainBanner(1));
        leftToprateButton.setOnMouseClicked(event -> scrollTopRate(-1));
        rightToprateButton.setOnMouseClicked(event -> scrollTopRate(1));
        startAutoScroll();
        loadTopRatedBooks();
    }

    private void scrollMainBanner(int direction) {
        stopAutoScroll();
        double currentHValue = mainBannerScroll.getHvalue();
        final double targetHValue = getTargetHValue(direction, currentHValue);

        Node currentBanner = getCurrentBanner(currentHValue);
        Node nextBanner = getCurrentBanner(targetHValue);
        if (currentBanner != null && nextBanner != null) {
            // Fade out current banner
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), currentBanner);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.7);

            // Fade in next banner
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), nextBanner);
            fadeIn.setFromValue(0.7);
            fadeIn.setToValue(1.0);

            // Set scroll position after fade out
            fadeOut.setOnFinished(event -> {
                mainBannerScroll.setHvalue(targetHValue);
                fadeIn.play();
            });

            fadeOut.play();
        }
        startAutoScroll();
    }

    private double getTargetHValue(int direction, double currentHValue) {
        double scrollAmount = 1.0 / (mainBannerContainer.getChildren().size() - 1);
        final double targetHValue;

        if (direction == -1 && currentHValue == 0) {
            targetHValue = 1;
        } else if (direction == 1 && currentHValue == 1) {
            targetHValue = 0;
        } else {
            targetHValue = currentHValue + (scrollAmount * direction);
        }
        return targetHValue;
    }

    private Node getCurrentBanner(double hValue) {
        int index = (int) Math.round(hValue * (mainBannerContainer.getChildren().size() - 1));
        if (index >= 0 && index < mainBannerContainer.getChildren().size()) {
            return mainBannerContainer.getChildren().get(index);
        }
        return null;
    }

    private void startAutoScroll() {
        autoScrollTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> scrollMainBanner(1)));
        autoScrollTimeline.setCycleCount(Timeline.INDEFINITE);
        autoScrollTimeline.play();
    }
    private void stopAutoScroll() {
        if (autoScrollTimeline != null) {
            autoScrollTimeline.stop();
        }
    }

    private void loadTopRatedBooks() {
        topRateList.clear();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String  query = "SELECT id, title, author, isbn, category, publisher, quantity_copy," +
                    " average_of_rating, year_published, language, number_of_pages, description," +
                    " book_image FROM books ORDER BY average_of_rating DESC LIMIT ?";
            PreparedStatement  preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, TOTAL_BOOKS);


            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Integer id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String isbn = resultSet.getString("isbn");
                String category = resultSet.getString("category");
                String publisher = resultSet.getString("publisher");
                Integer quantityCopy = resultSet.getInt("quantity_copy");
                Double averageOfRating = resultSet.getDouble("average_of_rating");
                String yearPublished = resultSet.getString("year_published");
                String language = resultSet.getString("language");
                String numberOfPages = resultSet.getString("number_of_pages");
                String description = resultSet.getString("description");
                String imageBook = resultSet.getString("book_image");

                if (imageBook == null) {
                    imageBook = "defaultBook.jpg";
                }

                Book book = new Book(id, title, author, isbn, category, publisher, quantityCopy, averageOfRating, yearPublished, language, numberOfPages, description, imageBook);

                topRateList.add(book);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        displayTopRatedBooks();
    }

    private void displayTopRatedBooks() {
        for (Book book : topRateList) {
            AnchorPane bookPane = new AnchorPane();
            bookPane.setPrefSize(183, 325);

            AnchorPane bookImagePane = new AnchorPane();
            bookImagePane.setPrefSize(183,226);
            bookImagePane.setLayoutX(0);

            AnchorPane infoPane = new AnchorPane();
            infoPane.setPrefSize(183, 99);
            infoPane.setLayoutY(226);

            ImageView bookImage = new ImageView();
            bookImage.setFitHeight(226);
            bookImage.setFitWidth(160);
            bookImage.setLayoutX(12);
            bookImage.setPickOnBounds(true);
            bookImage.setSmooth(true);
            String projectDir = System.getProperty("user.dir");
            String booksDir = projectDir + "/src/main/resources/images/book/";
            String path = booksDir + book.getImagePath();
            File file = new File(path);
            bookImage.setImage(new Image(file.toURI().toString()));

            Label titleLabel = new Label(book.getTitle());
            titleLabel.setLayoutX(11);
            titleLabel.setLayoutY(5);
            titleLabel.setPrefWidth(160);
            titleLabel.setWrapText(true);
            titleLabel.setFont(new javafx.scene.text.Font(14));
            titleLabel.setMaxHeight(48);

            Label authorLabel = new Label(book.getAuthor());
            authorLabel.setLayoutX(11);
            authorLabel.setLayoutY(47);
            authorLabel.setPrefWidth(160);
            authorLabel.setUnderline(true);

            AnchorPane buttonPane = new AnchorPane();
            buttonPane.setStyle("-fx-background-color: #FFF;");
            buttonPane.setPrefSize(162, 45);
            buttonPane.setLayoutY(225);
            buttonPane.setLayoutX(11);


            Button returnButton = new Button("QUICK BORROW");
            returnButton.getStyleClass().add("quick-borrow-button");
            returnButton.setLayoutX(6);
            returnButton.setLayoutY(5);
            buttonPane.getChildren().add(returnButton);

            bookImagePane.getChildren().addAll(bookImage,buttonPane);
            bookImagePane.setOnMouseEntered(e -> {
                TranslateTransition slideUp = new TranslateTransition(Duration.millis(250), buttonPane);
                slideUp.setFromY(0);
                slideUp.setToY(-38);
                slideUp.play();
            });

            bookImagePane.setOnMouseExited(e -> {
                TranslateTransition slideDown = new TranslateTransition(Duration.millis(250), buttonPane);
                slideDown.setFromY(-38);
                slideDown.setToY(0);
                slideDown.play();
            });

            bookPane.setOnMouseEntered(e -> bookPane.setStyle("-fx-cursor: hand; "));

            HBox starBox = new HBox(5);
            double rating = book.getAverageOfRating();
            for (int i = 1; i <= 5; i++) {
                ImageView star = new ImageView();
                if (i <= rating) {
                    star.setImage(new Image(getClass().getResource("/images/book/ratings/Star.png").toExternalForm()));
                }

                star.setFitHeight(15);
                star.setFitWidth(15);
                starBox.getChildren().add(star);
            }

            starBox.setLayoutX(11);
            starBox.setLayoutY(75);
            infoPane.setStyle("-fx-background-color: #FFFFFF;");
            infoPane.getChildren().addAll(titleLabel, authorLabel, starBox);
            bookPane.getChildren().addAll(bookImagePane,infoPane);
            topRateContainer.getChildren().add(bookPane);
        }
        topRateContainer.setSpacing(10);
    }

    private void scrollTopRate(int direction) {
        int newBookIndex = currentRatingBookIndex + (direction * BOOKS_PER_PAGE);
        if (newBookIndex >= 0 && newBookIndex <= TOTAL_BOOKS - BOOKS_PER_PAGE) {
            currentRatingBookIndex = newBookIndex;
            double targetHValue = (double) currentRatingBookIndex / (TOTAL_BOOKS - BOOKS_PER_PAGE);
            Timeline timeline = new Timeline();
            KeyFrame keyFrame = new KeyFrame(
                    Duration.seconds(0.4),
                    new javafx.animation.KeyValue(
                            topRateScroll.hvalueProperty(),
                            targetHValue,
                            Interpolator.EASE_BOTH
                    )
            );
            timeline.getKeyFrames().add(keyFrame);
            timeline.play();
        }
    }
}

