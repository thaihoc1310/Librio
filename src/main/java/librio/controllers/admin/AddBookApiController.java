package librio.controllers.admin;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import librio.controllers.LogoutController;
import librio.controllers.admin.CreateBookController;
import librio.models.Book;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Character.isDigit;

public class AddBookApiController implements Initializable {
    @FXML
    public Button addBookButton;
    @FXML
    public ImageView searchButton;
    @FXML
    public ComboBox<String> filterBox;
    @FXML
    public TextField searchTextField;
    @FXML
    private ScrollPane bookListScrollPane;

    private List<Book> bookList = new ArrayList<>();
    private ExecutorService executor;
    private int startIndex = 0;
    private int totalItems = 0;
    private VBox contentPane = new VBox(10);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filterBox.getItems().addAll("Title", "Author", "Category", "Language", "Publisher", "Year published", "ISBN");
        filterBox.getSelectionModel().selectFirst();
        executor = Executors.newFixedThreadPool(2);
        bookList.clear();
        loadBooksAsync("");
    }

    private void loadBooksAsync(String searchKeyWord) {
        Task<List<Book>> loadTask = new Task<>() {
            @Override
            protected List<Book> call() throws Exception {
                return loadBooksFromGoogleAPI(searchKeyWord);
            }

            @Override
            protected void succeeded() {
                List<Book> fetchedBooks = getValue();
                if (fetchedBooks != null) {
                    bookList.addAll(fetchedBooks);
                    displayBooks(fetchedBooks);
                }
            }
        };

        executor.submit(loadTask);
    }

    private List<Book> loadBooksFromGoogleAPI(String searchKeyWord) {
        List<Book> fetchedBooks = new ArrayList<>();
        try {
            String apiKey = "AIzaSyBRX3PmHB6TlSwDsU5KmcbexZxISjyd9hI";
            String filter = filterBox.getValue();
            String encodeKeyword = java.net.URLEncoder.encode(searchKeyWord, StandardCharsets.UTF_8);
            String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + getFilter(filter) + encodeKeyword
                    + "&startIndex=" + startIndex + "&maxResults=15&key=" + apiKey;

            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            JSONObject responseJson = new JSONObject(content.toString());
            totalItems = responseJson.optInt("totalItems", 0);

            JSONArray items = responseJson.optJSONArray("items");

            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject volumeInfo = items.getJSONObject(i).getJSONObject("volumeInfo");
                    String title = volumeInfo.optString("title", "Unknown Title");
                    String author = volumeInfo.optJSONArray("authors") != null ? volumeInfo.getJSONArray("authors").getString(0) : "Unknown Author";
                    String isbn = volumeInfo.optJSONArray("industryIdentifiers") != null ? volumeInfo.getJSONArray("industryIdentifiers").getJSONObject(0).getString("identifier") : "Unknown ISBN";
                    String category = volumeInfo.optJSONArray("categories") != null ? volumeInfo.getJSONArray("categories").getString(0) : "Unknown Category";
                    String publisher = volumeInfo.optString("publisher", "Unknown Publisher");
                    String yearPublished = volumeInfo.optString("publishedDate", "Unknown Year");
                    String language = volumeInfo.optString("language", "Unknown Language");
                    String description = volumeInfo.optString("description", "No Description");
                    String imageBook = volumeInfo.has("imageLinks") ? volumeInfo.getJSONObject("imageLinks").getString("smallThumbnail") : "defaultBook.jpg";
                    Integer numberOfPages = volumeInfo.optInt("pageCount", 0);
                    if (isDigit(isbn.charAt(0))) isbn = "ISBN : " + isbn;

                    Book book = new Book(0, title, author, isbn, category, publisher, 0, 0.0, yearPublished, language, String.valueOf(numberOfPages), description, imageBook);
                    fetchedBooks.add(book);
                }
                startIndex += 15;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fetchedBooks;
    }

    @FXML
    private void handleSearch() {
        // Cancel existing executor
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        // Re-initialize executor
        executor = Executors.newFixedThreadPool(2);
        String keyword = searchTextField.getText().trim();
        startIndex = 0;
        totalItems = 0;
        bookList.clear();
        loadBooksAsync(keyword);
        bookListScrollPane.setVvalue(0);
    }

    private String getFilter(String filter) {
        switch (filter) {
            case "Title":
                return "intitle:";
            case "Author":
                return "inauthor:";
            case "Category":
                return "subject:";
            case "Language":
                return "langRestrict:";
            case "Publisher":
                return "inpublisher:";
            case "Year published":
                return "publishedDate:";
            case "ISBN":
                return "isbn:";
            default:
                return "";
        }
    }


    private void displayBooks(List<Book> booksToDisplay) {
        Platform.runLater(() -> {
            if (startIndex == 0) {
                contentPane.getChildren().clear();
                contentPane.setPrefWidth(bookListScrollPane.getPrefWidth());
            } else {
                // Remove existing 'More' button if it exists
                if (!contentPane.getChildren().isEmpty() && contentPane.getChildren().get(contentPane.getChildren().size() - 1) instanceof Button) {
                    contentPane.getChildren().remove(contentPane.getChildren().size() - 1);
                }
            }

            for (Book book : booksToDisplay) {
                AnchorPane bookPane = createBookPane(book);
                contentPane.getChildren().add(bookPane);
            }

            if ((startIndex + 15) < totalItems) {
                Button moreButton = new Button("More");
                moreButton.setPrefHeight(32.0);
                moreButton.setPrefWidth(667.0);
                moreButton.setStyle("-fx-background-color: #72311c; -fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-background-radius: 5px; -fx-font-size: 15px;");
                moreButton.setOnMouseEntered(event -> moreButton.setStyle("-fx-background-color: #4c2113; -fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-background-radius: 5px; -fx-font-size: 15px; -fx-cursor: hand;"));
                moreButton.setOnMouseExited(event -> moreButton.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-background-radius: 5px; -fx-font-size: 15px;-fx-background-color: #72311c;"));
                moreButton.setOnAction(event -> loadBooksAsync(searchTextField.getText().trim()));
                contentPane.getChildren().add(moreButton);
            }

            bookListScrollPane.setContent(contentPane);
            bookListScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            bookListScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            bookListScrollPane.setPannable(true);
        });
    }

    /**
     * Create an AnchorPane for each book
     */
    private AnchorPane createBookPane(Book book) {
        AnchorPane bookPane = new AnchorPane();
        bookPane.setPrefHeight(155.0);
        bookPane.setPrefWidth(657.0);

        ImageView bookImage = new ImageView();
        bookImage.setFitHeight(142.0);
        bookImage.setFitWidth(119.0);
        bookImage.setLayoutX(24.0);
        bookImage.setLayoutY(7.0);
        if (!book.getImagePath().equals("defaultBook.jpg")) {
            Task<Image> imageLoadTask = new Task<>() {
                @Override
                protected Image call() {
                    return new Image(book.getImagePath(), true);
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> bookImage.setImage(getValue()));
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> bookImage.setImage(new Image(getClass().getResource("/images/book/defaultBook.jpg").toExternalForm())));
                }
            };
            executor.submit(imageLoadTask);
        } else {
            bookImage.setImage(new Image(getClass().getResource("/images/book/defaultBook.jpg").toExternalForm()));
        }
        bookPane.getChildren().add(bookImage);

        Label titleLabel = new Label(book.getTitle());
        titleLabel.setLayoutX(166.0);
        titleLabel.setLayoutY(14.0);
        titleLabel.setPrefHeight(27.0);
        titleLabel.setPrefWidth(250.0);
        bookPane.getChildren().add(titleLabel);

        Label authorLabel = new Label("Author: " + book.getAuthor());
        authorLabel.setLayoutX(166.0);
        authorLabel.setLayoutY(49.0);
        authorLabel.setPrefHeight(25.0);
        authorLabel.setPrefWidth(165.0);
        bookPane.getChildren().add(authorLabel);

        Label isbnLabel = new Label(book.getIsbn());
        isbnLabel.setLayoutX(166.0);
        isbnLabel.setLayoutY(81.0);
        isbnLabel.setPrefHeight(25.0);
        isbnLabel.setPrefWidth(189.0);
        bookPane.getChildren().add(isbnLabel);

        Button addButton = new Button("+ Add");
        addButton.setLayoutX(526.0);
        addButton.setLayoutY(62.0);
        addButton.setPrefHeight(32.0);
        addButton.setPrefWidth(106.0);
        addButton.setStyle("-fx-background-color: #72311c; -fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-background-radius: 5px; -fx-font-size: 15px;");
        addButton.setOnMouseEntered(event -> addButton.setStyle("-fx-background-color: #4c2113; -fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-background-radius: 5px; -fx-font-size: 15px; -fx-cursor: hand;"));
        addButton.setOnMouseExited(event -> addButton.setStyle("-fx-background-color: #72311c; -fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-background-radius: 5px; -fx-font-size: 15px;"));
        addButton.setOnAction(event -> openCreateBookScene(book));
        bookPane.getChildren().add(addButton);

        return bookPane;
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) searchButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void openCreateBookScene(Book book) {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/CreateBook.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) searchButton.getScene().getWindow();

            CreateBookController createBookController = loader.getController();
            createBookController.setBook(book);

            Stage stage = new Stage();
            stage.setTitle("Create New User");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            // Hiển thị scene
            stage.show();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
