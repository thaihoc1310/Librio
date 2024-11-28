package librio.controllers.member;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import librio.cache.ImageCache;
import librio.controllers.admin.BorrowDetailController;
import librio.controllers.admin.CreateBookController;
import librio.session.Session;
import librio.database.DatabaseConnection;
import librio.models.*;
import librio.util.DatabaseUtil;
import librio.util.DesignUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static librio.util.DatabaseUtil.checkIfUserBorrowedBook;
import static librio.util.DesignUtil.cropAndClipToCircle;
import static librio.util.DesignUtil.cropToAspectRatio;

public class BookDetailController implements Initializable {

    @FXML
    private Label author;

    @FXML
    private ImageView bookCoverImage;

    @FXML
    private ScrollPane bookDetailsPane;

    @FXML
    private ImageView bookImage;

    @FXML
    private AnchorPane borrowConfirmationPane;

    @FXML
    private Label borrowDateLabel;

    @FXML
    private Label categoryLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private Text descriptionText;

    @FXML
    private Label dueDateErrorLabel;

    @FXML
    private DatePicker dueDatePicker;

    @FXML
    private VBox feedbackContainer;

    @FXML
    private Label isbnLabel;

    @FXML
    private Label languageLabel;

    @FXML
    private Text moreLessLabel;

    @FXML
    private Label pageCountLabel;

    @FXML
    private Label publishedLabel;

    @FXML
    private Label publisherLabel;

    @FXML
    private Text title;

    @FXML
    private Label titleLabel;

    @FXML
    private HBox starBox;

    @FXML
    private ImageView qrCodeImageView;

    private boolean isExpanded = false;
    private String fullDescription;
    private static final int DESCRIPTION_LIMIT = 500;

    private Book book;

    private List<Feedback> feedbackList = new ArrayList<>();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        moreLessLabel.setText("more");
        moreLessLabel.setOnMouseClicked(event -> toggleDescription());
    }

    public void setBook(Book book){
        this.book = book;
        setBookDetails();
        loadFeedbacksFromDatabase();
        displayRating();
    }

    public void displayRating(){
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
        starBox.getStyleClass().add("star-box");

        Label ratingLabel = new Label("  "+rating + " (" + getTotalBorrows() + ")");
        ratingLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #4C2113; -fx-font-weight: bold;");
        ratingLabel.setAlignment(Pos.CENTER_LEFT);

        starBox.getChildren().add(ratingLabel);
        generateAndDisplayQRCode();
    }

    public void setBookDetails(){
        title.setText(book.getTitle());
        author.setText("   " + book.getAuthor());
        isbnLabel.setText("ISBN:     " + book.getIsbn());
        publisherLabel.setText("Publisher:     " + book.getPublisher());
        pageCountLabel.setText("Page count:     " + book.getNumberOfPages());
        publishedLabel.setText("Published:     " + book.getYearPublished());
        categoryLabel.setText("Category:     " + book.getCategory());
        languageLabel.setText("Language:     " + book.getLanguage());
        fullDescription = book.getDescription();
        if (fullDescription.length() > DESCRIPTION_LIMIT) {
            descriptionText.setText(fullDescription.substring(0, DESCRIPTION_LIMIT) + "...");
            moreLessLabel.setVisible(true);
        } else {
            descriptionText.setText(fullDescription);
            moreLessLabel.setVisible(false);
        }

        String projectDir = System.getProperty("user.dir");
        String booksDir = projectDir + "/src/main/resources/images/book/";
        String path = booksDir + book.getImagePath();

        Image image = ImageCache.getInstance().getImage(path,projectDir + "defaultBook.jpg");
        cropToAspectRatio(image, bookCoverImage, 217, 315);

        setConfirmButton();
    }

    private void setConfirmButton(){
        int availableCopy = book.getAvailableCopy();
        boolean isAlreadyBorrowed = checkIfUserBorrowedBook(Session.getInstance().getLoggedInUser(),book);
        if (availableCopy == 0) {
            updateBorrowButton("Out of stock", "#9e4b3e", false);
        } else if (isAlreadyBorrowed) {
            updateBorrowButton("Borrowing", "#b57a3e", false);
        } else {
            confirmButton.setText("Borrow");
        }
    }

    private void updateBorrowButton(String text, String color, boolean isEnabled) {
        confirmButton.setText(text);
        confirmButton.setStyle("-fx-background-color: " + color);
        confirmButton.setDisable(!isEnabled);
        confirmButton.setCursor(isEnabled ? Cursor.HAND : Cursor.DEFAULT);
    }


    private void toggleDescription() {
        if (isExpanded) {
            descriptionText.setText(fullDescription.substring(0, DESCRIPTION_LIMIT) + "...");
            moreLessLabel.setText(" more");
        } else {
            descriptionText.setText(fullDescription);
            moreLessLabel.setText(" less");
        }
        isExpanded = !isExpanded;
    }

    @FXML
    private void cancelBookDetail() {
        closeStage();
    }

    private void loadFeedbacksFromDatabase() {
        feedbackList.clear();
        feedbackContainer.setSpacing(15);
        feedbackContainer.setStyle("-fx-padding: 10 10 10 10");
        String query = "SELECT id, book_id, member_id, rating, about, created_at FROM feedbacks WHERE book_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, String.valueOf(book.getId()));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String memberId = resultSet.getString("member_id");
                int rating = resultSet.getInt("rating");
                String about = resultSet.getString("about");
                String createdAt = resultSet.getString("created_at");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime localDateTime = LocalDateTime.parse(createdAt, formatter);
                Instant createdAtInstant = localDateTime.toInstant(ZoneOffset.UTC);


                Feedback feedback1 = new Feedback(id, String.valueOf(book.getId()), memberId, rating, about, createdAtInstant);
                feedbackList.add(feedback1);

                HBox feedbackBox = new HBox();
                feedbackBox.setSpacing(15);
                feedbackBox.setStyle("-fx-padding: 10; -fx-background-color: #F4F4F4; -fx-border-color: #E0E0E0; -fx-border-radius: 5; -fx-background-radius: 5;");
                feedbackBox.setAlignment(Pos.TOP_LEFT);

                User user = DatabaseUtil.getUserById(memberId);

                String projectDir = System.getProperty("user.dir");
                String avatarsDir = projectDir + "/src/main/resources/images/user/";
                String path = avatarsDir + user.getAvatar();
                ImageView avatar = new ImageView();
                avatar.setFitWidth(50);
                avatar.setFitHeight(50);

                Image image = ImageCache.getInstance().getImage(path,avatarsDir + "Male User.png");
                cropAndClipToCircle(image, avatar, 25);


                VBox detailsBox = new VBox();
                detailsBox.setSpacing(5);

                Text borrowerNameText = new Text(user.getName());
                borrowerNameText.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

                StringBuilder stars = new StringBuilder();
                for (int i = 0; i < rating; i++) {
                    stars.append("★");
                }
                Text ratingText = new Text("Rating: " + stars.toString());
                ratingText.setStyle("-fx-font-size: 12; -fx-fill: #FFB700;");

                LocalDateTime formattedDate = LocalDateTime.ofInstant(createdAtInstant, ZoneOffset.UTC);
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                Text dateText = new Text("Return at: " + formattedDate.format(dateFormatter));
                dateText.setStyle("-fx-font-size: 12; -fx-fill: #666666;");

                Text commentText = new Text(about);
                commentText.setStyle("-fx-font-size: 13; -fx-fill: #333333;");

                detailsBox.getChildren().addAll(borrowerNameText, ratingText, dateText, commentText);
                feedbackBox.getChildren().addAll(avatar, detailsBox);
                feedbackContainer.getChildren().add(feedbackBox);
            }

            if (feedbackList.isEmpty()) {
                Label noBooksLabel = new Label("No rating provided for this book");
                noBooksLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #4C2113; -fx-font-weight: bold;");
                noBooksLabel.setAlignment(Pos.CENTER);

                VBox container = new VBox(noBooksLabel);
                container.setAlignment(Pos.CENTER);
                container.setPrefHeight(feedbackContainer.getHeight() - 25);
                container.setStyle("-fx-padding: 20;");
                feedbackContainer.getChildren().addAll(container);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void generateAndDisplayQRCode() {
        String bookUrl = "https://books.google.com.vn/books?vid=ISBN" + book.getIsbn() + "&redir_esc=y";
        int size = 350;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix bitMatrix = qrCodeWriter.encode(bookUrl, BarcodeFormat.QR_CODE, size, size, hints);

            WritableImage qrCodeImage = new WritableImage(size, size);
            PixelWriter pixelWriter = qrCodeImage.getPixelWriter();

            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    Color color = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                    pixelWriter.setColor(x, y, color);
                }
            }

            qrCodeImageView.setImage(qrCodeImage);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void closeStage() {
        Stage stage = (Stage) title.getScene().getWindow();
        stage.close();
    }

    private int getTotalBorrows() {
        int total = 0;
        String query = "select count(id) from borrows where book_isbn = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, String.valueOf(book.getIsbn()));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                total = resultSet.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }
    @FXML
    private void openBorrowConfirmationPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/ConfirmBorrow.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) author.getScene().getWindow();
            ConfirmBorrow confirmBorrow = loader.getController();
            confirmBorrow.setBook(book);
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(currentStage);
            stage.setOnShown(event -> {
                stage.setX(currentStage.getX() + (currentStage.getWidth() - stage.getWidth()) / 2);
                stage.setY(currentStage.getY() + (currentStage.getHeight() - stage.getHeight()) / 2);
            });

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);
            });
            stage.showAndWait();
            setConfirmButton();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
