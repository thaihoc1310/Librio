package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import librio.auth.Session;
import librio.database.DatabaseConnection;
import librio.models.Book;
import librio.util.DesignUtil;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class BorrowConfirmationController implements Initializable {
    private Book book;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    private void loadBookDetails() {
        authorNameLabel.setText(book.getAuthor());
        titleLabel.setText(book.getTitle());
        publisherLabel.setText("Publisher:   " + book.getPublisher());
        languageLabel.setText("Language:   " + book.getLanguage());
        borrowDateLabel.setText("Borrow Date:    " + LocalDate.now().toString());

        averageRatingBox.getChildren().clear();

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

            averageRatingBox.getChildren().add(starPane);

            String projectDir = System.getProperty("user.dir");
            String booksDir = projectDir + "/src/main/resources/images/book/";
            String path = booksDir + book.getImagePath();
            File file = new File(path);
            Image image;


            if(file.exists()) {
                image = new Image(file.toURI().toString());
            }else{
                image = new Image(getClass().getResource("/images/book/defaultBook.jpg").toExternalForm());
            }

            DesignUtil.cropToAspectRatio(image,bookImage,137,182);
        }
    }

    public void setBook(Book book) {
        this.book = book;
        loadBookDetails();
    }

    @FXML
    private Label authorNameLabel;

    @FXML
    private HBox averageRatingBox;

    @FXML
    private Label borrowDateLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private Label languageLabel;

    @FXML
    private Label publisherLabel;

    @FXML
    private Label dueDateErrorLabel;

    @FXML
    private DatePicker dueDatePicker;

    @FXML
    private Label titleLabel;

    @FXML
    private ImageView bookImage;


    @FXML
    private void cancelBookBorrowConfirmation() {
        closeStage();
    }

    @FXML
    private void confirm() {
        LocalDate dueDate = dueDatePicker.getValue();

        boolean validation = false;

        if(dueDate == null) {
            dueDateErrorLabel.setText("Please choose your expected return date!");
            validation = true;
        }else if(dueDate.isBefore(dueDate)) {
            dueDateErrorLabel.setText("Expected return date must be after today!");
            validation = true;
        }else if(ChronoUnit.DAYS.between(LocalDate.now(), dueDate) > 60){
            dueDateErrorLabel.setText("The borrowing period cannot exceed 60 days!");
            validation = true;
        }else{
            dueDateErrorLabel.setText("");
        }

        if(validation) {
            return;
        }

        String query = "INSERT INTO borrows (member_id, book_isbn, borrow_date, due_date, return_date, status, fine, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, Session.getInstance().getLoggedInUser().getId());
            statement.setString(2, book.getIsbn());
            statement.setString(3, LocalDate.now().toString());
            statement.setString(4, dueDate.toString());
            statement.setString(5, null);
            statement.setString(6, "BORROWING");
            statement.setString(7, String.valueOf(0));
            statement.setString(8, LocalDateTime.now().toString());

            int rowsInserted = statement.executeUpdate();
            closeStage();


            Stage currentStage = (Stage) confirmButton.getScene().getWindow();
            Stage bookDetailStage = (Stage) currentStage.getOwner(); // Lấy BookDetail stage

            currentStage.close();
            if (bookDetailStage != null) {
                bookDetailStage.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void closeStage() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }
}
