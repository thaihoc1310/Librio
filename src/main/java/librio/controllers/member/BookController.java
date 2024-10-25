package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.text.TextFlow;
import librio.database.DatabaseConnection;
import librio.models.Book;

import java.io.File;
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
    private AnchorPane menuPane;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Pagination pagination;

    private List<Book> bookList = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load books from database
        loadBooksFromDatabase();
        displayBooks(bookList);
    }

    /**
     * Tải danh sách sách từ cơ sở dữ liệu
     */
    private void loadBooksFromDatabase() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT id, title, author, isbn, category, publisher, quantity_copy, average_of_rating, year_published, language, number_of_pages, description, book_image FROM books";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String id = resultSet.getString("id");
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

                if (imageBook == null ) {
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
        System.out.println(tilePaneWidth);

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
