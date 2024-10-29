package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.text.TextFlow;
import librio.database.DatabaseConnection;
import librio.models.Book;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BookController implements Initializable {

    @FXML
    private TilePane tilePane;
    @FXML
    private ImageView ClickAvatar;
    @FXML
    private TextField searchTextField;
    @FXML
    private ComboBox<String> filterBox;
    @FXML
    private AnchorPane menuPane;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ImageView searchButton;
    private List<Book> bookList = new ArrayList<>();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filterBox.getItems().addAll("Title", "Author", "Category", "Language", "Publisher", "Year published", "ISBN", "Rating");
        filterBox.getSelectionModel().selectFirst();

        loadBooksFromDatabase("");
//        loadBooksFromGoogleAPI("");

    }

    /**
     * Tải danh sách sách từ cơ sở dữ liệu
     */
    private void loadBooksFromDatabase(String keyword) {
        bookList.clear();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String selectedFilter = filterBox.getValue();
            String query;
            PreparedStatement preparedStatement;

            if (keyword == null || keyword.isEmpty()) {
                query = "SELECT id, title, author, isbn, category, publisher, quantity_copy, average_of_rating, year_published, language, number_of_pages, description, book_image FROM books";
                preparedStatement = connection.prepareStatement(query);
            } else {
                switch (selectedFilter) {
                    case "Author":
                        query = "SELECT * FROM books WHERE author LIKE ?";
                        break;
                    case "ISBN":
                        query = "SELECT * FROM books WHERE isbn LIKE ?";
                        break;
                    case "Category":
                        query = "SELECT * FROM books WHERE category LIKE ?";
                        break;
                    case "Language":
                        query = "SELECT * FROM books WHERE language LIKE ?";
                        break;
                    case "Publisher":
                        query = "SELECT * FROM books WHERE publisher LIKE ?";
                        break;
                    case "Year published":
                        query = "SELECT * FROM books WHERE year_published LIKE ?";
                        break;
                    case "Rating":
                        query = "SELECT * FROM books WHERE average_of_rating LIKE ?";
                        break;
                    case "Title":
                    default:
                        query = "SELECT * FROM books WHERE title LIKE ?";
                        break;
                }
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, "%" + keyword + "%");
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

                // Tạo đối tượng Book với tất cả các thuộc tính
                Book book = new Book(id, title, author, isbn, category, publisher, quantityCopy, averageOfRating, yearPublished, language, numberOfPages, description, imageBook);

                // Thêm vào danh sách
                bookList.add(book);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        displayBooks(bookList);
    }

    private void loadBooksFromGoogleAPI(String keyword) {
        bookList.clear();
        try {
            String apiKey = "AIzaSyBRX3PmHB6TlSwDsU5KmcbexZxISjyd9hI";
            String apiUrl;
            if (keyword == null || keyword.isEmpty()) {
                apiUrl = "https://www.googleapis.com/books/v1/volumes?q=a&maxResults=40&key=" + apiKey;
            } else {
                apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + keyword + "&key=" + apiKey;
            }
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            JSONObject jsonResponse = new JSONObject(content.toString());
            JSONArray items = jsonResponse.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject volumeInfo = items.getJSONObject(i).getJSONObject("volumeInfo");

                String id = items.getJSONObject(i).getString("id");
                String title = volumeInfo.has("title") ? volumeInfo.getString("title") : "Unknown Title";
                String author = volumeInfo.has("authors") ? volumeInfo.getJSONArray("authors").getString(0) : "Unknown Author";
                String isbn = volumeInfo.has("industryIdentifiers") ? volumeInfo.getJSONArray("industryIdentifiers").getJSONObject(0).getString("identifier") : "Unknown ISBN";
                String category = volumeInfo.has("categories") ? volumeInfo.getJSONArray("categories").getString(0) : "Unknown Category";
                String publisher = volumeInfo.has("publisher") ? volumeInfo.getString("publisher") : "Unknown Publisher";
                String yearPublished = volumeInfo.has("publishedDate") ? volumeInfo.getString("publishedDate") : "Unknown Year";
                String language = volumeInfo.has("language") ? volumeInfo.getString("language") : "Unknown Language";
                String description = volumeInfo.has("description") ? volumeInfo.getString("description") : "No Description";
                String imageBook = volumeInfo.has("imageLinks") ? volumeInfo.getJSONObject("imageLinks").getString("thumbnail") : "defaultBook.jpg";

                // Tạo đối tượng Book với tất cả các thuộc tính
                Book book = new Book(Integer.parseInt(id), title, author, isbn, category, publisher, 2, 5.0, yearPublished, language, "100", description, imageBook);

                // Thêm vào danh sách
                bookList.add(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        displayBooks(bookList);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchTextField.getText().trim();
        loadBooksFromDatabase(keyword);
//        loadBooksFromGoogleAPI(keyword);
    }

    /**
     * Hiển thị danh sách các cuốn sách trong TilePane
     */
    private void displayBooks(List<Book> booksToDisplay) {
        tilePane.getChildren().clear();
        for (Book book : booksToDisplay) {
            AnchorPane bookPane = createBookPane(book);
            tilePane.getChildren().add(bookPane);
        }
        tilePane.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double paneWidth = newWidth.doubleValue();
            adjustBookPaneLayout(paneWidth);
        });

        adjustBookPaneLayout(tilePane.getWidth());
    }



    private void adjustBookPaneLayout(double tilePaneWidth) {
        if (tilePaneWidth <= 0) return;

        double horizontalPadding = (tilePaneWidth - 1270) / 2;

        tilePane.setPadding(new Insets(230, 0, 10, horizontalPadding));
    }

    /**
     * Tạo một AnchorPane cho mỗi cuốn sách
     */
    private AnchorPane createBookPane(Book book) {
        AnchorPane bookPane = new AnchorPane();
        bookPane.setPrefSize(270, 400);
        bookPane.getStyleClass().add("tilePane-book");

        // Thiết lập ImageView cho hình ảnh bìa sách
        ImageView bookCover = new ImageView();
        bookCover.setFitHeight(314);
        bookCover.setFitWidth(215);
        bookCover.setX(29);  // Đảm bảo hình ảnh nằm giữa AnchorPane
        bookCover.setY(1);

        // set image
        String projectDir = System.getProperty("user.dir");
        String booksDir = projectDir + "/src/main/resources/images/book/";
        String path = booksDir + book.getImagePath();
        File file = new File(path);
        bookCover.setImage(new Image(file.toURI().toString()));

        bookCover.setPickOnBounds(true);
        bookCover.setPreserveRatio(true);
        bookPane.getChildren().add(bookCover);

        // Tạo TextFlow chứa tiêu đề
        TextFlow bookInfo = new TextFlow();
        Label titleLabel = new Label(book.getTitle());

        // Giới hạn Label chỉ hiển thị 2 dòng và thêm dấu "..." nếu quá dài
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(215);  // Giới hạn chiều rộng tối đa cho tiêu đề
        titleLabel.setStyle("-fx-font-weight: 200;");  // Giảm độ dày của chữ
        titleLabel.setMaxHeight(48);  // Chiều cao tối đa cho 2 dòng

        // Căn giữa TextFlow theo chiều ngang trong AnchorPane
        bookInfo.getChildren().addAll(titleLabel);
        bookInfo.setMaxWidth(Double.MAX_VALUE);

        // Thêm TextFlow vào AnchorPane
        AnchorPane.setLeftAnchor(bookInfo, 29.0);
        AnchorPane.setRightAnchor(bookInfo, 29.0);
        AnchorPane.setTopAnchor(bookInfo, 320.0);
        bookPane.getChildren().add(bookInfo);

        // Thêm HBox để hiển thị rating bằng ngôi sao
        HBox starBox = new HBox(5);

        double rating = book.getAverageOfRating(); // Giả sử bạn có phương thức getRating() trả về số sao (từ 1 đến 5)
        for (int i = 1; i <= 5; i++) {
            ImageView star = new ImageView();
            if (i <= rating) {
                star.setImage(new Image(getClass().getResource("/images/book/ratings/Star.png").toExternalForm())); // Hình ảnh ngôi sao đầy
            }

            star.setFitHeight(15);
            star.setFitWidth(15);
            starBox.getChildren().add(star);
        }


        AnchorPane.setTopAnchor(starBox, 370.0);
        AnchorPane.setLeftAnchor(starBox, 29.0);
        AnchorPane.setRightAnchor(starBox, 29.0);
        bookPane.getChildren().add(starBox);

//        bookPane.setOnMouseClicked(event -> showBookDetails(book));
        return bookPane;
    }
    private boolean isAnchorPaneVisible = false;

@FXML
private void handleAvatarClick() {
    if (!isAnchorPaneVisible) {
        menuPane.toFront();
        isAnchorPaneVisible = true;
    } else {
        menuPane.toBack();
        isAnchorPaneVisible = false;
    }
}
@FXML
    private void cancelMenuButton(){
    if(isAnchorPaneVisible){
        menuPane.toBack();
        isAnchorPaneVisible = false;
    }
}
//    private void showBookDetails(Book book) {
//        try {
//
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/BookDetail.fxml"));
//            Parent bookDetailRoot = loader.load();
//
//            BookDetailController controller = loader.getController();
//            controller.setBookDetails(book);
//
//            double scrollPosition = scrollPane.getVvalue();
//            String currentSearch = searchTextField.getText();
//
//            Stage currentStage = (Stage) searchTextField.getScene().getWindow();
//            Scene currentScene = currentStage.getScene();
//
//            Scene bookDetailScene = new Scene(bookDetailRoot);
//            currentStage.setScene(bookDetailScene);
//
//            controller.setOnBackAction(() -> {
//                currentStage.setScene(currentScene);
//                scrollPane.setVvalue(scrollPosition);
//                searchTextField.setText(currentSearch);
//            });
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}
