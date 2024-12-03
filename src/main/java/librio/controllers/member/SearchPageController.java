package librio.controllers.member;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static librio.util.DatabaseUtil.*;
import static librio.util.DesignUtil.*;


public class SearchPageController implements Initializable {
    private final List<Book> bookList = new ArrayList<>();

    @FXML
    public Label englishLabel, vietnameseLabel, frenchLabel, germanLabel, spanishLabel,
            italianLabel, russianLabel, dutchLabel, japaneseLabel, koreanLabel,
            danishLabel, thaiLabel, chineseLabel, fiveStarsLabel, fourStarsLabel,
            threeStarsLabel, twoStarsLabel, oneStarLabel, noRatingsLabel, userNameUser,
            fictionLabel, economicsLabel, computersLabel, historyLabel, scienceLabel,
            healthLabel, lawLabel, socialScienceLabel, technologyLabel, artLabel,
            educationLabel, sportsLabel, travelLabel, fictionLabel2, historyLabel2,
            scienceLabel2, technologyLabel2, computersLabel2, economicsLabel2,
            lawLabel2, socialScienceLabel2, educationLabel2, artLabel2, musicLabel, othersLabel;
    @FXML
    public VBox searchSuggestionContainer;
    @FXML
    public AnchorPane searchSuggestion, notificationPane, menuPane, numberPane;
    @FXML
    public ImageView clickAvatar, avatarUser, searchButton, banner15;
    @FXML
    public Pane backPane;
    @FXML
    public TitledPane categoryPane, ratePane;
    @FXML
    public ComboBox<String> filterBox, limitBox, sortBox;
    @FXML
    public FlowPane flowPane;
    @FXML
    public ScrollPane mainScroll;
    @FXML
    public Pagination pagination;
    @FXML
    public Circle moreIcon;
    @FXML
    public ProgressIndicator loadingIndicator;
    @FXML
    public TextField searchTextField;
    @FXML
    public Text numberText;

    private String keyword;

    private ExecutorService executor;

    private boolean isNoRatingFilter = false;

    private double currentRatingFilter = 0.0;

    private Label selectedRateLabel = null;

    private Label selectedLanguageLabel = null;

    private Label selectedCategoryLabel = null;

    private String currentLanguageFilter = null;

    private String currentCategoryFilter = null;

    private boolean isAnchorPaneVisible = false;

    private boolean isNotificationPane = false;

    private String additionalCondition = null;

    /**
     * Initializes the search page controller with necessary setups and configurations.
     * This method sets up user interface components, listeners, and initializes resources
     * needed for the controller to function. It configures the avatar, user name, thread
     * pool for asynchronous execution, and animations for specified panes. Additionally,
     * it prepares the pagination and drop-down boxes used in the interface, and attaches
     * necessary event listeners for user interactions and suggestions.
     *
     * @param location The location used to resolve relative paths for the root object, or
     *                 null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the
     *                  root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAvatarAndUserName();
        executor = Executors.newCachedThreadPool();
        setupAnimatedPane(ratePane, 255);
        setupAnimatedPane(categoryPane, 454);
        iniBox();
        pagination.setPageFactory(this::createPage);
        setupComboBoxListeners();
        setupPaneListeners();
        initCategoryLabelClick();
        setupSearchSuggestions();
        initNotification();
        banner15.setOnMouseClicked(event -> handleBannersClick());
    }

    /**
     * Initializes the filter, limit, and sort combo boxes with their respective items
     * and selects default values for each. The filter box is populated with options
     * such as "Title", "Author", "Category", etc., and the first item is selected by default.
     * The limit box is populated with numerical options such as "100", "50", and "20",
     * defaulting to selecting the third option. The sort box includes options for
     * sorting based on ratings, borrow frequency, and date, selecting the top-rated
     * sort option by default.
     */
    private void iniBox() {
        filterBox.getItems().addAll("Title", "Author", "Category", "Language", "Publisher", "Year published", "ISBN");
        filterBox.getSelectionModel().selectFirst();
        limitBox.getItems().addAll("100", "50", "20", "10");
        limitBox.getSelectionModel().select(2);
        sortBox.getItems().addAll("Top rated", "Most borrowed", "Newest to Oldest", "Oldest to Newest", "Title A-Z");
        sortBox.getSelectionModel().selectFirst();
    }

    /**
     * Initializes mouse click event handlers for category labels. Each label, when clicked,
     * sets a keyword and category value to be used in searches. This method is used to
     * associate specific category keywords with their respective labels, facilitating user
     * interactions with the search interface by enabling quick category selection.
     */
    private void initCategoryLabelClick() {
        fictionLabel2.setOnMouseClicked(event -> setKeywordAndCategory("Fiction"));
        historyLabel2.setOnMouseClicked(event -> setKeywordAndCategory("History"));
        scienceLabel2.setOnMouseClicked(event -> setKeywordAndCategory("Science"));
        technologyLabel2.setOnMouseClicked(event -> setKeywordAndCategory("Technology"));
        computersLabel2.setOnMouseClicked(event -> setKeywordAndCategory("Computers"));
        economicsLabel2.setOnMouseClicked(event -> setKeywordAndCategory("Economics"));
        computersLabel2.setOnMouseClicked(event -> setKeywordAndCategory("Computers"));
        economicsLabel2.setOnMouseClicked(event -> setKeywordAndCategory("Economics"));
        lawLabel2.setOnMouseClicked(event -> setKeywordAndCategory("Law"));
        socialScienceLabel2.setOnMouseClicked(event -> setKeywordAndCategory("Social Science"));
        educationLabel2.setOnMouseClicked(event -> setKeywordAndCategory("Education"));
        artLabel2.setOnMouseClicked(event -> setKeywordAndCategory("Art"));
    }

    /**
     * Initializes the notification pane based on the total number of books retrieved
     * from the session. If there are no books, the notification pane is set to be
     * invisible. If books are present, the number pane becomes visible. It displays
     * the total number of books if they are less than 100, otherwise it shows "99+".
     * This method is used to provide visual feedback about the user's total book count
     * in the interface.
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
    }

    /**
     * Loads a list of books asynchronously for the given page index and updates
     * the user interface with the retrieved books.
     *
     * This method initiates an asynchronous task to load books from a database
     * based on the specified page index. It updates the user interface by showing
     * a loading indicator while the task is in progress and clears any existing
     * book displays. Upon successful completion of the book-loading task, the
     * fetched books are displayed using the appropriate UI components. In case of
     * failure, any exception encountered during the task is printed to the
     * standard error stream for debugging purposes.
     *
     * @param pageIndex the index of the page to load books for, used to fetch a
     *                  specific subset of books from the database.
     */
    private void loadBooksAsync(int pageIndex) {
        loadingIndicator.setVisible(true);
        flowPane.getChildren().clear();
        Task<List<Book>> loadTask = new Task<>() {
            @Override
            protected List<Book> call() {
                return loadBooksFromDatabase(pageIndex);
            }

            @Override
            protected void succeeded() {
                List<Book> fetchedBooks = getValue();

                if (fetchedBooks != null) {
                    displayBooks(fetchedBooks);
                }
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
            }
        };
        executor.submit(loadTask);
    }

    /**
     * Sets the search parameters and initiates a search operation on the search interface.
     *
     * @param keyword              the search keyword to be used in the search text field.
     * @param filter               the filter criteria to apply to the search, selected from the filter box.
     * @param additionalCondition  an additional SQL-like condition to refine the search results.
     * @param sortCondition        the sorting criterion to order the search results, selected from the sort box.
     */
    public void setSearchParameters(String keyword, String filter, String additionalCondition, String sortCondition) {
        searchTextField.setText(keyword);
        filterBox.getSelectionModel().select(filter);
        sortBox.getSelectionModel().select(sortCondition);
        this.additionalCondition = additionalCondition;
        if (additionalCondition != null && additionalCondition.contains("COUNT(*)")) {
            this.sortBox.getSelectionModel().select(1);
        }
        handleSearch();
    }

    /**
     * Updates the user interface to display the current user's avatar and username.
     *
     * This method sets the user's avatar by retrieving the image associated with
     * the currently logged-in user from the file system, applying it to designated
     * image view elements (`avatarUser` and `clickAvatar`) after cropping it into
     * a circular shape. The user's username is also updated and displayed in the
     * `userNameUser` text component.
     *
     * The method depends on the existence of user-specific avatar image files located
     * in a directory relative to the application root. If the specific avatar file is
     * not found, a default image is used.
     *
     * Additionally, the method sets a static icon from resources to the `moreIcon`
     * variable for display purposes.
     *
     * It is assumed that a session management system is in place, allowing retrieval
     * of the current user's details through the `Session` singleton class.
     */
    public void setAvatarAndUserName() {
        Image iconImage = new Image(getClass().getResource("/icons/MemberIcon/more.png").toExternalForm());
        moreIcon.setFill(new ImagePattern(iconImage));
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + Session.getInstance().getLoggedInUser().getAvatar();

        File file = new File(path);
        if(!file.exists()){
            path = avatarsDir + "Male User.png";
        }
        Image image = ImageCache.getInstance().getImage(path,path);
        cropAndClipToCircle(image, avatarUser, 23);
        cropAndClipToCircle(image, clickAvatar, 23);

        userNameUser.setText(Session.getInstance().getLoggedInUser().getName());
    }

    /**
     * Configures a TitledPane to animate its expansion and collapse.
     * This method sets the initial state of the pane to collapsed and adds
     * a listener to manage the animation. The animation smoothly transitions
     * the pane's height between zero and a specified target height over a duration
     * of 0.3 seconds.
     *
     * @param pane the TitledPane to be configured with animation.
     * @param targetHeight the target height the pane should expand to when opened.
     */
    private void setupAnimatedPane(TitledPane pane, double targetHeight) {
        pane.setExpanded(false);
        pane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Timeline expandTimeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(pane.prefHeightProperty(), 0)),
                        new KeyFrame(Duration.seconds(0.3), new KeyValue(pane.prefHeightProperty(), targetHeight))
                );
                expandTimeline.play();
            } else {
                Timeline collapseTimeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(pane.prefHeightProperty(), targetHeight)),
                        new KeyFrame(Duration.seconds(0.3), new KeyValue(pane.prefHeightProperty(), 0))
                );
                collapseTimeline.play();
            }
        });
    }

    /**
     * Loads a list of books from the database based on the provided pagination and filtering parameters.
     *
     * @param pageIndex the index of the page to load books from; used to calculate the offset for pagination
     * @return a list of Book objects retrieved from the database, based on the specified pagination and filters
     */
    private List<Book> loadBooksFromDatabase(int pageIndex) {
        List<Book> fetchedBooks = new ArrayList<>();
        int offsetIndex = pageIndex * Integer.parseInt(limitBox.getValue());
        String limitClause = getLimitClause();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query;
            String selectedFilter = filterBox.getValue();
            String orderByClause = getOrderByClause(sortBox.getValue());
            query = buildQuery(selectedFilter, orderByClause, limitClause);

            PreparedStatement preparedStatement = connection.prepareStatement(query);

            int paramIndex = 1;


            if (currentLanguageFilter != null) {
                preparedStatement.setString(paramIndex++, currentLanguageFilter);
            }
            if (currentCategoryFilter != null) {
                preparedStatement.setString(paramIndex++, currentCategoryFilter);
            }
            if (keyword != null && !keyword.isEmpty()) {
                preparedStatement.setString(paramIndex++, "%" + keyword + "%");
            }
            if (!isNoRatingFilter) {
                preparedStatement.setDouble(paramIndex++, currentRatingFilter);
            }


            preparedStatement.setInt(paramIndex, offsetIndex);

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
     * Builds a SQL query string based on the provided filter, order, and limit clauses.
     * Constructs the query by appending various conditions and clauses using a StringBuilder.
     *
     * @param selectedFilter The filter criteria applied to the search query, determining the fields to search by.
     * @param orderByClause The SQL ORDER BY clause used to sort the query results.
     * @param limitClause The SQL LIMIT clause used to restrict the number of query results.
     * @return A complete SQL query string assembled from the filter, order by, and limit clauses.
     */
    private String buildQuery(String selectedFilter, String orderByClause, String limitClause) {
        StringBuilder queryBuilder = new StringBuilder();
        if (orderByClause.contains("GROUP BY")) {
            queryBuilder.append("SELECT books.*, COUNT(br.id) AS borrow_count ");
            queryBuilder.append("FROM books ");
            queryBuilder.append("LEFT JOIN borrows br ON books.isbn = br.book_isbn ");
        } else {
            queryBuilder.append("SELECT * FROM books ");
        }

        List<String> conditions = new ArrayList<>();
        if (currentLanguageFilter != null) {
            conditions.add("language = ?");
        }
        if (currentCategoryFilter != null) {
            conditions.add("category = ?");
        }
        if (keyword != null && !keyword.isEmpty()) {
            conditions.add(getFilter(selectedFilter) + " LIKE ?");
        }
        if (!isNoRatingFilter) {
            conditions.add("average_of_rating >= ?");
        } else {
            conditions.add("(average_of_rating IS NULL OR average_of_rating = 0)");
        }

        queryBuilder.append("WHERE ").append(String.join(" AND ", conditions));
        if (additionalCondition != null) {
            queryBuilder.append(" AND ").append(additionalCondition);
        }

        queryBuilder.append(orderByClause).append(" ");
        queryBuilder.append(limitClause).append(" OFFSET ?");

        return queryBuilder.toString();
    }

    /**
     * Creates a new page node for the specified page index.
     *
     * This method initializes the user interface for a specific page by asynchronously loading
     * book data and setting the main scroll's vertical value to zero. It returns a new BorderPane
     * which acts as the container for the page's content.
     *
     * @param pageIndex the index of the page to be created, used to load relevant book data.
     * @return a newly created BorderPane node representing the page.
     */
    private Node createPage(int pageIndex) {
        loadBooksAsync(pageIndex);
        mainScroll.setVvalue(0);
        return new BorderPane();
    }

    /**
     * Converts a specified filter string into a corresponding database column name.
     * This method maps user-friendly filter options to their respective
     * column names used in the database queries.
     *
     * @param filter the filter criterion input as a user-friendly string.
     *               Possible values include "Author", "Category", "Language",
     *               "Publisher", "Year published", "ISBN", or other string.
     * @return a string representing the database column name that corresponds
     *         to the given filter. If the filter does not match any of the
     *         predefined options, "title" is returned as the default column name.
     */
    private String getFilter(String filter) {
        return switch (filter) {
            case "Author" -> "author";
            case "Category" -> "category";
            case "Language" -> "language";
            case "Publisher" -> "publisher";
            case "Year published" -> "year_published";
            case "ISBN" -> "isbn";
            default -> "title";
        };
    }

    /**
     * Constructs and returns an SQL ORDER BY clause based on the sorting criterion provided.
     *
     * @param sortBy a string representing the desired sorting criterion.
     *               Accepted values are "Top rated", "Most borrowed", "Newest to Oldest",
     *               "Oldest to Newest", and "Title A-Z".
     *               Any other value will result in an empty string.
     * @return a string that contains the SQL clause for ordering results.
     *         The string is tailored to the specific sorting criterion.
     *         If the criterion does not match any recognized values, an empty string is returned.
     */
    private String getOrderByClause(String sortBy) {
        return switch (sortBy) {
            case "Top rated" -> " ORDER BY average_of_rating DESC";
            case "Most borrowed" -> " GROUP BY books.id ORDER BY COUNT(br.id) DESC";
            case "Newest to Oldest" -> " ORDER BY year_published DESC";
            case "Oldest to Newest" -> " ORDER BY year_published";
            case "Title A-Z" -> " ORDER BY title";
            default -> "";
        };
    }

    /**
     * Constructs a SQL `LIMIT` clause using the current value of `limitBox`.
     * The method retrieves the value from `limitBox`, parses it to an integer,
     * and then formats it into a `LIMIT` clause for SQL query purposes.
     *
     * @return A SQL `LIMIT` clause as a string, incorporating the parsed integer value
     *         from `limitBox`.
     */
    private String getLimitClause() {
        int limit = Integer.parseInt(limitBox.getValue());
        return " LIMIT " + limit;
    }

    /**
     * Handles the process of returning to the homepage by loading the homepage
     * FXML file asynchronously and updating the current scene's root node.
     * The method displays a loading indicator during the loading process
     * and hides it upon completion. If loading the FXML file fails, the
     * loading indicator is hidden and the exception stack trace is printed.
     * The method clears all children from the flowPane after successfully
     * setting the new root.
     *
     * Utilizes a background task to perform the loading operation off the
     * JavaFX Application thread, ensuring the UI remains responsive.
     *
     * The method uses a shared executor service to manage the background task.
     */
    @FXML
    private void returnHomepage() {
        loadingIndicator.setVisible(true);

        Task<Parent> loadHomePageTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                return new FXMLLoader(getClass().getResource("/fxml/member/HomePage.fxml")).load();
            }

            @Override
            protected void succeeded() {
                Parent homepageRoot = getValue();
                Stage currentStage = (Stage) mainScroll.getScene().getWindow();
                Scene currentScene = currentStage.getScene();
                currentScene.setRoot(homepageRoot);
                loadingIndicator.setVisible(false);
                flowPane.getChildren().clear();

            }

            @Override
            protected void failed() {
                Platform.runLater(() -> loadingIndicator.setVisible(false));
                getException().printStackTrace();
            }
        };

        executor.submit(loadHomePageTask);
        executor.shutdownNow();
    }

    /**
     * Reloads the data by initializing the asynchronous loading of books
     * and resets the pagination to the first page.
     *
     * This method is typically used to refresh the data displayed in the
     * user interface, ensuring that the most recent data is shown. It
     * calls the loadBooksAsync method from the beginning of the data
     * source and resets the pagination index to zero.
     */
    private void reloadData() {
        loadBooksAsync(0);
        pagination.setCurrentPageIndex(0);
    }

    /**
     * This method is triggered when the search button is clicked in the UI.
     * It resets the additional search condition to null and then calls
     * the handleSearch() method to perform the search operation.
     */
    @FXML
    private void handleSearchClick() {
        additionalCondition = null;
        handleSearch();
    }

    /**
     * Handles the search operation for the book list. This method clears the current
     * book list and retrieves the search keyword from the text field. It then reloads
     * the data based on the entered keyword and hides the search suggestion component.
     * The method expects any existing book data to be cleared and refreshed according
     * to the new search input.
     */
    private void handleSearch() {
        bookList.clear();
        keyword = searchTextField.getText().trim();
        reloadData();
        searchSuggestion.setVisible(false);
    }

    /**
     * Attaches listeners to combo boxes for sort and limit options.
     *
     * This method sets up listeners on the selection models of the sortBox
     * and limitBox combo boxes. When a new item is selected in either combo
     * box, the listeners trigger a reload of the data by calling the
     * reloadData() method. This ensures that any changes in the selected
     * options for sorting or limiting the data are immediately reflected
     * in the displayed data set.
     */
    private void setupComboBoxListeners() {
        sortBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            reloadData();
        });

        limitBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            reloadData();
        });
    }

    /**
     * Configures the search suggestions for the searchTextField. This method sets up a listener
     * on the text property of the searchTextField to monitor changes in the input value.
     *
     * When the text field's value changes:
     * - If the new value is empty or consists solely of whitespace, the search suggestion container
     *   is cleared, and search suggestions are made invisible.
     * - If there is a non-empty input and the text field is focused, search suggestions are made visible
     *   and fetched based on the trimmed input value.
     * - If the text field is not focused, search suggestions are hidden, and no fetching occurs.
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
     * Initiates an asynchronous task to fetch search suggestions based on the given query.
     * This method will execute a database call on a separate thread to retrieve suggestions
     * and update the user interface accordingly. In case of failure during the task execution,
     * the exception will be logged to the error stream.
     *
     * @param query the search string input for which suggestions are to be fetched
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

    /**
     * Updates the search suggestion container with a list of suggestions.
     * Clears the current suggestions and populates with new ones. Each suggestion
     * is represented as a clickable label which, when clicked, sets the text in the
     * search field and triggers a search action.
     *
     * @param suggestions a list of suggestion strings to be displayed in the search suggestion container
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
     * Initializes click listeners for various UI components representing rating,
     * language, and category labels. Each label is associated with a corresponding
     * value (e.g., a numerical rating or a specific string for a language or category).
     *
     * This method sets up:
     * - Rating label click listeners that associate labels with numerical ratings.
     * - Language label click listeners that map labels to specific language strings.
     * - Category label click listeners that map labels to specific category strings.
     *
     * Listeners are invoked when a label is clicked, allowing the application to
     * respond accordingly to user selections and interactions.
     */
    private void setupPaneListeners() {
        setRateLabelClickListener(fiveStarsLabel, 5.0);
        setRateLabelClickListener(fourStarsLabel, 4.0);
        setRateLabelClickListener(threeStarsLabel, 3.0);
        setRateLabelClickListener(twoStarsLabel, 2.0);
        setRateLabelClickListener(oneStarLabel, 1.0);
        setRateLabelClickListener(noRatingsLabel, 0.0);

        setLanguageLabelClickListener(englishLabel, "English");
        setLanguageLabelClickListener(vietnameseLabel, "Vietnamese");
        setLanguageLabelClickListener(frenchLabel, "French");
        setLanguageLabelClickListener(germanLabel, "German");
        setLanguageLabelClickListener(spanishLabel, "Spanish");
        setLanguageLabelClickListener(italianLabel, "Italian");
        setLanguageLabelClickListener(russianLabel, "Russian");
        setLanguageLabelClickListener(dutchLabel, "Dutch");
        setLanguageLabelClickListener(japaneseLabel, "Japanese");
        setLanguageLabelClickListener(koreanLabel, "Korean");
        setLanguageLabelClickListener(danishLabel, "Danish");
        setLanguageLabelClickListener(thaiLabel, "Thai");
        setLanguageLabelClickListener(chineseLabel, "Chinese");

        setCategoryLabelClickListener(fictionLabel, "Fiction");
        setCategoryLabelClickListener(economicsLabel, "Economics");
        setCategoryLabelClickListener(computersLabel, "Computers");
        setCategoryLabelClickListener(historyLabel, "History");
        setCategoryLabelClickListener(scienceLabel, "Science");
        setCategoryLabelClickListener(healthLabel, "Health");
        setCategoryLabelClickListener(lawLabel, "Law");
        setCategoryLabelClickListener(socialScienceLabel, "Social Science");
        setCategoryLabelClickListener(technologyLabel, "Technology");
        setCategoryLabelClickListener(artLabel, "Art");
        setCategoryLabelClickListener(educationLabel, "Education");
        setCategoryLabelClickListener(sportsLabel, "Sports");
        setCategoryLabelClickListener(travelLabel, "Travel");
        setCategoryLabelClickListener(musicLabel, "Music");
        setCategoryLabelClickListener(othersLabel, "Others");
    }

    /**
     * Sets a mouse click listener to the specified label, which toggles the selected language filter.
     * When the label is clicked, it will be styled to indicate selection, and clicking it again will
     * deselect it. If another label was previously selected, it will be reset. Updates the current
     * language filter accordingly and triggers a data reload.
     *
     * @param label the Label component that is assigned the click listener
     * @param language the language identifier associated with the label to be used as a filter
     */
    private void setLanguageLabelClickListener(Label label, String language) {
        label.setOnMouseClicked(event -> {
            if (selectedLanguageLabel == label) {
                selectedLanguageLabel = null;
                resetLabelStyle(label);
                currentLanguageFilter = null;
            } else {
                if (selectedLanguageLabel != null) {
                    resetLabelStyle(selectedLanguageLabel);
                }
                selectedLanguageLabel = label;
                applySelectedStyle(label);
                currentLanguageFilter = language;
            }
            additionalCondition = null;
            reloadData();
        });
    }

    /**
     * Sets a click listener for a given label that represents a rating value.
     * Upon clicking the label, the method updates the selected rate label
     * and adjusts the current rating filter accordingly. If the currently selected label
     * is clicked again, it resets the selection. It also handles a special case
     * for a 'no ratings' label which indicates no rating filter should be applied.
     * After handling the click event, the data is reloaded.
     *
     * @param label the Label to set the click listener on
     * @param rating the rating value associated with the label
     */
    private void setRateLabelClickListener(Label label, double rating) {
        label.setOnMouseClicked(event -> {
            if (selectedRateLabel == label) {
                selectedRateLabel = null;
                resetLabelStyle(label);
                currentRatingFilter = 0.0;
                isNoRatingFilter = false;
            } else {
                if (selectedRateLabel != null) {
                    resetLabelStyle(selectedRateLabel);
                }
                selectedRateLabel = label;
                applySelectedStyle(label);
                if (label == noRatingsLabel) {
                    isNoRatingFilter = true;
                    currentRatingFilter = 0.0;
                } else {
                    isNoRatingFilter = false;
                    currentRatingFilter = rating;
                }
            }
            additionalCondition = null;
            reloadData();
        });
    }

    /**
     * Sets a click listener on the provided label, enabling it to function as a category filter.
     * When the label is clicked, it is either marked as selected, triggering the application
     * of a category filter, or unselected, removing the filter. The appearance and state
     * of the label are updated accordingly.
     *
     * @param label the Label component to add a click listener to
     * @param category the category string associated with the label, used to filter data
     */
    private void setCategoryLabelClickListener(Label label, String category) {
        label.setOnMouseClicked(event -> {
            if (selectedCategoryLabel == label) {
                selectedCategoryLabel = null;
                resetLabelStyle(label);
                currentCategoryFilter = null;
            } else {
                if (selectedCategoryLabel != null) {
                    resetLabelStyle(selectedCategoryLabel);
                }
                selectedCategoryLabel = label;
                applySelectedStyle(label);
                currentCategoryFilter = category;
            }
            additionalCondition = null;
            reloadData();
        });
    }

    /**
     * Applies a predefined style to the specified label to indicate a selected state.
     *
     * @param label the Label instance to which the selected style will be applied
     */
    private void applySelectedStyle(Label label) {
        label.setStyle("-fx-text-fill: #4C2113;");
    }

    /**
     * Resets the style of the provided label by setting its text fill color.
     *
     * @param label the label whose style is to be reset
     */
    private void resetLabelStyle(Label label) {
        label.setStyle("-fx-text-fill: #B38B60;");
    }

    /**
     * Displays a list of books on the user interface. If the list is empty,
     * a message indicating no books are found is shown instead.
     *
     * @param bookList A list of Book objects to be displayed. If the list is empty,
     *                 a message indicating no books are available will be displayed.
     */
    private void displayBooks(List<Book> bookList) {
        flowPane.getChildren().clear();
        flowPane.setAlignment(Pos.TOP_LEFT);
        if (bookList.isEmpty()) {
            Label noBooksLabel = new Label("No books found");
            noBooksLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #B38B60; -fx-font-weight: bold;");
            flowPane.getChildren().add(noBooksLabel);
            flowPane.setHgap(0);
            flowPane.setVgap(0);
            flowPane.setAlignment(Pos.CENTER);
            loadingIndicator.setVisible(false);
            return;
        }
        Task<List<AnchorPane>> loadBooksTask = new Task<>() {
            @Override
            protected List<AnchorPane> call() {
                List<AnchorPane> panes = new ArrayList<>();
                for (Book book : bookList) {
                    panes.add(createBookPane(book));
                }
                return panes;
            }

            @Override
            protected void succeeded() {
                flowPane.getChildren().clear();
                List<AnchorPane> panes = getValue();
                flowPane.getChildren().addAll(panes);
                flowPane.setHgap(40);
                flowPane.setVgap(20);
                loadingIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
            }
        };

        executor.submit(loadBooksTask);
    }

    /**
     * Creates an AnchorPane representing a book with its image, title, author, and a quick borrow button.
     * The pane includes interactive transitions and responds to mouse events.
     *
     * @param book the Book object containing information such as image path, title, author, and ISBN
     * @return an AnchorPane configured to display the book's details and allowing interaction for borrowing
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

    /**
     * Handles the action when the avatar is clicked. This method toggles the visibility
     * of the menu pane. If the menu pane is not currently visible, it will be shown, 
     * and any visible notification pane will be hidden. Additionally, the back pane's
     * visibility will be set based on the menu pane's visibility. If the menu pane 
     * is already visible, it will be hidden.
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
     * Toggles the visibility of the notification pane and updates related pane states.
     *
     * This method is triggered as an event handler when attempting to open or close 
     * the notification pane in the user interface. It checks the current visibility 
     * state of the notification pane and toggles it. Additionally, it manages the 
     * visibility of the menu pane and the back pane accordingly:
     *
     * - If the notification pane is not currently visible, it will be set to visible.
     * - If the anchor pane is visible, it will be hidden to ensure only the notification
     *   pane is displayed.
     * - Conversely, if the notification pane is already visible, it will be hidden, 
     *   and the back pane visibility will also be updated.
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
     * Handles the action of the cancel menu button. This method is typically
     * invoked to hide specific UI components associated with the menu and
     * notifications within the application.
     *
     * It performs the following actions:
     * 1. If the anchor pane is currently visible, it hides the menu pane
     *    and sets the visibility state of the anchor pane to false.
     * 2. If the notification pane is currently in use, it hides the
     *    notification pane and sets the notification pane state to false.
     * 3. Finally, it hides the back pane.
     *
     * The method assumes the existence of specific boolean flags
     * and pane components that control the visibility of various UI elements.
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
     * Logs out the current user and transitions the application to the login screen.
     *
     * This method performs the following tasks:
     * 1. Retrieves the current stage from the scene of the search text field.
     * 2. Loads the login UI from the specified FXML file and sets it as the scene for a new stage.
     * 3. Displays the new login stage.
     * 4. Clears any user session data by calling the logout method from the Session singleton instance.
     * 5. Clears cached images by invoking the clearCache method from the ImageCache singleton instance.
     * 6. Closes the current stage to complete the logout process.
     *
     * @throws IOException if the FXMLLoader fails to load the specified FXML file.
     */
    @FXML
    void logOut() throws IOException {
        Stage currenStage = (Stage) searchTextField.getScene().getWindow();
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
     * Loads and displays the "Borrowed" member interface.
     *
     * The method attempts to load the FXML resource for the "Borrowed" interface and sets it
     * as the root scene of the current stage. If an IOException occurs during the loading
     * process, the exception stack trace is printed to the console.
     *
     * This method leverages JavaFX's FXMLLoader for loading the FXML file and assumes that
     * the FXML file located at "/fxml/member/Borrowed.fxml" is correctly structured and
     * accessible.
     *
     * It retrieves the current stage from the `avatarUser` component's scene and modifies the
     * root of the existing scene. The Stage and Scene are not replaced, only the root node
     * of the current scene changes to the "Borrowed" layout.
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
     * Opens the Edit Profile scene when invoked. This method loads the AccountSetting.fxml
     * layout file, applies a brightness adjustment effect to the current window while the
     * edit profile window is open, and launches a new stage with specified properties.
     * Upon closing the new stage, the brightness adjustment effect is removed.
     *
     * The new stage is initialized as a modal window, ensuring it blocks input to
     * other windows until closed, and is displayed with transparent styling.
     * The avatar and username are refreshed upon closing the edit profile scene.
     *
     * Catches and handles IOExceptions that may occur during the loading of the FXML file.
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
     * Opens the book detail scene in a new modal window, displaying 
     * information about the specified book. Applies a dimming effect 
     * to the current stage while the detail window is open.
     *
     * @param book the book whose detail information is to be displayed
     * @param confirmButton the button which may require updates based on 
     *                      actions performed in the book detail scene
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
                setConfirmButton(confirmButton, book);
            });

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens a confirmation pane when a user tries to borrow a book. This method loads the
     * FXML for the confirmation dialog, applies a dimming effect to the current window,
     * displays the confirmation dialog, and configures it to be centered over the current window.
     * Once the confirmation pane is closed, it restores the original brightness of the current window
     * and sets up the confirm button's functionality.
     *
     * @param book The book to be borrowed, which will be displayed in the confirmation dialog.
     * @param confirmButton The button that initiates the borrowing process, which gets configured
     *                      once the confirmation pane is closed.
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
                setConfirmButton(confirmButton, book);

            });
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the search keyword and selects the category in the filter box.
     *
     * This method assigns the provided category to the search field and selects
     * the predefined index of the category in the filter box. It also clears any
     * additional conditions and initiates a search.
     *
     * @param category the category to be used as the search keyword
     */
    private void setKeywordAndCategory(String category) {
        additionalCondition = null;
        this.searchTextField.setText(category);
        filterBox.getSelectionModel().select(2);
        handleSearch();
    }

    /**
     * Handles the click event for banner items, setting up search parameters
     * and adjusting the main scroll position.
     *
     * This method configures the search parameters to filter and display
     * top-rated items with a specific ISBN. It also resets the vertical scroll
     * position of the main scroll pane to the top.
     *
     * The parameters set within this method include:
     * - An empty search term to clear any previous text-based search.
     * - The title as the primary search category.
     * - A filter for the specific ISBN '9781529901795'.
     * - A sort order of "Top rated" to prioritize high-ranking items.
     *
     * After setting the search criteria, the main scroll view is adjusted to
     * ensure that the user starts at the beginning of the search results.
     */
    @FXML
    private void handleBannersClick() {
        setSearchParameters("", "Title", "isbn = '9781529901795'", "Top rated");
        mainScroll.setVvalue(0);
    }
}