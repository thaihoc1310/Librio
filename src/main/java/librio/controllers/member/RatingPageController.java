package librio.controllers.member;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import librio.auth.Session;
import librio.cache.ImageCache;
import librio.database.DatabaseConnection;
import librio.models.Book;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import static librio.util.DatabaseUtil.updateBookAverageRating;

public class RatingPageController {

    @FXML
    private Label authorNameLabel;

    @FXML
    private ImageView bookImage;

    @FXML
    private TextArea commentBox;

    @FXML
    private HBox starBox;

    @FXML
    private Button submitButton;

    @FXML
    private Label titleLabel;

    private Book book;
    private int borrowId;

    public void setBookAndBorrowId (Book book, int borrowId) {
        this.book = book;
        this.borrowId = borrowId;

        titleLabel.setText(book.getTitle());
        authorNameLabel.setText(book.getAuthor());

        String projectDir = System.getProperty("user.dir");
        String booksDir = projectDir + "/src/main/resources/images/book/";
        String path = booksDir + book.getImagePath();
        Image image = ImageCache.getInstance().getImage(path,booksDir + "Male User.png");
        bookImage.setImage(image);
    }

    private final int starCount = 5;
    private int selectedStars = 0;

    private final Image emptyStar = new Image(getClass().getResource("/icons/MemberIcon/Star_notfill.png").toExternalForm());
    private final Image fullStar = new Image(getClass().getResource("/icons/MemberIcon/Star.png").toExternalForm());

    @FXML
    public void initialize() {
        for (int i = 0; i < starCount; i++) {
            Pane starPane = createStarPane(i);
            starBox.getChildren().add(starPane);
        }
        Platform.runLater(() -> {
            starBox.requestFocus();
        });
    }

    private Pane  createStarPane (int index) {

        ImageView star = new ImageView(emptyStar);
        star.setFitHeight(20);
        star.setFitWidth(20);
        star.setX(10);
        Pane starPane = new Pane(star);
        starPane.setPrefSize(45, 20);
        starPane.setStyle("-fx-cursor: hand;");

        starPane.setOnMouseEntered(event -> {
            updateStars(index + 1, fullStar);
        });

        starPane.setOnMouseExited(event -> {
            updateStars(selectedStars, fullStar);
        });

        starPane.setOnMouseClicked(event -> {
            selectedStars = index + 1;
            updateStars(selectedStars, fullStar);
        });

        return starPane;
    }

    private void updateStars(int starCount, Image fullStar) {
        for (int i = 0; i < starBox.getChildren().size(); i++) {
            Pane starPane = (Pane) starBox.getChildren().get(i); // Lấy Pane
            ImageView star = (ImageView) starPane.getChildren().getFirst();
            if (i < starCount) {
                star.setImage(fullStar);
            } else {
                star.setImage(new Image(getClass().getResource("/icons/MemberIcon/Star_notfill.png").toExternalForm())); // Đổi ngôi sao rỗng
            }
        }
    }

    @FXML
    private void confirmAction() {
        String query = "INSERT INTO feedbacks (member_id, book_id, borrow_id, rating, about, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, Session.getInstance().getLoggedInUser().getId());
            statement.setInt(2, book.getId());
            statement.setInt(3, borrowId);
            statement.setInt(4, selectedStars);
            statement.setString(5, commentBox.getText());
            statement.setString(6, Session.getInstance().getLoggedInUser().getEmail());
            statement.setString(7, LocalDateTime.now().toString());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                updateBookAverageRating(book.getIsbn());
            }
            closeStage();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void closeStage() {
        Stage stage = (Stage) authorNameLabel.getScene().getWindow();
        stage.close();
    }


}
