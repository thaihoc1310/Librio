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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import librio.cache.ImageCache;
import librio.database.DatabaseConnection;
import librio.models.Book;
import librio.session.Session;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static librio.util.DatabaseUtil.*;
import static librio.util.DesignUtil.*;

public class HomePageController implements Initializable {
    private static final int TOTAL_BOOKS = 18;

    private static final int BOOKS_PER_PAGE = 6;

    private final Map<String, Integer> currentIndexes = new HashMap<>();

    @FXML
    private ScrollPane mainScroll, mainBannerScroll, topRateScroll, mostBorrowedScroll,
            ourFictionScroll, newestBooksScroll, ourEconomicsBooksScroll, educationScroll;
    @FXML
    private ImageView avatarUser, clickAvatar, searchButton, mainBanner0, mainBanner1,
            mainBanner2, banner3, banner4, banner5, banner6, banner7, banner8, banner9,
            banner10, banner12, banner13, banner14;
    @FXML
    private ComboBox<String> filterBox;
    @FXML
    private Button leftMainBannerButton, leftToprateButton, leftMostborrowedButton,
            leftOurFictionButton, leftNewestBooksButton, leftOurEconomicsBooksButton, leftEducationButton;
    @FXML
    private Button rightMainBannerButton, rightToprateButton, rightMostborrowedButton,
            rightOurFictionButton, rightNewestBooksButton, rightOurEconomicsBooksButton, rightEducationButton;
    @FXML
    private TextField searchTextField;
    @FXML
    private HBox mainBannerContainer, topRateContainer, mostBorrowedContainer, ourFictionContainer,
            newestBooksContainer, ourEconomicsBooksContainer, educationContainer;
    @FXML
    private Label userNameUser, fictionLabel, historyLabel, scienceLabel, technologyLabel,
            computersLabel, economicsLabel, lawLabel, socialScienceLabel, educationLabel, artLabel;
    @FXML
    private AnchorPane menuPane, searchSuggestion, notificationPane;
    @FXML
    private Pane numberPane, backPane;
    @FXML
    private Circle moreIcon;
    @FXML
    private VBox searchSuggestionContainer;
    @FXML
    private Text numberText;
    @FXML
    private ProgressIndicator loadingIndicator;

    private boolean isAnchorPaneVisible = false;

    private boolean isNotificationPane = false;

    private Timeline autoScrollTimeline;

    private ExecutorService executor;

    /**
     * Initializes the HomePageController with necessary setups such as execution services, user interface elements
     * configuration, and category label interactions.
     *
     * @param location The location used to resolve relative paths for the root object, or {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        executor = Executors.newCachedThreadPool();
        setAvatarAndUserName();
        loadAllBooksAsync();
        filterBox.getItems().addAll("Title", "Author", "Category", "Language", "Publisher", "Year published", "ISBN");
        filterBox.getSelectionModel().selectFirst();
        initScrollNavigation();
        initCategoryLabelClick();
        setupSearchSuggestions();
        initNotification();
        initBannersClick();
    }

    /**
     * Sets the avatar image and user name for the currently logged-in user in the UI.
     * This method fetches the avatar image from the file system using the path
     * retrieved from the user's session information and displays it in two ImageView components.
     * If the user does not have a specific avatar set, a default avatar is used instead.
     * It also updates the Text component with the logged-in user's name.
     *
     * The avatar image is processed to fit a circular shape with a specified radius.
     *
     * The image and user name are taken from the currently logged-in user's session.
     * The avatar image path is constructed based on the project directory path and user information.
     *
     * Utilizes the ImageCache singleton for efficient image retrieval and caching.
     **/
    public void setAvatarAndUserName() {
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + Session.getInstance().getLoggedInUser().getAvatar();

        File file = new File(path);
        if(!file.exists()){
            path = avatarsDir + "Male User.png";
        }else{
            path = avatarsDir + Session.getInstance().getLoggedInUser().getAvatar();
        }
        Image image = ImageCache.getInstance().getImage(path,path);
        cropAndClipToCircle(image, avatarUser, 23);
        cropAndClipToCircle(image, clickAvatar, 23);
        userNameUser.setText(Session.getInstance().getLoggedInUser().getName());

    }

    /**
     * Scrolls the main banner in a specified direction, applying fade transitions between banners.
     * This method adjusts the horizontal scroll value of the banner, transitioning the visibility
     * of the current and next banner using fade effects.
     *
     * @param direction An integer indicating the scroll direction. A value of -1 indicates
     *                  scrolling to the left, while a value of 1 indicates scrolling to the right.
     */
    private void scrollMainBanner(int direction) {
        double currentHValue = mainBannerScroll.getHvalue();
        final double targetHValue = getTargetHValue(direction, currentHValue);

        Node currentBanner = getCurrentBanner(currentHValue);
        Node nextBanner = getCurrentBanner(targetHValue);
        if (currentBanner != null && nextBanner != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), currentBanner);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.7);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), nextBanner);
            fadeIn.setFromValue(0.7);
            fadeIn.setToValue(1.0);

            fadeOut.setOnFinished(event -> {
                mainBannerScroll.setHvalue(targetHValue);
                fadeIn.play();
            });

            fadeOut.play();
        }
    }

    /**
     * Calculates the target horizontal value for a scrolling operation based on the direction and current value.
     *
     * @param direction the scrolling direction; -1 for left and 1 for right.
     * @param currentHValue the current horizontal value of the scroll position.
     * @return the target horizontal value after applying the scrolling operation.
     */
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

    /**
     * Retrieves the current banner node based on the horizontal scroll value.
     *
     * @param hValue the current horizontal scroll value, which determines the index of the banner.
     * @return the Node representing the current banner if the index is valid; otherwise, returns null.
     */
    private Node getCurrentBanner(double hValue) {
        int index = (int) Math.round(hValue * (mainBannerContainer.getChildren().size() - 1));
        if (index >= 0 && index < mainBannerContainer.getChildren().size()) {
            return mainBannerContainer.getChildren().get(index);
        }
        return null;
    }

    /**
     * Loads different categories of books asynchronously and populates the corresponding UI containers
     * with the retrieved book data.
     *
     * This method executes multiple asynchronous tasks to fetch books in order of various criteria such as
     * top-rated, most borrowed, newest, and by specific categories like Fiction, Economics, and Education.
     * It sends queries to the database to fetch books for each category and updates the respective
     * display containers with the results.
     *
     * The following categories of books are loaded:
     * - Top rated books
     * - Most borrowed books
     * - Newest books
     * - Fiction books
     * - Economics books
     * - Education books
     *
     * Each category is limited to 18 books and sorted based on the specified criteria.
     * The results are displayed in predefined UI containers specific to each category.
     */
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

    /**
     * Asynchronously loads books from the database based on a specified category query and updates
     * the provided UI container with the retrieved book data. This method handles both successful
     * and failed data retrieval scenarios.
     *
     * @param query the database query string that specifies the category and criteria for selecting books.
     * @param container the HBox UI component where the fetched book data will be displayed.
     */
    private void loadBooksByCategoryAsync(String query, HBox container) {

        Task<List<Book>> loadTask = new Task<>() {
            @Override
            protected List<Book> call() {
                return loadBooksFromDatabase(query);
            }

            @Override
            protected void succeeded() {
                List<Book> books = getValue();
                if (books != null) {
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

    /**
     * Loads books from the database based on the specified SQL query.
     *
     * @param query the SQL query to execute for fetching the books from the database.
     *              This query should specify the conditions and ordering for retrieving the books.
     * @return a list of Book objects loaded from the database that match the query conditions.
     *         If no books are found or an error occurs, an empty list is returned.
     */
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

    /**
     * Handles the search action when the search button is clicked.
     * This method retrieves the keyword and selected filter from UI components,
     * conceals the search suggestion, and navigates to the SearchPage with specified search parameters.
     * It loads the SearchPage FXML file, initializes the controller with the search parameters,
     * and transitions the scene to display the search results.
     * If an IOException occurs during the process, it logs the stack trace.
     */
    @FXML
    private void handleSearch() {
        String keyword = searchTextField.getText().trim();
        String selectedFilter = filterBox.getValue();
        searchSuggestion.setVisible(false);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/SearchPage.fxml"));
        try {
            Parent searchPageRoot = loader.load();
            SearchPageController searchController = loader.getController();
            searchController.setSearchParameters(keyword, selectedFilter, null, "Top rated");
            Stage currentStage = (Stage) mainScroll.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            executor.shutdownNow();
            currentScene.setRoot(searchPageRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays a list of books in the specified UI container. This method clears
     * the existing content of the container and populates it with AnchorPane nodes
     * representing each book. The method runs the operation asynchronously to ensure
     * that the UI remains responsive while the book data is being prepared and displayed.
     *
     * @param books     A list of Book objects to be displayed.
     * @param container The HBox UI container where the book data will be displayed.
     */
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

    /**
     * Creates and configures an AnchorPane that visually represents a Book object.
     * This pane includes the book's image, title, author, and a button for quick borrowing,
     * alongside visual effects for mouse interactions and ratings that are loaded asynchronously.
     *
     * @param book the Book object containing details such as title, author, image path,
     *             and ISBN to be displayed within the pane.
     * @return the AnchorPane configured with the book information and interactive elements.
     */
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


        HBox starBox = getStarBox(book);
        starBox.setLayoutX(42);
        starBox.setLayoutY(80);

        infoPane.getChildren().add(starBox);
        return bookPane;
    }

    /**
     * Scrolls through a collection of books within a scrollable area.
     *
     * @param direction An integer indicating the direction of scrolling. A positive value moves forward,
     *                  while a negative value moves backward.
     * @param currentIndex The current starting index of the books being displayed in the scroll pane.
     * @param scrollPane The ScrollPane UI component in which the books are displayed.
     * @param updateIndex A Consumer functional interface used to update the current index of the displayed
     *                    books after scrolling.
     */
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

    /**
     * Opens the book detail scene in a new modal window, adjusting the current window's
     * brightness and centering the modal over the parent window. After the modal is closed,
     * resets the brightness of the parent window and updates all containers with the book information.
     *
     * @param book the Book object whose details are to be displayed in the new scene
     * @param confirmButton the Button that triggers the opening of the book detail scene
     */
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

    /**
     * Handles the click event on the avatar component.
     *
     * This method toggles the visibility of the menu pane and the back pane when the avatar is clicked.
     * If the anchor pane is not currently visible, it will be made visible, and if the notification pane is visible,
     * it will be hidden.
     *
     * If the anchor pane is already visible, clicking the avatar will hide the menu pane.
     *
     * The method updates the visibility state of the anchor pane and the notification pane accordingly.
     */
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

    /**
     * Toggles the visibility of the notification pane within the user interface.
     * If the notification pane is currently not visible, it becomes visible and any
     * visible menu pane is hidden. Otherwise, it hides the notification pane.
     * Maintains the state of auxiliary panes such as the back pane to ensure a consistent UI behavior.
     */
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

    /**
     * Handles the action of the cancel menu button.
     *
     * This method sets the `menuPane` visibility to `false` if `isAnchorPaneVisible` is `true`.
     * It also sets the `notificationPane` visibility to `false` if `isNotificationPane` is `true`.
     * Finally, it sets the `backPane` visibility to `false`.
     */
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

    /**
     * Handles the process of logging out the current user from the application.
     * Closes the current stage and opens the login stage.
     *
     * @throws IOException if there is an issue loading the login FXML file.
     */
    @FXML
    private void logOut() throws IOException {
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

    /**
     * Opens the Borrowed interface by loading the Borrowed.fxml file
     * and setting it as the root of the current scene. This method
     * is triggered using JavaFX's @FXML annotation, making it accessible
     * as a controller method for the corresponding FXML view.
     *
     * The method attempts to load the FXML resource associated with
     * managing borrowed items for a member. If an IOException occurs
     * during the loading process, the stack trace is printed to help
     * diagnose the issue.
     *
     * This method changes the current root node of the active scene
     * to the loaded FXML root node, effectively navigating the user
     * interface to the Borrowed.fxml view.
     */
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

    /**
     * Opens the borrow confirmation pane, allowing the user to confirm the borrowing of a book.
     * Applies visual effects to the current stage and waits for the confirmation pane to be closed.
     *
     * @param book The book for which the borrowing confirmation is requested.
     * @param confirmButton The button that triggers the borrowing confirmation pane.
     */
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

    /**
     * Opens the Edit Profile scene by loading the AccountSetting.fxml file.
     * The method temporarily adjusts the brightness of the current window,
     * displays the edit profile in a new modal window with a transparent background,
     * and ensures the main window remains disabled until the edit profile window is closed.
     * Once the edit profile window is hidden, the brightness effect applied to the
     * main window is removed and the user's avatar and name are updated.
     *
     * Handles potential IOExceptions that may occur during the loading of the FXML file.
     */
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
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);

            });
            stage.showAndWait();
            setAvatarAndUserName();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates all specified containers with the provided book information by invoking an update on each container.
     *
     * @param book the Book object containing information to update in each container
     */
    private void updateAllContainers(Book book) {
        List<HBox> containers = Arrays.asList(topRateContainer, mostBorrowedContainer, ourFictionContainer, ourEconomicsBooksContainer, newestBooksContainer, educationContainer);
        for (HBox container : containers) {
            updateButtonInContainer(container, book);
        }
    }

    /**
     * Sets the keyword and category for the search functionality.
     * This method assigns the given category value to the search text field,
     * selects the third item in the filter box, and triggers a search action.
     *
     * @param category the category to be set as the keyword for the search
     */
    private void setKeywordAndCategory(String category) {
        this.searchTextField.setText(category);
        filterBox.getSelectionModel().select(2);
        handleSearch();
    }

    /**
     * Displays a list of all the most borrowed books.
     * This method loads and displays book data based on the specified query
     * criteria focused on the most frequently borrowed books.
     * Invokes the loadByQuery method with a null value for the first argument
     * and a query string "Most borrowed" to filter the dataset appropriately.
     * Designed to be used in a JavaFX application where it is triggered via a
     * user interface action.
     */
    @FXML
    private void seeAllMostBorrowedBooks() {
        loadByQuery(null, "Most borrowed");
    }


    /**
     * Displays all books that are categorized as "Top rated".
     * This method is intended to be triggered by a UI component event.
     * It utilizes a query to fetch and load the books that belong to the
     * "Top rated" category. The category is specified in the query parameter
     * while the first parameter is null, indicating that no additional
     * filtering is applied beyond the provided category.
     */
    @FXML
    private void seeAllTopRatedBooks() {
        loadByQuery(null, "Top rated");
    }

    /**
     * This method is a JavaFX event handler that displays all books sorted from newest to oldest.
     * It triggers the loading of book data using a query that sorts the books in the desired order.
     * The current implementation does not filter by any specific category or criteria other than the sorting order.
     */
    @FXML
    private void seeAllNewestBooks() {
        loadByQuery(null, "Newest to Oldest");
    }

    /**
     * Displays a list of all books in the Economics category.
     * This method sets the search keyword to "Economics" and updates the view
     * to show the available books related to Economics. It is typically triggered
     * by the user from the UI to filter books by the Economics category.
     */
    @FXML
    private void seeAllEconomicsBooks() {
        setKeywordAndCategory("Economics");
    }

    /**
     * Handles the event to display all fiction books in the collection.
     * This method sets the appropriate keyword and category filters
     * to show only fiction books to the user interface.
     * It is triggered when a specific user action, such as clicking a button
     * labeled 'See All Fiction Books', occurs.
     */
    @FXML
    private void seeAllFictionBooks() {
        setKeywordAndCategory("Fiction");
    }

    /**
     * This method is triggered to filter and display all books
     * within the "Education" category. It sets a predefined
     * keyword and category to filter the book list accordingly.
     *
     * This method specifically calls the setKeywordAndCategory
     * function with the parameter "Education" to ensure that
     * only educational books are shown to the user.
     */
    @FXML
    private void seeAllEducationBooks() {
        setKeywordAndCategory("Education");
    }

    /**
     * Loads a new search page with specified query conditions and updates the current scene.
     *
     * @param additionalCondition a string specifying additional conditions to be applied to the search query. This can include filters or constraints to refine the search results
     * .
     * @param sortCondition a string specifying how the search results should be sorted. This can define the order or priority of search results display.
     */
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

    /**
     * Opens books associated with the specific banner image by constructing a query with book ISBNs.
     * This method determines the list of ISBNs based on the ID of the provided image and then
     * constructs a query to load books accordingly.
     *
     * @param image the ImageView object that represents the banner. The ID of this image is used
     *              to determine which set of books to open.
     */
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
                        , "9780751585568"
                        , "9781761189159"
                        , "9781250328144"
                        , "9780593852200"

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
                Collections.addAll(list, "9781646222384");
                break;

            default:
                break;
        }

        StringBuilder condition = new StringBuilder();
        condition.append("isbn IN (");
        for (String s : list) {
            condition.append(s).append(",");
        }
        condition.deleteCharAt(condition.length() - 1);
        condition.append(")");

        loadByQuery(condition.toString(), "Top rated");
    }

    /**
     * Initializes mouse click event handlers for a series of banners.
     * When a banner is clicked, the corresponding event triggers the
     * opening of books related to that banner by calling the
     * openBooksFromBanner method.
     *
     * This method sets the onMouseClicked event for each of the following banners:
     * mainBanner0, mainBanner1, mainBanner2, banner3, banner4, banner5, banner6,
     * banner7, banner8, banner9, banner10, banner12, banner13, and banner14.
     *
     * Each banner's click event will lead to the execution of the
     * openBooksFromBanner function, passing the clicked banner as an argument.
     */
    private void initBannersClick() {
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

    /**
     * Initializes mouse click event handlers for category labels.
     * When a category label is clicked, it sets the associated keyword and category.
     * This method registers event handlers for the following labels:
     * fictionLabel, historyLabel, scienceLabel, technologyLabel, computersLabel,
     * economicsLabel, lawLabel, socialScienceLabel, educationLabel, and artLabel.
     * Each event handler sets a specific keyword and category based on the label clicked.
     */
    private void initCategoryLabelClick() {
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

    /**
     * Initializes the scroll navigation for various book categories and the main banner.
     * The method sets up default starting indexes for each category and assigns mouse click
     * event handlers to both left and right navigation buttons. These handlers update the
     * current scroll position and initiate the scrolling animation in the specified direction.
     *
     * The categories include:
     * - Top Rate
     * - Most Borrowed
     * - Newest Books
     * - Our Fiction
     * - Education
     * - Our Economics Books
     *
     * The main banner also has left and right navigation to scroll through the featured items.
     *
     * The method assumes the existence of pre-defined scroll containers and buttons for each
     * category, updating the relevant index upon scrolling.
     */
    private void initScrollNavigation() {
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

    /**
     * Initializes the notification pane by setting its visibility and updating the display elements
     * based on the total number of books available in the session. If the total number of books
     * is greater than zero, the number pane is made visible and displays the total count of books.
     * If the count is less than 100, the exact number is shown; otherwise, "99+" is displayed.
     * Also updates the 'more' icon with a predefined image.
     */
    private void initNotification() {
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
        Image image = new Image(getClass().getResource("/icons/MemberIcon/more.png").toExternalForm());
        moreIcon.setFill(new ImagePattern(image));
    }

    /**
     * Updates the search suggestion container with a list of suggestions. Clears any existing
     * suggestions before adding the new ones. Each suggestion is represented as a clickable label
     * that updates the search text field and triggers a search when clicked. If the list of
     * suggestions is empty, the suggestion container is hidden.
     *
     * @param suggestions a list of suggestion strings to be displayed in the suggestion container.
     */
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

    /**
     * Configures the behavior of the search suggestions feature for a search text field.
     *
     * The method sets up a listener on the search text field to monitor changes to its text property.
     * When the text field's content changes:
     * - If the new text is empty or consists only of whitespace, the search suggestion container is cleared and hidden.
     * - If the text field loses focus, the search suggestions remain hidden.
     * - Otherwise, it fetches and displays search suggestions based on the trimmed input text.
     *
     * The visibility of the search suggestions is controlled based on the content and focus state of the search text field.
     */
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

    /**
     * Initiates an asynchronous task to fetch search suggestions based on the provided query.
     * The suggestions are retrieved from a database and upon successful completion,
     * the results are used to update the search suggestion container. If the task
     * fails, the exception is printed to the stack trace.
     *
     * @param query the search query for which suggestions are to be fetched.
     */
    private void fetchSearchSuggestions(String query) {
        Task<List<String>> suggestionTask = new Task<>() {
            @Override
            protected List<String> call() {
                return loadSuggestionsFromDatabase(query, filterBox);
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

