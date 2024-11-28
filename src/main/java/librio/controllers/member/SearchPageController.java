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
import javafx.scene.shape.Rectangle;
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
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static librio.util.DatabaseUtil.checkIfUserBorrowedBook;
import static librio.util.DatabaseUtil.getAvailableCopyByIsbn;
import static librio.util.DesignUtil.cropAndClipToCircle;
import static librio.util.DesignUtil.setConfirmButton;

/**
 * Controller class for the search page of the application.
 * Manages the UI elements and interactions within the search page.
 */
public class SearchPageController implements Initializable {
    @FXML
    public Label englishLabel;
    @FXML
    public Label vietnameseLabel;
    @FXML
    public Label frenchLabel;
    @FXML
    public Label germanLabel;
    @FXML
    public Label spanishLabel;
    @FXML
    public Label italianLabel;
    @FXML
    public Label russianLabel;
    @FXML
    public Label dutchLabel;
    @FXML
    public Label japaneseLabel;
    @FXML
    public Label koreanLabel;
    @FXML
    public Label danishLabel;
    @FXML
    public Label thaiLabel;
    @FXML
    public Label chineseLabel;
    @FXML
    public VBox searchSuggestionContainer;
    @FXML
    public AnchorPane searchSuggestion;
    @FXML
    private ImageView clickAvatar;
    @FXML
    private ImageView avatarUser;
    @FXML
    private Pane backPane;
    @FXML
    private TitledPane categoryPane;
    @FXML
    private ComboBox<String> filterBox;
    @FXML
    private FlowPane flowPane;
    @FXML
    private ComboBox<String> limitBox;
    @FXML
    private ScrollPane mainScroll;
    @FXML
    private Pagination pagination;
    @FXML
    private TitledPane ratePane;
    @FXML
    private Label userNameUser;
    @FXML
    private ImageView searchButton;
    @FXML
    private TextField searchTextField;
    @FXML
    private ComboBox<String> sortBox;
    @FXML
    private Circle moreIcon;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Label fiveStarsLabel;
    @FXML
    private Label fourStarsLabel;
    @FXML
    private Label threeStarsLabel;
    @FXML
    private Label twoStarsLabel;
    @FXML
    private Label oneStarLabel;
    @FXML
    private Label noRatingsLabel;
    @FXML
    private Label fictionLabel;
    @FXML
    private Label economicsLabel;
    @FXML
    private Label computersLabel;
    @FXML
    private Label historyLabel;
    @FXML
    private Label scienceLabel;
    @FXML
    private Label healthLabel;
    @FXML
    private Label lawLabel;
    @FXML
    private Label socialScienceLabel;
    @FXML
    private Label technologyLabel;
    @FXML
    private Label artLabel;
    @FXML
    private Label educationLabel;
    @FXML
    private Label sportsLabel;
    @FXML
    private Label travelLabel;
    @FXML
    private Label fictionLabel2;
    @FXML
    private Label historyLabel2;
    @FXML
    private Label scienceLabel2;
    @FXML
    private Label technologyLabel2;
    @FXML
    private Label computersLabel2;
    @FXML
    private Label economicsLabel2;
    @FXML
    private Label lawLabel2;
    @FXML
    private Label socialScienceLabel2;
    @FXML
    private Label educationLabel2;
    @FXML
    private Label artLabel2;
    @FXML
    private Label musicLabel;
    @FXML
    private Label othersLabel;
    @FXML
    private AnchorPane notificationPane;
    @FXML
    private AnchorPane menuPane;
    @FXML
    private AnchorPane numberPane;
    @FXML
    private ImageView banner15;
    @FXML
    private Text numberText;
    private List<Book> bookList = new ArrayList<>();
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image image = new Image(getClass().getResource("/icons/MemberIcon/more.png").toExternalForm());
        moreIcon.setFill(new ImagePattern(image));
        setAvatarAndUserName();
        loadingIndicator.setVisible(false);
        executor = Executors.newCachedThreadPool();
        setupAnimatedPane(ratePane, 255);
        setupAnimatedPane(categoryPane, 454);
        filterBox.getItems().addAll("Title", "Author", "Category", "Language", "Publisher", "Year published", "ISBN");
        filterBox.getSelectionModel().selectFirst();
        limitBox.getItems().addAll("100", "50", "20", "10");
        limitBox.getSelectionModel().select(2);
        sortBox.getItems().addAll("Top rated", "Most borrowed", "Newest to Oldest", "Oldest to Newest", "Title A-Z");
        sortBox.getSelectionModel().selectFirst();
        pagination.setPageFactory(this::createPage);
        setupComboBoxListeners();
        setupPaneListeners();
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

        banner15.setOnMouseClicked(event -> handleBannersClick());
    }

    private void initCategoryLabelClick(){
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
     * Asynchronously loads a list of books for the specified page index and updates the UI components accordingly.
     *
     * @param pageIndex the index of the page to load books for
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
     * Sets the search parameters and initiates a search.
     *
     * @param keyword the keyword to search for
     * @param filter  the filter criteria to apply
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

    public void setAvatarAndUserName() {
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + Session.getInstance().getLoggedInUser().getAvatar();

        Image image = ImageCache.getInstance().getImage(path, avatarsDir + "img.png");
        cropAndClipToCircle(image, avatarUser, 23);
        cropAndClipToCircle(image, clickAvatar, 23);

        userNameUser.setText(Session.getInstance().getLoggedInUser().getName());
    }

    private void setupAnimatedPane(TitledPane pane, double targetHeight) {
        pane.setExpanded(false);
        pane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Tạo animation mở rộng chiều cao
                Timeline expandTimeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(pane.prefHeightProperty(), 0)),
                        new KeyFrame(Duration.seconds(0.3), new KeyValue(pane.prefHeightProperty(), targetHeight))
                );
                expandTimeline.play();
            } else {
                // Tạo animation thu nhỏ chiều cao
                Timeline collapseTimeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(pane.prefHeightProperty(), targetHeight)),
                        new KeyFrame(Duration.seconds(0.3), new KeyValue(pane.prefHeightProperty(), 0))
                );
                collapseTimeline.play();
            }
        });
    }

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


    private Node createPage(int pageIndex) {
        loadBooksAsync(pageIndex);
        mainScroll.setVvalue(0);
        return new BorderPane();
    }

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


    private String getLimitClause() {
        int limit = Integer.parseInt(limitBox.getValue());
        return " LIMIT " + limit;
    }

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
                Parent homepageRoot = getValue();Stage currentStage = (Stage) mainScroll.getScene().getWindow();
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
    }


    private void reloadData() {
        loadBooksAsync(0);
        pagination.setCurrentPageIndex(0);
    }

    @FXML
    private void handleSearchClick() {
        additionalCondition = null;
        handleSearch();
    }

    private void handleSearch() {
        bookList.clear();
        keyword = searchTextField.getText().trim();
        reloadData();
        searchSuggestion.setVisible(false);
    }

    private void setupComboBoxListeners() {
        sortBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            reloadData();
        });

        limitBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            reloadData();
        });
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

    private void applySelectedStyle(Label label) {
        label.setStyle("-fx-text-fill: #4C2113;");
    }

    private void resetLabelStyle(Label label) {
        label.setStyle("-fx-text-fill: #B38B60;");
    }

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
            // Hiển thị scene
            stage.showAndWait();
            setAvatarAndUserName();
        } catch (IOException e) {
            e.printStackTrace();
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
                setConfirmButton(confirmButton, book);
            });

            stage.showAndWait();
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
                setConfirmButton(confirmButton, book);

            });
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setKeywordAndCategory(String category) {
        additionalCondition = null;
        this.searchTextField.setText(category);
        filterBox.getSelectionModel().select(2);
        handleSearch();
    }

    private void handleBannersClick(){
        setSearchParameters("", "Title", "isbn = '9781529901795'", "Top rated");
    }
}