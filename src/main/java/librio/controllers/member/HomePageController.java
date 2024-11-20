package librio.controllers.member;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import librio.auth.Session;
import librio.database.DatabaseConnection;
import librio.models.Book;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.function.Consumer;

import static librio.util.DatabaseUtil.checkIfUserBorrowedBook;
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
        filterBox.getItems().addAll("Title", "Author", "Category", "Language", "Publisher", "Year published", "ISBN");
        filterBox.getSelectionModel().selectFirst();
        startAutoScroll();
        loadTopRatedBooks();
        loadMostBorrowedBooks();
    }

    public void setAvatarAndUserName() {
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + Session.getInstance().getLoggedInUser().getAvatar();

        File file = new File(path);
        if (file.exists()) {
            Image image = new Image(file.toURI().toString());
            cropAndClipToCircle(image, avatarUser, 23);
            cropAndClipToCircle(image, clickAvatar, 23);
        } else {
            String defaultImage = avatarsDir + "Male User.png";
            File defaultImageFile = new File(defaultImage);
            Image image = new Image(defaultImageFile.toURI().toString());
            cropAndClipToCircle(image, avatarUser, 23);
            cropAndClipToCircle(image, clickAvatar, 23);
        }
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
            File file = new File(path);
            bookImage.setImage(new Image(file.toURI().toString()));

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

            Button returnButton = new Button();
            returnButton.getStyleClass().add("quick-borrow-button");
            returnButton.setLayoutX(6);
            returnButton.setLayoutY(5);
            setConfirmButton(returnButton, book);

            buttonPane.getChildren().add(returnButton);
            returnButton.setOnAction(e -> openBorrowConfirmationPane(book));
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
            bookPane.setOnMouseClicked(event -> openBookDetailScene(book));
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


    private void openBookDetailScene(Book book) {
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
                updateAllContainers(book);
            });

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateAllContainers(Book book) {
        List<HBox> containers = Arrays.asList(topRateContainer, mostBorrowedContainer);
        for (HBox container : containers) {
            updateButtonInContainer(container, book);
            container.layout();
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
        Stage currenStage = (Stage) searchTextField.getScene().getWindow();
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent loginRoot = loader.load();
        stage.setScene(new Scene(loginRoot));
        stage.show();
        Session.getInstance().logout();
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

    private void openBorrowConfirmationPane(Book book) {
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
                updateAllContainers(book);

            });
            stage.showAndWait();

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
}

