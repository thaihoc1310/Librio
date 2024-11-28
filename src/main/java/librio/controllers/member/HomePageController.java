package librio.controllers.member;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
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
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private Button leftEducationButton;
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
    private Button rightEducationButton;
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
    private ScrollPane educationScroll;
    @FXML
    private HBox educationContainer;
    @FXML
    private Label userNameUser;
    @FXML
    private AnchorPane menuPane;
    @FXML
    private Pane backPane;
    @FXML
    private Circle moreIcon;
    @FXML
    private Label fictionLabel;
    @FXML
    private Label historyLabel;
    @FXML
    private Label scienceLabel;
    @FXML
    private Label technologyLabel;
    @FXML
    private Label computersLabel;
    @FXML
    private Label economicsLabel;
    @FXML
    private Label lawLabel;
    @FXML
    private Label socialScienceLabel;
    @FXML
    private Label educationLabel;
    @FXML
    private Label artLabel;
    @FXML
    private ImageView mainBanner0;
    @FXML
    private ImageView mainBanner1;
    @FXML
    private ImageView mainBanner2;
    @FXML
    private ImageView banner3;
    @FXML
    private ImageView banner4;
    @FXML
    private ImageView banner5;
    @FXML
    private ImageView banner6;
    @FXML
    private ImageView banner7;
    @FXML
    private ImageView banner8;
    @FXML
    private ImageView banner9;
    @FXML
    private ImageView banner10;
    @FXML
    private ImageView banner12;
    @FXML
    private ImageView banner13;
    @FXML
    private ImageView banner14;
    @FXML
    public VBox searchSuggestionContainer;
    @FXML
    public AnchorPane searchSuggestion;
    @FXML
    private AnchorPane notificationPane;
    @FXML
    private Text numberText;
    @FXML
    private AnchorPane numberPane;
    @FXML
    private ProgressIndicator loadingIndicator;
    private boolean isAnchorPaneVisible = false;
    private boolean isNotificationPane = false;
    private Timeline autoScrollTimeline;
    private ExecutorService executor;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image image = new Image(getClass().getResource("/icons/MemberIcon/more.png").toExternalForm());
        executor = Executors.newCachedThreadPool();
        moreIcon.setFill(new ImagePattern(image));
        setAvatarAndUserName();
        loadAllBooksAsync();

        filterBox.getItems().addAll("Title", "Author", "Category", "Language", "Publisher", "Year published", "ISBN");
        filterBox.getSelectionModel().selectFirst();
        startAutoScroll();
        initScrollNavigation();
        initCategoryLabelClick();
        setupSearchSuggestions();

        notificationPane.setVisible(false);
        int totalBooks = Session.getInstance().getTotalBooks();
        if (totalBooks != 0) {
            numberPane.setVisible(true);
            if (totalBooks < 100) {
                numberText.setText(String.valueOf(totalBooks));
            } else {
                numberText.setText("99+");
            }
        }

        initBannersClick();
    }

    public void setAvatarAndUserName() {
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + Session.getInstance().getLoggedInUser().getAvatar();

        Image image = ImageCache.getInstance().getImage(path,avatarsDir + "Male User.png");
        cropAndClipToCircle(image, avatarUser, 23);
        cropAndClipToCircle(image, clickAvatar, 23);
        userNameUser.setText(Session.getInstance().getLoggedInUser().getName());

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


    private void loadAllBooksAsync() {
        loadBooksByCategoryAsync("SELECT * FROM books ORDER BY average_of_rating DESC LIMIT 18", topRateContainer);
        loadBooksByCategoryAsync(
                "SELECT b.* FROM books b JOIN borrows br ON b.isbn = br.book_isbn GROUP BY b.id ORDER BY COUNT(*) DESC LIMIT 18",
                mostBorrowedContainer
        );
        loadBooksByCategoryAsync("SELECT * FROM books ORDER BY id DESC LIMIT 18", newestBooksContainer);
        loadBooksByCategoryAsync(
                "SELECT * FROM books WHERE category = 'Fiction' ORDER BY average_of_rating DESC LIMIT 18",
                ourFictionContainer
        );
        loadBooksByCategoryAsync(
                "SELECT * FROM books WHERE category = 'Economics' ORDER BY average_of_rating DESC LIMIT 18",
                ourEconomicsBooksContainer
        );
        loadBooksByCategoryAsync(
                "SELECT * FROM books WHERE category = 'Education' ORDER BY average_of_rating DESC LIMIT 18",
                educationContainer
        );
    }


    private void loadBooksByCategoryAsync(String query, HBox container) {

        Task<List<Book>> loadTask = new Task<>() {
            @Override
            protected List<Book> call() {
                return loadBooksFromDatabase(query);
            }

            @Override
            protected void succeeded() {
                List<Book> books = getValue();
               if(books!=null) {
                   container.getChildren().clear();
                   displayBooks(books, container);
               }
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    container.getChildren().clear();
                });
                getException().printStackTrace();
            }
        };
        executor.submit(loadTask);
    }

    private List<Book> loadBooksFromDatabase(String query) {
        List<Book> fetchedBooks = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String isbn = resultSet.getString("isbn");
                String category = resultSet.getString("category");
                String publisher = resultSet.getString("publisher");
                Integer availableCopy = resultSet.getInt("available_copy");
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

                Book book = new Book(id, title, author, isbn, category, publisher, quantityCopy, availableCopy, averageOfRating, yearPublished, language, numberOfPages, description, imageBook);
                fetchedBooks.add(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fetchedBooks;
    }




    @FXML
    private void handleSearch() {
        String keyword = searchTextField.getText().trim();
        String selectedFilter = filterBox.getValue();
        searchSuggestion.setVisible(false);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/SearchPage.fxml"));
        try {
            Parent searchPageRoot = loader.load();
            SearchPageController searchController = loader.getController();
            searchController.setSearchParameters(keyword, selectedFilter, null,"Top rated");
            Stage currentStage = (Stage) mainScroll.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(searchPageRoot);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayBooks(List<Book> books, HBox container) {
        container.getChildren().clear();
        Task<List<AnchorPane>> loadBooksTask = new Task<>() {
            @Override
            protected List<AnchorPane> call() {
                List<AnchorPane> panes = new ArrayList<>();
                for (Book book : books) {
                    panes.add(createBookPane(book));
                }
                return panes;
            }
            @Override
            protected void succeeded() {
                container.getChildren().clear();
                List<AnchorPane> panes = getValue();
                container.getChildren().addAll(panes);
            }
            @Override
            protected void failed() {
                getException().printStackTrace();
            }
        };
        executor.submit(loadBooksTask);
    }

    private AnchorPane createBookPane(Book book) {
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
        bookImage.setImage(ImageCache.getInstance().getImage(path, booksDir + "defaultBook.jpg"));

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
        infoPane.setStyle("-fx-background-color: #FFFFFF;-fx-padding: 0;");
        infoPane.getChildren().addAll(titleLabel, authorLabel);
        bookPane.getChildren().addAll(bookImagePane, infoPane);

        bookPane.setOnMouseClicked(e -> openBookDetailScene(book, quickBorrowButton));

        boolean isAlreadyBorrowed = checkIfUserBorrowedBook(Session.getInstance().getLoggedInUser(), book);

        if (!isAlreadyBorrowed && getAvailableCopyByIsbn(book.getIsbn()) > 0) {
            quickBorrowButton.setOnAction(e -> openBorrowConfirmationPane(book, quickBorrowButton));
        }

        Task<HBox> ratingTask = new Task<>() {
            @Override
            protected HBox call() {
                return getStarBox(book);
            }

            @Override
            protected void succeeded() {
                HBox starBox = getValue();
                starBox.setLayoutX(42);
                starBox.setLayoutY(80);
                infoPane.getChildren().add(starBox);
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                });
            }
        };
        executor.execute(ratingTask);
        return bookPane;
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
            menuPane.setVisible(true);
            isAnchorPaneVisible = true;

            if (isNotificationPane) {
                notificationPane.setVisible(false);
                isNotificationPane = false;
            }

            backPane.setVisible(true);
        } else {
            menuPane.setVisible(false);
            isAnchorPaneVisible = false;

        }
    }

    @FXML
    private void handleOpenNotification() {
        if (!isNotificationPane) {
            notificationPane.setVisible(true);
            isNotificationPane = true;
            if (isAnchorPaneVisible) {
                menuPane.setVisible(false);
                isAnchorPaneVisible = false;
            }
            backPane.setVisible(true);
        } else {
            notificationPane.setVisible(false);
            isNotificationPane = false;
            backPane.setVisible(false);
        }
    }

    @FXML
    private void cancelMenuButton() {
        if (isAnchorPaneVisible) {
            menuPane.setVisible(false);
            isAnchorPaneVisible = false;
        }

        if (isNotificationPane) {
            notificationPane.setVisible(false);
            isNotificationPane = false;
        }

        backPane.setVisible(false);
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
            Stage currentStage = (Stage) searchTextField.getScene().getWindow();
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(searchTextField.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL); stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);

            });
            stage.showAndWait();
            setAvatarAndUserName();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateAllContainers(Book book) {
        List<HBox> containers = Arrays.asList(topRateContainer, mostBorrowedContainer, ourFictionContainer, ourEconomicsBooksContainer, newestBooksContainer, educationContainer);
        for (HBox container : containers) {
            updateButtonInContainer(container, book);
        }
    }

    private void setKeywordAndCategory(String category){
        this.searchTextField.setText(category);
        filterBox.getSelectionModel().select(2);
        handleSearch();
    }

    private HBox getStarBox(Book book) {
        HBox starBox = new HBox(5);
        double rating = book.getAverageOfRating();
        int fullStars = (int) rating;
        double decimalPart = rating - fullStars;
        Image fullStarImage = new Image(getClass().getResource("/icons/MemberIcon/Star.png").toExternalForm());
        Image emptyStarImage = new Image(getClass().getResource("/icons/MemberIcon/Star_notfill.png").toExternalForm());
        for (int i = 1; i <= 5; i++) {
            StackPane starPane = new StackPane();

            ImageView emptyStar = new ImageView(emptyStarImage);
            emptyStar.setFitHeight(15);
            emptyStar.setFitWidth(15);

            starPane.getChildren().add(emptyStar);

            if (i <= fullStars) {
                ImageView fullStar = new ImageView(fullStarImage);
                fullStar.setFitHeight(15);
                fullStar.setFitWidth(15);
                starPane.getChildren().add(fullStar);
            } else if (i == fullStars + 1 && decimalPart > 0) {
                ImageView fullStar = new ImageView(fullStarImage);
                fullStar.setFitHeight(15);
                fullStar.setFitWidth(15);

                Rectangle clip = new Rectangle(15 * decimalPart, 15);
                fullStar.setClip(clip);
                starPane.getChildren().add(fullStar);
            }

            starBox.getChildren().add(starPane);
        }
        return starBox;
    }

    @FXML
    private void seeAllMostBorrowedBooks() {
        loadByQuery(null,"Most borrowed");
    }


    @FXML
    private void seeAllTopRatedBooks(){
        loadByQuery(null,"Top rated");
    }

    @FXML
    private void seeAllNewestBooks(){
        loadByQuery(null,"Newest to Oldest");
    }

    @FXML
    private void seeAllEconomicsBooks(){
        setKeywordAndCategory("Economics");
    }

    @FXML
    private void seeAllFictionBooks(){
        setKeywordAndCategory("Fiction");
    }

    @FXML
    private void seeAllEducationBooks(){
        setKeywordAndCategory("Education");
    }

    @FXML
    private void loadByQuery(String additionalCondition, String sortCondition) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/SearchPage.fxml"));
        try {
            Parent searchPageRoot = loader.load();
            SearchPageController searchController = loader.getController();

            searchController.setSearchParameters("", "Title", additionalCondition, sortCondition);

            Stage currentStage = (Stage) mainScroll.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(searchPageRoot);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openBooksFromBanner(ImageView image) {
        List<String> list = new ArrayList<>();

        String imageId = image.getId();

        switch (imageId) {
            case "mainBanner0":
                Collections.addAll(list, "1568582307", "9781250165343", "9781838718039");
                break;
            case "mainBanner1":
                Collections.addAll(list,
                        "9780593418932",
                        "9780735281820",
                        "9781668027912",
                        "9780593862735",
                        "9781035005703",
                        "9781648294273",
                        "9780593536148",
                        "9781802065831",
                        "9781789466058",
                        "9780593809884",
                        "9781250906168"
                );
                break;
            case "mainBanner2":
                Collections.addAll(list,
                        "9781250906168",
                        "9780593809884",
                        "9781250893444",
                        "9780735281820",
                        "0063410400",
                        "9780316581479"
                );
                break;

            case "banner3":
                Collections.addAll(list,
                        "9781668027912"
                );
                break;

            case "banner4":
                Collections.addAll(list
                        ,"9780751585568"
                        ,"9781761189159"
                        ,"9781250328144"
                        ,"9780593852200"

                );
                break;

            case "banner5":
                Collections.addAll(list,
                        "9781400337026",
                        "0008610746",
                        "1728296226",
                        "9781454954903",
                        "9780063251991",
                        "9781423147343"

                );
                break;

            case "banner6":
                Collections.addAll(list,
                        "1423145143\n"
                );
                break;

            case "banner7":
                Collections.addAll(list,
                        "9781646222384",
                        "9781250031211",
                        "9781464218637",
                        "9780063371378",
                        "9781982163310",
                        "9781250360687"
                );
                break;

            case "banner8":
                Collections.addAll(list,
                        "9780593862735",
                        "9781250759009",
                        "9780316557818",
                        "9780735281820",
                        "9780316581479",
                        "9781984863164",
                        "9780593536148",
                        "9781405963732",
                        "9780063251991",
                        "9780316569439",
                        "9788756799775"

                );
                break;

            case "banner9":
                Collections.addAll(list,
                        "9781594748639",
                        "9780593201282",
                        "9780593201251"
                );
                break;

            case "banner10":
                Collections.addAll(list,
                        "9781534427204",
                        "9781665974608",
                        "9798887075143",
                        "9780241583029",
                        "9780525647744",
                        "9780593707968"
                );
                break;

            case "banner12":
                Collections.addAll(list,
                        "9781464218637\n"
                );
                break;

            case "banner13":
                Collections.addAll(list,
                        "9781786583253",
                        "9780593815717",
                        "9781529052114",
                        "9780369742018",
                        "9781529029598",
                        "9780369747303"

                );
                break;

            case "banner14":
                Collections.addAll(list,"9781646222384");
                break;

            default:
                break;
        }

        StringBuilder condition = new StringBuilder();
        condition.append("isbn IN (");
        for(String s : list){
            condition.append(s).append(",");
        }
        condition.deleteCharAt(condition.length()-1);
        condition.append(")");

        loadByQuery(condition.toString(),"Top rated");
    }

    private void initBannersClick(){
        mainBanner0.setOnMouseClicked(event -> openBooksFromBanner(mainBanner0));
        mainBanner1.setOnMouseClicked(event -> openBooksFromBanner(mainBanner1));
        mainBanner2.setOnMouseClicked(event -> openBooksFromBanner(mainBanner2));
        banner3.setOnMouseClicked(event -> openBooksFromBanner(banner3));
        banner4.setOnMouseClicked(event -> openBooksFromBanner(banner4));
        banner5.setOnMouseClicked(event -> openBooksFromBanner(banner5));
        banner6.setOnMouseClicked(event -> openBooksFromBanner(banner6));
        banner7.setOnMouseClicked(event -> openBooksFromBanner(banner7));
        banner8.setOnMouseClicked(event -> openBooksFromBanner(banner8));
        banner9.setOnMouseClicked(event -> openBooksFromBanner(banner9));
        banner10.setOnMouseClicked(event -> openBooksFromBanner(banner10));
        banner12.setOnMouseClicked(event -> openBooksFromBanner(banner12));
        banner13.setOnMouseClicked(event -> openBooksFromBanner(banner13));
        banner14.setOnMouseClicked(event -> openBooksFromBanner(banner14));
    }

    private void initCategoryLabelClick(){
        fictionLabel.setOnMouseClicked(event -> setKeywordAndCategory("Fiction"));
        historyLabel.setOnMouseClicked(event -> setKeywordAndCategory("History"));
        scienceLabel.setOnMouseClicked(event -> setKeywordAndCategory("Science"));
        technologyLabel.setOnMouseClicked(event -> setKeywordAndCategory("Technology"));
        computersLabel.setOnMouseClicked(event -> setKeywordAndCategory("Computers"));
        economicsLabel.setOnMouseClicked(event -> setKeywordAndCategory("Economics"));
        computersLabel.setOnMouseClicked(event -> setKeywordAndCategory("Computers"));
        economicsLabel.setOnMouseClicked(event -> setKeywordAndCategory("Economics"));
        lawLabel.setOnMouseClicked(event -> setKeywordAndCategory("Law"));
        socialScienceLabel.setOnMouseClicked(event -> setKeywordAndCategory("Social Science"));
        educationLabel.setOnMouseClicked(event -> setKeywordAndCategory("Education"));
        artLabel.setOnMouseClicked(event -> setKeywordAndCategory("Art"));
    }

    private void initScrollNavigation(){
        currentIndexes.put("TopRate", 0);
        currentIndexes.put("MostBorrowed", 0);
        currentIndexes.put("NewestBooks", 0);
        currentIndexes.put("OurFiction", 0);
        currentIndexes.put("Education", 0);
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

        leftEducationButton.setOnMouseClicked(event ->
                scrollBooks(-1, currentIndexes.get("Education"), educationScroll, index -> currentIndexes.put("Education", index))
        );

        rightEducationButton.setOnMouseClicked(event ->
                scrollBooks(1, currentIndexes.get("Education"), educationScroll, index -> currentIndexes.put("Education", index))
        );

        leftOurEconomicsBooksButton.setOnMouseClicked(event ->
                scrollBooks(-1, currentIndexes.get("OurEconomicsBooks"), ourEconomicsBooksScroll, index -> currentIndexes.put("OurEconomicsBooks", index))
        );

        rightOurEconomicsBooksButton.setOnMouseClicked(event ->
                scrollBooks(1, currentIndexes.get("OurEconomicsBooks"), ourEconomicsBooksScroll, index -> currentIndexes.put("OurEconomicsBooks", index))
        );
    }

    private List<String> loadSuggestionsFromDatabase(String query) {
        List<String> suggestions = new ArrayList<>();
        String filter = filterBox.getValue();
        String sqlQuery = "SELECT DISTINCT " + filter + " FROM books WHERE " + filter + " LIKE ? LIMIT 10";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, "%" + query + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                suggestions.add(resultSet.getString(filter.toLowerCase()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suggestions;
    }

    private void updateSearchSuggestionContainer(List<String> suggestions) {
        searchSuggestionContainer.getChildren().clear();
        if (suggestions.isEmpty()) {
            searchSuggestion.setVisible(false);
            return;
        }

        for (String suggestion : suggestions) {
            Label suggestionLabel = new Label(suggestion);
            suggestionLabel.setOnMouseClicked(event -> {
                searchTextField.setText(suggestion);
                handleSearch();
            });
            suggestionLabel.getStyleClass().add("suggest-label");
            suggestionLabel.setPrefWidth(647);
            suggestionLabel.setPrefHeight(38);
            searchSuggestionContainer.getChildren().add(suggestionLabel);
        }
        searchSuggestion.setVisible(true);
    }
    private void setupSearchSuggestions() {
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                searchSuggestionContainer.getChildren().clear();
                searchSuggestion.setVisible(false);
            } else {
                if (!searchTextField.isFocused()) {
                    searchSuggestion.setVisible(false);
                    return;
                }
                searchSuggestion.setVisible(true);
                fetchSearchSuggestions(newValue.trim());
            }
        });
    }

    private void fetchSearchSuggestions(String query) {
        Task<List<String>> suggestionTask = new Task<>() {
            @Override
            protected List<String> call() {
                return loadSuggestionsFromDatabase(query);
            }

            @Override
            protected void succeeded() {
                List<String> suggestions = getValue();
                updateSearchSuggestionContainer(suggestions);
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
            }
        };
        executor.submit(suggestionTask);
    }
}

