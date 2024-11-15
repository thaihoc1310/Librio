package librio.controllers.member;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;


import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import librio.database.DatabaseConnection;
import librio.models.Book;

public class SearchPageController implements Initializable {
    @FXML
    private ImageView avatar;

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
    private ImageView searchButton;

    @FXML
    private TextField searchTextField;

    @FXML
    private ComboBox<String> sortBox;

    @FXML
    private ProgressIndicator loadingIndicator;


    private List<Book> bookList = new ArrayList<>();
    private String keyword;
    private ExecutorService executor;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadingIndicator.setVisible(false);
        executor = Executors.newFixedThreadPool(2);
        setupAnimatedPane(ratePane, 255);
        setupAnimatedPane(categoryPane, 454);
        filterBox.getItems().addAll("Title", "Author", "Category", "Language", "Publisher", "Year published", "ISBN");
        filterBox.getSelectionModel().selectFirst();
        pagination.setPageFactory(this::createPage);
    }

    private void loadBooksAsync(int pageIndex) {
        Platform.runLater(() -> loadingIndicator.setVisible(true));
        Task<List<Book>> loadTask = new Task<>() {
            @Override
            protected List<Book> call() throws Exception {
                return loadBooksFromDatabase(pageIndex);
            }

            @Override
            protected void succeeded() {
                List<Book> fetchedBooks = getValue();
                if (fetchedBooks != null) {
                    displayBooks(fetchedBooks);
                }
                Platform.runLater(() -> loadingIndicator.setVisible(false));
            }
            @Override
            protected void failed() {
                Platform.runLater(() -> loadingIndicator.setVisible(false));
                getException().printStackTrace();
            }
        };

        executor.submit(loadTask);
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
        int offsetIndex = pageIndex * 20;
        try (Connection connection = DatabaseConnection.getConnection()) {
            String selectedFilter = filterBox.getValue();
            String query;
            PreparedStatement preparedStatement;

            if (keyword == null || keyword.isEmpty()) {
                query = "SELECT id, title, author, isbn, category, publisher, quantity_copy, average_of_rating, year_published, language, number_of_pages, description, book_image FROM books LIMIT ? OFFSET ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, 20);
                preparedStatement.setInt(2, offsetIndex);
            } else {
                query = "SELECT * FROM books WHERE " + getFilter(selectedFilter) + " LIKE ? LIMIT ? OFFSET ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, "%" + keyword + "%");
                preparedStatement.setInt(2, 20);
                preparedStatement.setInt(3, offsetIndex);
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

                fetchedBooks.add(book);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fetchedBooks;
    }

    private Node createPage(int pageIndex) {
        loadBooksAsync(pageIndex);
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

    @FXML
    private void handleSearch() {
        bookList.clear();
        keyword = searchTextField.getText().trim();
        loadBooksAsync(0);
        pagination.setCurrentPageIndex(0);
    }

    private void displayBooks(List<Book> bookList) {
        flowPane.getChildren().clear();
        for (Book book : bookList) {
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
            infoPane.setStyle("-fx-background-color: #FFFFFF;-fx-padding: 0;");
            infoPane.getChildren().addAll(titleLabel, authorLabel);
            Task<HBox> ratingTask = new Task<>() {
                @Override
                protected HBox call() {
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

                    return starBox;
                }

                @Override
                protected void succeeded() {
                        HBox starBox = getValue();
                        Platform.runLater(() -> {
                        starBox.setLayoutX(42);
                        starBox.setLayoutY(80);
                        infoPane.getChildren().add(starBox);
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                    });
                }
            };
            executor.execute(ratingTask);
            bookPane.getChildren().addAll(bookImagePane,infoPane);
            flowPane.getChildren().add(bookPane);
            flowPane.setHgap(40);
            flowPane.setVgap(20);
        }
    }


}
