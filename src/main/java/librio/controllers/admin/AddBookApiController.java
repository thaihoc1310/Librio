package librio.controllers.admin;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import librio.models.Book;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Character.isDigit;

/**
 * The AddBookApiController class is responsible for managing the interactions
 * between the user interface and the Google Books API. It handles the logic for
 * searching books, displaying them in the UI, and enabling user actions such as
 * adding a book to a collection. The controller is initialized with interactive
 * elements such as a search button, filter options, and text fields to facilitate
 * dynamic user input.
 */
public class AddBookApiController implements Initializable {
    @FXML
    private ImageView searchButton;
    @FXML
    private ComboBox<String> filterBox;
    @FXML
    private TextField searchTextField;
    @FXML
    private ScrollPane bookListScrollPane;
    @FXML
    private ProgressIndicator loadingIndicator;

    private final List<Book> bookList = new ArrayList<>();

    private ExecutorService executor;

    private int startIndex = 0;

    private int totalItems = 0;

    private final VBox contentPane = new VBox(10);

    /**
     * Initializes the controller after its root element has been completely processed.
     * This method sets up the initial state of the filter options, initializes the
     * executor for handling asynchronous tasks, and initiates the loading of books.
     *
     * @param location The location used to resolve relative paths for the root object, or
     *                 null if the location is not known.
     * @param resources The resources used to localize the root object, or null if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filterBox.getItems().addAll("Title", "Author", "Category", "Language", "Year published", "ISBN");
        filterBox.getSelectionModel().selectFirst();
        executor = Executors.newFixedThreadPool(2);
        bookList.clear();
        loadBooksAsync("");
    }

    /**
     * Asynchronously loads books based on the provided search keyword.
     * Initiates a background task to fetch books from the Google API using the search keyword,
     * updates the UI with the fetched books, and handles any task failures.
     *
     * @param searchKeyWord the keyword used to search for books through the Google API
     */
    private void loadBooksAsync(String searchKeyWord) {
        Platform.runLater(() -> loadingIndicator.setVisible(true));
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

    /**
     * Fetches a list of books from the Google Books API based on the provided search keyword.
     *
     * @param searchKeyWord the search keyword used to query the Google Books API.
     * @return a list of books that match the search criteria.
     */
    private List<Book> loadBooksFromGoogleAPI(String searchKeyWord) {
        List<Book> fetchedBooks = new ArrayList<>();
        try {
            String apiKey = "AIzaSyBRX3PmHB6TlSwDsU5KmcbexZxISjyd9hI";
            String filter = filterBox.getValue();
            String encodeKeyword = java.net.URLEncoder.encode(searchKeyWord, StandardCharsets.UTF_8);
            JSONObject responseJson = getJsonObject(filter, encodeKeyword, apiKey);
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
                    String imageBook = volumeInfo.has("imageLinks") ? volumeInfo.getJSONObject("imageLinks").getString("smallThumbnail") : null;
                    Integer numberOfPages = volumeInfo.optInt("pageCount", 0);
                    if (isDigit(isbn.charAt(0))) isbn = "ISBN : " + isbn;
                    Book book = new Book(0, title, author, isbn, category, publisher, 0, 0, 0.0, yearPublished, language, String.valueOf(numberOfPages), description, imageBook);
                    fetchedBooks.add(book);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fetchedBooks;
    }

    /**
     * Fetches a JSONObject from the constructed API URL based on the given filter, encoded keyword, and API key.
     *
     * @param filter the filter parameter used to customize the API query
     * @param encodeKeyword the keyword part of the query, URL-encoded
     * @param apiKey the API key required to authenticate and access the service
     * @return a JSONObject containing the response from the API
     * @throws IOException if an I/O error occurs during the API request
     */
    private JSONObject getJsonObject(String filter, String encodeKeyword, String apiKey) throws IOException {
        String apiUrl = getApiUrl(filter, encodeKeyword, apiKey);

        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        return new JSONObject(content.toString());
    }

    /**
     * Constructs the API URL for accessing the Google Books API based on the specified
     * filter, encoded keyword, and API key. The constructed URL is used to retrieve
     * a list of books according to the given search criteria.
     *
     * @param filter Specifies the type of filter to apply to the search query.
     *               Valid options include "Title", "Author", "Category", "Year published",
     *               "ISBN", or "Language".
     * @param encodeKeyword The keyword to be encoded and appended to the search query in the API URL.
     * @param apiKey The API key required for authenticating the request to the Google Books API.
     *
     * @return A string representing the constructed API URL containing the search query parameters.
     */
    private String getApiUrl(String filter, String encodeKeyword, String apiKey) {
        String apiUrl;
        if (filter.equals("Language")) {
            apiUrl = "https://www.googleapis.com/books/v1/volumes?q=\"\""
                    + "&langRestrict=" + encodeKeyword
                    + "&startIndex=" + startIndex
                    + "&maxResults=15&key=" + apiKey;
        } else {
            apiUrl = "https://www.googleapis.com/books/v1/volumes?q="
                    + getFilter(filter) + encodeKeyword
                    + "&startIndex=" + startIndex
                    + "&maxResults=15&key=" + apiKey;
        }
        return apiUrl;
    }

    /**
     * Handles the search functionality triggered by the user. This method sets up the
     * environment for performing a new search by resetting necessary fields and states.
     * If there is an existing executor for asynchronous tasks, it is shut down before
     * initiating a new one. The search keyword is fetched from the user input, and the
     * current list of books is cleared to accommodate new search results. It also resets
     * the scroll position of the book list display.
     */
    @FXML
    private void handleSearch() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        executor = Executors.newFixedThreadPool(2);
        String keyword = searchTextField.getText().trim();
        startIndex = 0;
        totalItems = 0;
        bookList.clear();
        loadBooksAsync(keyword);
        bookListScrollPane.setVvalue(0);
    }

    /**
     * Maps a given filter name to its corresponding query prefix for the Google Books API.
     *
     * @param filter The filter name to be mapped. Valid values include "Title", "Author",
     *               "Category", "Year published", and "ISBN".
     * @return A string representing the query prefix associated with the filter name.
     *         Returns an empty string if the filter is not recognized.
     */
    private String getFilter(String filter) {
        return switch (filter) {
            case "Title" -> "intitle:";
            case "Author" -> "inauthor:";
            case "Category" -> "subject:";
            case "Year published" -> "publishedDate:";
            case "ISBN" -> "isbn:";
            default -> "";
        };
    }

    /**
     * Displays a list of books in the user interface, updating the content pane
     * to reflect the current set of books and providing a button to load more
     * if applicable.
     *
     * @param booksToDisplay the list of Book objects to be displayed in the UI.
     */
    private void displayBooks(List<Book> booksToDisplay) {
        Platform.runLater(() -> {
            if (startIndex == 0) {
                contentPane.setStyle("-fx-padding: 10 0 0 0;");
                contentPane.getChildren().clear();
                contentPane.setPrefWidth(bookListScrollPane.getPrefWidth());
            } else {
                if (!contentPane.getChildren().isEmpty() && contentPane.getChildren().getLast() instanceof Button) {
                    contentPane.getChildren().removeLast();
                }
            }

            for (Book book : booksToDisplay) {
                AnchorPane bookPane = createBookPane(book);
                contentPane.getChildren().add(bookPane);
            }

            if ((startIndex + 15) < totalItems) {
                Button moreButton = getMoreButton();
                contentPane.getChildren().add(moreButton);
            }

            bookListScrollPane.setContent(contentPane);
            bookListScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            bookListScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            bookListScrollPane.setPannable(false);
        });
    }

    /**
     * Creates and returns a "More" button configured with specific styling and behavior.
     * The button is styled to match the application's visual design and includes event handlers
     * for mouse hover to change its appearance and a click action that loads more book entries
     * asynchronously from the Google Books API starting from an incremented index.
     *
     * @return a Button instance labeled "More" that fetches additional book entries when clicked
     */
    private Button getMoreButton() {
        Button moreButton = new Button("More");
        moreButton.setPrefHeight(32.0);
        moreButton.setPrefWidth(667.0);
        moreButton.setStyle("-fx-background-color: #72311c; -fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-background-radius: 5px; -fx-font-size: 15px;");
        moreButton.setOnMouseEntered(event -> moreButton.setStyle("-fx-background-color: #4c2113; -fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-background-radius: 5px; -fx-font-size: 15px; -fx-cursor: hand;"));
        moreButton.setOnMouseExited(event -> moreButton.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-background-radius: 5px; -fx-font-size: 15px;-fx-background-color: #72311c;"));
        moreButton.setOnAction(event -> {
            startIndex += 15;
            loadBooksAsync(searchTextField.getText().trim());
        });
        return moreButton;
    }

    /**
     * Constructs and returns an AnchorPane representing the UI layout for a specific book.
     * This method sets up the visual components such as image, title, author, and ISBN for the book,
     * and includes an "Add" button for user interaction.
     *
     * @param book the Book object containing information about the book to display.
     * @return an AnchorPane configured with book details and an interactive "Add" button.
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
        if (book.getImagePath() != null) {
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

        Button addButton = getAddButton(book);
        bookPane.getChildren().add(addButton);

        return bookPane;
    }

    /**
     * Creates and returns a styled "Add" button with specific behavior and appearance for adding a book.
     * The button includes mouse event handlers to change its style when hovered over, and triggers an action
     * to open a scene for creating a book entry when clicked.
     *
     * @param book the book object to be potentially added, which the button action will utilize.
     * @return a Button instance labeled "+ Add" configured with styling and event handling for interaction.
     */
    private Button getAddButton(Book book) {
        Button addButton = new Button("+ Add");
        addButton.setLayoutX(526.0);
        addButton.setLayoutY(62.0);
        addButton.setPrefHeight(32.0);
        addButton.setPrefWidth(106.0);
        addButton.setStyle("-fx-background-color: #72311c; -fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-background-radius: 5px; -fx-font-size: 15px;");
        addButton.setOnMouseEntered(event -> addButton.setStyle("-fx-background-color: #4c2113; -fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-background-radius: 5px; -fx-font-size: 15px; -fx-cursor: hand;"));
        addButton.setOnMouseExited(event -> addButton.setStyle("-fx-background-color: #72311c; -fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-background-radius: 5px; -fx-font-size: 15px;"));
        addButton.setOnAction(event -> openCreateBookScene(book));
        return addButton;
    }

    /**
     * Closes the current window of the application.
     * This method retrieves the window associated with the 'searchButton'
     * and calls the close method on its stage to terminate the window.
     */
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) searchButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Opens the scene for creating or editing a book. This method prepares
     * and displays a new stage containing the CreateBook view, initialized
     * with the given book's data. If the book's ISBN is not available,
     * it invokes an alternative method to handle that case.
     *
     * @param book The Book object which contains information to be displayed
     *             or edited in the CreateBook scene. The book must have a valid
     *             ISBN; otherwise, an alternative scene is triggered.
     */
    @FXML
    private void openCreateBookScene(Book book) {
        try {
            if (!book.getIsbn().contains("ISBN") || book.getIsbn().equals("Unknown ISBN")) {
                openIsbnNotAvailable();
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/CreateBook.fxml"));
            Parent root = loader.load();

            Stage addBookStage = (Stage) searchButton.getScene().getWindow();
            CreateBookController createBookController = loader.getController();
            createBookController.setBook(book);
            Stage createBookStage = new Stage();
            createBookStage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            createBookStage.setScene(scene);
            createBookStage.setResizable(false);
            createBookStage.initOwner(addBookStage);
            createBookStage.initModality(Modality.APPLICATION_MODAL);
            createBookStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the "ISBN Not Available" dialog window to inform the user when a book's ISBN
     * cannot be used. This method loads the appropriate FXML layout, applies a brightness
     * effect to darken the current window, and displays the dialog in a modal stage.
     * The dialog is displayed transparently and non-resizable, and upon closing, it
     * removes any visual effects applied to the underlying window.
     * Handles IOException internally by printing the stack trace, in case of any loading
     * errors with the FXML file.
     */
    @FXML
    private void openIsbnNotAvailable() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/IsbnNotAvailable.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) searchButton.getScene().getWindow();
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(searchButton.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {

                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);
            });
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
