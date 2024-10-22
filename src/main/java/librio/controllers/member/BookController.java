package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import librio.controllers.admin.BookDetailController;
import librio.database.DatabaseConnection;
import librio.models.Book;

import java.io.IOException;
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
    private TextField searchTextField;

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

                // Kiểm tra nếu book_image là null hoặc rỗng, sử dụng ảnh mặc định
                if (imageBook == null || imageBook.isEmpty()) {
                    imageBook = getClass().getResource("/images/book/defaultBook.jpg").toExternalForm(); // Đường dẫn đến ảnh mặc định
                } else {
                    imageBook = getClass().getResource("/images/book/" + imageBook).toExternalForm();
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
            AnchorPane bookPane = createBookPane(book); // Tạo AnchorPane cho từng cuốn sách
            tilePane.getChildren().add(bookPane);      // Thêm vào tilePane
        }
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
        bookCover.setImage(new Image(book.getImagePath()));
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
//        bookInfo.setTextAlignment(TextAlignment.CENTER);
        bookInfo.setMaxWidth(Double.MAX_VALUE);

        // Thêm TextFlow vào AnchorPane
        AnchorPane.setLeftAnchor(bookInfo, 29.0);
        AnchorPane.setRightAnchor(bookInfo, 29.0);
        AnchorPane.setTopAnchor(bookInfo, 320.0);
        bookPane.getChildren().add(bookInfo);

        // Thêm HBox để hiển thị rating bằng ngôi sao
        HBox starBox = new HBox(5);
//        starBox.setAlignment(Pos.CENTER); // Căn giữa các ngôi sao

        double rating = book.getAverageOfRating(); // Giả sử bạn có phương thức getRating() trả về số sao (từ 1 đến 5)
        for (int i = 1; i <= 5; i++) {
            ImageView star = new ImageView();
            if (i <= rating) {
                star.setImage(new Image(getClass().getResource("/images/book/ratings/Star.png").toExternalForm())); // Hình ảnh ngôi sao đầy
            }
//            else {
//                star.setImage(new Image("/path/to/empty_star.png")); // Hình ảnh ngôi sao trống
//            }
            star.setFitHeight(15); // Kích thước chiều cao của ngôi sao
            star.setFitWidth(15);  // Kích thước chiều rộng của ngôi sao
            starBox.getChildren().add(star);

        }

        // Đặt vị trí cho HBox (starBox) trong AnchorPane
        AnchorPane.setTopAnchor(starBox, 370.0); // Đặt dưới tiêu đề sách
        AnchorPane.setLeftAnchor(starBox, 29.0);
        AnchorPane.setRightAnchor(starBox, 29.0);
        bookPane.getChildren().add(starBox);
        bookPane.setOnMouseClicked(event -> showBookDetails(book));
        return bookPane; // Trả về AnchorPane chứa thông tin cuốn sách và đánh giá
    }

    private void showBookDetails(Book book) {
        try {
            // Tải file FXML của Book Detail
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/BookDetail.fxml"));
            Parent root = loader.load();

            // Lấy controller từ FXML loader và thiết lập thông tin sách
            BookForMemberDetailController controller = loader.getController();
            controller.setBookDetails(book);

            Stage detailStage = new Stage();
            detailStage.setTitle("Thông tin chi tiết sách");

            Scene scene = new Scene(root);
            detailStage.setScene(scene);
            detailStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * Tìm kiếm sách dựa trên từ khóa nhập vào
     */
    @FXML
    private void searchBooks() {
        String keyword = searchTextField.getText().trim().toLowerCase();
        List<Book> filteredBooks = new ArrayList<>();

        // Lọc sách dựa trên từ khóa tìm kiếm
        for (Book book : bookList) {
            if (book.getTitle().toLowerCase().contains(keyword)) {
                filteredBooks.add(book);
            }
        }

        // Hiển thị sách đã lọc
        displayBooks(filteredBooks);
    }
}
