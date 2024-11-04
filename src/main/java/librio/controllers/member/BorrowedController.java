package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
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

public class BorrowedController implements Initializable {
    @FXML
    private TabPane tabPane;
    @FXML
    private TilePane tilePane;
    private List<Book> borrowBookList = new ArrayList<>();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    loadBorrowBookFromDatabase();

    }
private void loadBorrowBookFromDatabase() {
    borrowBookList.clear();
    try (Connection connection = DatabaseConnection.getConnection()) {
        String query;
        PreparedStatement preparedStatement;
        query = "SELECT id, title, author, isbn, category, publisher, quantity_copy," +
                " average_of_rating, year_published, language, number_of_pages, description, book_image FROM books";
        preparedStatement = connection.prepareStatement(query);
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

            borrowBookList.add(book);
        }
    }
    catch (Exception e) {
        e.printStackTrace();
    }
    displayBorrowedBooks(borrowBookList);
}
    private void displayBorrowedBooks(List<Book> booksToDisplay) {
        tilePane.getChildren().clear();
        for (Book book : booksToDisplay) {
            AnchorPane bookPane = createBookPane(book);
            tilePane.getChildren().add(bookPane);
        }
        tilePane.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double paneWidth = newWidth.doubleValue();
//            adjustBookPaneLayout(paneWidth);
        });

//        adjustBookPaneLayout(tilePane.getWidth());
    }
    private AnchorPane createBookPane(Book book) {
        AnchorPane bookPane = new AnchorPane();
        bookPane.setPrefSize(170, 310);

        // Image view setup
        ImageView bookImageView = new ImageView();
        bookImageView.setFitWidth(150);
        bookImageView.setFitHeight(180);
        bookImageView.setLayoutX(10);
        bookImageView.setLayoutY(10);


        String projectDir = System.getProperty("user.dir");
        String booksDir = projectDir + "/src/main/resources/images/book/";
        String path = booksDir + book.getImagePath();
        File file = new File(path);
        bookImageView.setImage(new Image(file.toURI().toString()));
        // Title label setup
        Label titleLabel = new Label(book.getTitle());
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        titleLabel.setPrefWidth(150);
        titleLabel.setLayoutX(10);
        titleLabel.setLayoutY(200);

        // Remaining days label setup
        Label daysRemainingLabel = new Label("Days remaining: 10"); // Replace "10" with actual calculation logic
        daysRemainingLabel.setTextFill(javafx.scene.paint.Color.RED);
        daysRemainingLabel.setStyle("-fx-font-size: 12px;");
        daysRemainingLabel.setLayoutX(10);
        daysRemainingLabel.setLayoutY(240);

        // More button setup
        Button moreButton = new Button("More");
        moreButton.setVisible(false);
        moreButton.setStyle("-fx-background-color: #ff7f50; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand;");
        moreButton.setLayoutX(60);
        moreButton.setLayoutY(270);

        // Hover effect to show button
        bookPane.setOnMouseEntered(e -> moreButton.setVisible(true));
        bookPane.setOnMouseExited(e -> moreButton.setVisible(false));

        // Add all components to the anchor pane
        bookPane.getChildren().addAll(bookImageView, titleLabel, daysRemainingLabel, moreButton);

        return bookPane;
    }




}
