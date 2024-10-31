package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import librio.controllers.auth.Session;
import librio.database.DatabaseConnection;
import librio.models.Book;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static librio.util.DesignUtil.cropAndClipToCircle;

public class BookController implements Initializable {

    @FXML
    private TilePane tilePane;
    @FXML
    private ImageView ClickAvatar;
    @FXML
    private Pane overlayPane;
    @FXML
    private Text title;
    @FXML
    private Label author;
    @FXML
    private Label isbn;
    @FXML
    private Label year;
    @FXML
    private Label publisher;
    @FXML
    private Text description;
    @FXML
    private ImageView bookCoverImage;
    @FXML
    private AnchorPane bookDetailsPane;
    @FXML
    private AnchorPane mainAnchorPane;
    @FXML
    private TextField searchTextField;
    @FXML
    private ComboBox<String> filterBox;
    @FXML
    private AnchorPane menuPane;
    @FXML
    private ImageView avatarUser;
    @FXML
    private Label userNameUser;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ImageView searchButton;
    private List<Book> bookList = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAvatarAndUserName();
        filterBox.getItems().addAll("Title", "Author", "Category", "Language", "Publisher", "Year published", "ISBN", "Rating");
        filterBox.getSelectionModel().selectFirst();
        overlayPane.setVisible(false); // Ẩn overlay khi khởi động
        overlayPane.setOnMouseClicked(event -> cancelBookDetail());
        loadBooksFromDatabase("");

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

    @FXML
    private void handleSearch() {
        String keyword = searchTextField.getText().trim();
        loadBooksFromDatabase(keyword);
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
    private boolean isBookDetailVisible = false;
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
        bookPane.setOnMouseClicked(event -> showBookDetails(book));

        return bookPane;
    }

    public void setAvatarAndUserName() {
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + Session.getInstance().getLoggedInUser().getAvatar();

        File file = new File(path);
        if (file.exists()) {
            Image image = new Image(file.toURI().toString());
            cropAndClipToCircle(image, avatarUser, 23);
            cropAndClipToCircle(image, ClickAvatar, 23);
        } else {
            String defaultImage = avatarsDir + "Male User.png";
            File defaultImageFile = new File(defaultImage);
            Image image = new Image(defaultImageFile.toURI().toString());
            cropAndClipToCircle(image, avatarUser, 23);
            cropAndClipToCircle(image, ClickAvatar, 23);
        }
        userNameUser.setText(Session.getInstance().getLoggedInUser().getName());
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
    private void cancelMenuButton() {
        if (isAnchorPaneVisible) {
            menuPane.toBack();
            System.out.println("hello");
            isAnchorPaneVisible = false;
        }
    }

    public void setBookDetails(Book book) {

        title.setText(book.getTitle());
        author.setText(book.getAuthor());
        year.setText("Published:    "+book.getYearPublished());
        isbn.setText("ISBN:   " + book.getIsbn());
        publisher.setText("Publisher:   " + book.getPublisher());
        description.setText(book.getDescription());
        try {
            bookCoverImage.setImage(new Image(book.getImagePath()));
        } catch (Exception e) {
            System.out.println("Không thể tải ảnh, sử dụng ảnh mặc định.");
            bookCoverImage.setImage(new Image(getClass().getResource("/images/book/defaultBook.jpg").toExternalForm()));
        }


    }

    private void showBookDetails(Book book) {
            setBookDetails(book);
            mainAnchorPane.setOpacity(0.4);
            bookDetailsPane.toFront();
            overlayPane.setVisible(true);
            isBookDetailVisible = true;

    }
    @FXML
    private void cancelBookDetail(){
        if (isBookDetailVisible) {
            bookDetailsPane.toBack();
            mainAnchorPane.setOpacity(1);
            overlayPane.setVisible(false);
            isBookDetailVisible = false;
        }
    }
    @FXML
    void logOut() throws IOException {
        Stage currenStage = (Stage) searchTextField.getScene().getWindow();
        Stage stage = new Stage();
        stage.setTitle("Librio");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent loginRoot  = loader.load();
        stage.setScene(new Scene(loginRoot));
        stage.show();
        Session.getInstance().logout();
        currenStage.close();
    }

}
