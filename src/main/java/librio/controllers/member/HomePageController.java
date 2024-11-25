package librio.controllers.member;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import librio.session.Session;
import librio.cache.ImageCache;
import librio.database.DatabaseConnection;
import librio.models.Book;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.function.Consumer;

import static librio.util.DatabaseUtil.checkIfUserBorrowedBook;
import static librio.util.DatabaseUtil.getAvailableCopyByIsbn;
import static librio.util.DesignUtil.*;

public class HomePageController implements Initializable {

    private static final int TOTAL_BOOKS = 18;
    private static final int BOOKS_PER_PAGE = 6;
    private final Map<String, Integer> currentIndexes = new HashMap<>();
    @FXML
    private ScrollPane mainScroll;
    @FXML
    private ImageView avatarUser;
    @FXML
    private ImageView clickAvatar;
    @FXML
    private ComboBox<String> filterBox;
    @FXML
    private Button leftMainBannerButton;
    @FXML
    private Button leftToprateButton;
    @FXML
    private Button leftMostborrowedButton;
    @FXML
    private Button leftOurFictionButton;
    @FXML
    private Button leftNewestBooksButton;
    @FXML
    private Button leftOurEconomicsBooksButton;
    @FXML
    private Button leftTrendingNowButton;
    @FXML
    private ScrollPane mainBannerScroll;
    @FXML
    private HBox mainBannerContainer;
    @FXML
    private Button rightMainBannerButton;
    @FXML
    private Button rightToprateButton;
    @FXML
    private Button rightMostborrowedButton;
    @FXML
    private Button rightOurFictionButton;
    @FXML
    private Button rightNewestBooksButton;
    @FXML
    private Button rightOurEconomicsBooksButton;
    @FXML
    private Button rightTrendingNowButton;

    @FXML
    private ImageView searchButton;
    @FXML
    private TextField searchTextField;
    @FXML
    private HBox topRateContainer;
    @FXML
    private ScrollPane topRateScroll;
    @FXML
    private HBox mostBorrowedContainer;
    @FXML
    private ScrollPane mostBorrowedScroll;
    @FXML
    private HBox ourFictionContainer;
    @FXML
    private ScrollPane ourFictionScroll;
    @FXML
    private HBox newestBooksContainer;
    @FXML
    private ScrollPane newestBooksScroll;
    @FXML
    private ScrollPane ourEconomicsBooksScroll;
    @FXML
    private HBox ourEconomicsBooksContainer;
    @FXML
    private ScrollPane trendingNowScroll;
    @FXML
    private HBox trendingNowContainer;
    @FXML
    private Label userNameUser;
    @FXML
    private Label userNameUser2;
    @FXML
    private AnchorPane menuPane;
    @FXML
    private Pane backPane;
    @FXML
    private Circle moreIcon;

    private boolean isAnchorPaneVisible = false;
    private Timeline autoScrollTimeline;
    private List<Book> topRateList = new ArrayList<>();
    private List<Book> mostBorrowedList = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image image = new Image(getClass().getResource("/icons/MemberIcon/more.png").toExternalForm());
        moreIcon.setFill(new ImagePattern(image));
        setAvatarAndUserName();
        currentIndexes.put("TopRate", 0);
        currentIndexes.put("MostBorrowed", 0);
        currentIndexes.put("NewestBooks", 0);
        currentIndexes.put("OurFiction", 0);
        currentIndexes.put("TrendingNow", 0);
        currentIndexes.put("OurEconomicsBooks", 0);
        leftMainBannerButton.setOnMouseClicked(event -> scrollMainBanner(-1));
        rightMainBannerButton.setOnMouseClicked(event -> scrollMainBanner(1));

        leftToprateButton.setOnMouseClicked(event ->
                scrollBooks(-1, currentIndexes.get("TopRate"), topRateScroll, index -> currentIndexes.put("TopRate", index))
        );
        rightToprateButton.setOnMouseClicked(event ->
                scrollBooks(1, currentIndexes.get("TopRate"), topRateScroll, index -> currentIndexes.put("TopRate", index))
        );

        leftMostborrowedButton.setOnMouseClicked(event ->
                scrollBooks(-1, currentIndexes.get("MostBorrowed"), mostBorrowedScroll, index -> currentIndexes.put("MostBorrowed", index))
        );
        rightMostborrowedButton.setOnMouseClicked(event ->
                scrollBooks(1, currentIndexes.get("MostBorrowed"), mostBorrowedScroll, index -> currentIndexes.put("MostBorrowed", index))
        );

        leftNewestBooksButton.setOnMouseClicked(event ->
                scrollBooks(-1, currentIndexes.get("NewestBooks"), newestBooksScroll, index -> currentIndexes.put("NewestBooks", index))
        );

        rightNewestBooksButton.setOnMouseClicked(event ->
                scrollBooks(1, currentIndexes.get("NewestBooks"), newestBooksScroll, index -> currentIndexes.put("NewestBooks", index))
        );

        leftOurFictionButton.setOnMouseClicked(event ->
                scrollBooks(-1, currentIndexes.get("OurFiction"), ourFictionScroll, index -> currentIndexes.put("OurFiction", index))
        );

        rightOurFictionButton.setOnMouseClicked(event ->
                scrollBooks(1, currentIndexes.get("OurFiction"), ourFictionScroll, index -> currentIndexes.put("OurFiction", index))
        );

        leftTrendingNowButton.setOnMouseClicked(event ->
                scrollBooks(-1, currentIndexes.get("TrendingNow"), trendingNowScroll, index -> currentIndexes.put("TrendingNow", index))
        );

        rightTrendingNowButton.setOnMouseClicked(event ->
                scrollBooks(1, currentIndexes.get("TrendingNow"), trendingNowScroll, index -> currentIndexes.put("TrendingNow", index))
        );



        leftOurEconomicsBooksButton.setOnMouseClicked(event ->
                scrollBooks(-1, currentIndexes.get("OurEconomicsBooks"), ourEconomicsBooksScroll, index -> currentIndexes.put("OurEconomicsBooks", index))
        );

        rightOurEconomicsBooksButton.setOnMouseClicked(event ->
                scrollBooks(1, currentIndexes.get("OurEconomicsBooks"), ourEconomicsBooksScroll, index -> currentIndexes.put("OurEconomicsBooks", index))
        );

        filterBox.getItems().addAll("Title", "Author", "Category", "Language", "Publisher", "Year published", "ISBN");
        filterBox.getSelectionModel().selectFirst();
        startAutoScroll();
        loadTopRatedBooks();
        loadMostBorrowedBooks();
        loadOurFictionBooks();
        loadNewestBooks();
        loadOurEconomicsBooks();
        loadTrendingNowBooks();
    }

    public void setAvatarAndUserName() {
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + Session.getInstance().getLoggedInUser().getAvatar();

        Image image = ImageCache.getInstance().getImage(path,avatarsDir + "Male User.png");
        cropAndClipToCircle(image, avatarUser, 23);
        cropAndClipToCircle(image, clickAvatar, 23);
        userNameUser.setText(Session.getInstance().getLoggedInUser().getName());
        userNameUser2.setText(Session.getInstance().getLoggedInUser().getName());
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
        stopAutoScroll();
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
        String query = "SELECT * FROM books ORDER BY average_of_rating DESC LIMIT ?";
        List<Book> topRatedBooks = loadBooks(query, TOTAL_BOOKS);
        displayBooks(topRatedBooks, topRateContainer);
    }



    private void loadMostBorrowedBooks() {
        String query = "SELECT b.* FROM books b JOIN borrows br ON b.isbn = br.book_isbn GROUP BY b.id ORDER BY COUNT(*) DESC LIMIT ?";
        List<Book> mostBorrowedBooks = loadBooks(query, TOTAL_BOOKS);
        displayBooks(mostBorrowedBooks, mostBorrowedContainer);
    }

    private void loadOurFictionBooks() {
        String query = "SELECT * FROM books WHERE category = 'Fiction' ORDER BY average_of_rating DESC LIMIT ?";
        List<Book> ourFictionBooks = loadBooks(query, TOTAL_BOOKS);
        displayBooks(ourFictionBooks, ourFictionContainer);
    }

    private void loadNewestBooks() {
        String query = "SELECT * FROM books ORDER BY id DESC LIMIT ?";
        List<Book> newestBooks = loadBooks(query, TOTAL_BOOKS);
        displayBooks(newestBooks, newestBooksContainer);
    }

    private void loadOurEconomicsBooks() {
        String query = "SELECT * FROM books WHERE category = 'Economics' ORDER BY average_of_rating DESC LIMIT ?";
        List<Book> ourEconomicsBooks = loadBooks(query, TOTAL_BOOKS);
        displayBooks(ourEconomicsBooks, ourEconomicsBooksContainer);
    }

    private void loadTrendingNowBooks() {
        String query = "SELECT b.*, COUNT(br.id) AS borrow_count " +
                "FROM books b " +
                "JOIN borrows br ON b.isbn = br.book_isbn " +
                "WHERE br.borrow_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
                "GROUP BY b.isbn " +
                "ORDER BY borrow_count DESC " +
                "LIMIT ?";

        List<Book> trendingNowBooks = loadBooks(query, TOTAL_BOOKS);
        displayBooks(trendingNowBooks, trendingNowContainer);
    }

    private List<Book> loadBooks(String query, Object... params) {
        List<Book> bookList = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String isbn = resultSet.getString("isbn");
                String category = resultSet.getString("category");
                String publisher = resultSet.getString("publisher");
                Integer quantityCopy = resultSet.getInt("quantity_copy");
                Integer availableCopy = resultSet.getInt("available_copy");
                Double averageOfRating = resultSet.getDouble("average_of_rating");
                String yearPublished = resultSet.getString("year_published");
                String language = resultSet.getString("language");
                String numberOfPages = resultSet.getString("number_of_pages");
                String description = resultSet.getString("description");
                String imageBook = resultSet.getString("book_image");

                if (imageBook == null) {
                    imageBook = "defaultBook.jpg";
                }

                Book book = new Book(id, title, author, isbn, category, publisher, quantityCopy, availableCopy, averageOfRating, yearPublished, language, numberOfPages, description, imageBook);
                bookList.add(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookList;
    }


    @FXML
    private void handleSearch() {
        String keyword = searchTextField.getText().trim();
        String selectedFilter = filterBox.getValue();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/SearchPage.fxml"));
        try {
            Parent searchPageRoot = loader.load();
            SearchPageController searchController = loader.getController();
            searchController.setSearchParameters(keyword, selectedFilter);
            Stage currentStage = (Stage) mainScroll.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(searchPageRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayBooks(List<Book> books, HBox container) {
        container.getChildren().clear();
        for (Book book : books) {
            AnchorPane bookPane = new AnchorPane();
            bookPane.setPrefSize(183, 325);

            AnchorPane bookImagePane = new AnchorPane();
            bookImagePane.setPrefSize(183, 226);
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
            bookImage.setPreserveRatio(false);
            String projectDir = System.getProperty("user.dir");
            String booksDir = projectDir + "/src/main/resources/images/book/";
            String path = booksDir + book.getImagePath();
            Image image = ImageCache.getInstance().getImage(path,booksDir + "defaultBook.jpg");
            bookImage.setImage(image);

            Label titleLabel = new Label(book.getTitle());
            titleLabel.setLayoutX(11);
            titleLabel.setLayoutY(8);
            titleLabel.setPrefWidth(160);
            titleLabel.getStyleClass().add("title-label");

            Label authorLabel = new Label(book.getAuthor());
            authorLabel.setLayoutX(11);
            authorLabel.setLayoutY(55);
            authorLabel.setPrefWidth(160);
            authorLabel.getStyleClass().add("author-label");

            AnchorPane buttonPane = new AnchorPane();
            buttonPane.setStyle("-fx-background-color: #FFF;");
            buttonPane.setPrefSize(162, 45);
            buttonPane.setLayoutY(225);
            buttonPane.setLayoutX(11);

            Button quickBorrowButton = new Button();
            quickBorrowButton.getStyleClass().add("quick-borrow-button");
            quickBorrowButton.setLayoutX(6);
            quickBorrowButton.setLayoutY(5);

            setConfirmButton(quickBorrowButton, book);
            buttonPane.getChildren().add(quickBorrowButton);

            bookImagePane.getChildren().addAll(bookImage, buttonPane);
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

            starBox.setLayoutX(42);
            starBox.setLayoutY(80);
            infoPane.setStyle("-fx-background-color: #FFFFFF;-fx-padding: 0;");
            infoPane.getChildren().addAll(titleLabel, authorLabel, starBox);
            bookPane.getChildren().addAll(bookImagePane, infoPane);

            bookPane.setOnMouseClicked(e -> openBookDetailScene(book,quickBorrowButton));

            boolean isAlreadyBorrowed = checkIfUserBorrowedBook(Session.getInstance().getLoggedInUser(), book);
            if (!isAlreadyBorrowed && getAvailableCopyByIsbn(book.getIsbn()) > 0) {
                quickBorrowButton.setOnAction(e -> {
                    openBorrowConfirmationPane(book, quickBorrowButton);
                });
            }
            container.getChildren().add(bookPane);
        }
        container.setSpacing(10);
    }

    private void scrollBooks(int direction, int currentIndex, ScrollPane scrollPane, Consumer<Integer> updateIndex) {
        int newBookIndex = currentIndex + (direction * BOOKS_PER_PAGE);
        if (newBookIndex >= 0 && newBookIndex <= TOTAL_BOOKS - BOOKS_PER_PAGE) {
            updateIndex.accept(newBookIndex);
            double targetHValue = (double) newBookIndex / (TOTAL_BOOKS - BOOKS_PER_PAGE);
            Timeline timeline = new Timeline();
            KeyFrame keyFrame = new KeyFrame(
                    Duration.seconds(0.4),
                    new javafx.animation.KeyValue(
                            scrollPane.hvalueProperty(),
                            targetHValue,
                            Interpolator.EASE_BOTH
                    )
            );
            timeline.getKeyFrames().add(keyFrame);
            timeline.play();

        }
    }


    private void openBookDetailScene(Book book, Button confirmButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/BookDetail.fxml"));
            Parent rootContent = loader.load();
            Stage currentStage = (Stage) searchTextField.getScene().getWindow();
            BookDetailController bookDetailController = loader.getController();
            bookDetailController.setBook(book);

            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(rootContent);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(currentStage);
            stage.setOnShown(event -> {
                stage.setX(currentStage.getX() + (currentStage.getWidth() - stage.getWidth()) / 2);
                stage.setY(currentStage.getY() + (currentStage.getHeight() - stage.getHeight()) / 2);
            });

            stage.initModality(Modality.WINDOW_MODAL);

            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);

            });

            stage.showAndWait();

            updateAllContainers(book);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleAvatarClick() {
        if (!isAnchorPaneVisible) {
            menuPane.toFront();
            isAnchorPaneVisible = true;
            backPane.setVisible(true);

        } else {
            menuPane.toBack();
            isAnchorPaneVisible = false;
            backPane.setVisible(false);
        }
    }

    @FXML
    private void cancelMenuButton() {
        if (isAnchorPaneVisible) {
            menuPane.toBack();
            isAnchorPaneVisible = false;
            backPane.setVisible(false);
        }
    }

    @FXML
    void logOut() throws IOException {
        Stage currenStage = (Stage) avatarUser.getScene().getWindow();
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent loginRoot = loader.load();
        stage.setScene(new Scene(loginRoot));
        stage.show();
        Session.getInstance().logout();
        ImageCache.getInstance().clearCache();
        currenStage.close();
    }

    @FXML
    private void openBorrowed() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/Borrowed.fxml"));
            Parent manageUserRoot = loader.load();
            Stage currentStage = (Stage) avatarUser.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageUserRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openBorrowConfirmationPane(Book book, Button confirmButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/ConfirmBorrow.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) searchTextField.getScene().getWindow();
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

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);


            });

            stage.showAndWait();
            updateAllContainers(book);



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openEditProfileScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/AccountSetting.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Edit Profile");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initOwner(searchTextField.getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            setAvatarAndUserName();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateAllContainers(Book book) {
        List<HBox> containers = Arrays.asList(topRateContainer, mostBorrowedContainer);
        for (HBox container : containers) {
            updateButtonInContainer(container, book);
        }
    }
}

