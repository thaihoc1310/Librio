package librio.controllers.member;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import librio.auth.Session;
import librio.database.DatabaseConnection;
import librio.models.Book;
import librio.models.BorrowedBook;
import librio.models.Status;
import librio.util.DesignUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static librio.models.Status.BORROWING;
import static librio.models.Status.OVERDUE;
import static librio.util.DesignUtil.cropAndClipToCircle;

public class BorrowedController implements Initializable {
    @FXML
    private TabPane tabPane;
    @FXML
    private ImageView avatarUser;
    @FXML
    private ImageView ClickAvatar;
    @FXML
    private Label userNameUser;
    @FXML
    private Label userNameUser2;
    @FXML
    private Circle moreIcon;
    @FXML
    private AnchorPane menuPane;
    @FXML
    private TilePane tilePane;
    private List<BorrowedBook> borrowBookList = new ArrayList<>();
    private boolean isAnchorPaneVisible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image image = new Image(getClass().getResource("/icons/MemberIcon/more.png").toExternalForm());
        moreIcon.setFill(new ImagePattern(image));
        loadBorrowBookFromDatabase();
        setAvatarAndUserName();
    }

    private void loadBorrowBookFromDatabase() {
        borrowBookList.clear();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query;
            String borrowUser = Session.getInstance().getLoggedInUser().getId();
            PreparedStatement preparedStatement;
            query = "SELECT b.id, b.title, b.author, b.isbn," +
                    " b.book_image, br.due_date, br.status FROM books b " +
                    " JOIN borrows br on b.isbn = br.book_isbn" +
                    " WHERE br.member_id= " + borrowUser;
            preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String isbn = resultSet.getString("isbn");
                String imageBook = resultSet.getString("book_image");
                String statusString = resultSet.getString("status");
                Status status = Status.valueOf(statusString);
                LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();

                if (imageBook == null) {
                    imageBook = "defaultBook.jpg";
                }

                if (status == BORROWING || status == OVERDUE) {
                    Book book = new Book(id, title, author, isbn, imageBook);
                    borrowBookList.add(new BorrowedBook(book, dueDate, status));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        displayBorrowedBooks(borrowBookList);
    }

    private void displayBorrowedBooks(List<BorrowedBook> booksToDisplay) {
        tilePane.getChildren().clear();
        for (BorrowedBook book : booksToDisplay) {
            AnchorPane bookPane = createBookPane(book);
            tilePane.getChildren().add(bookPane);
        }
        tilePane.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double paneWidth = newWidth.doubleValue();
//            adjustBookPaneLayout(paneWidth);
        });

//        adjustBookPaneLayout(tilePane.getWidth());
    }

    private AnchorPane createBookPane(BorrowedBook book) {
        AnchorPane bookPane = new AnchorPane();
        bookPane.setPrefSize(170, 360);

        ImageView bookImageView = new ImageView();
        String projectDir = System.getProperty("user.dir");
        String booksDir = projectDir + "/src/main/resources/images/book/";
        String path = booksDir + book.getImagePath();
        File file = new File(path);
        Image image = new Image(file.toURI().toString());
        DesignUtil.cropToAspectRatio(image, bookImageView, 170, 270);

        Text titleText = new Text(book.getTitle());
        titleText.setStyle("-fx-font-size: 16px;-fx-font-weight: 600;");
        titleText.fontProperty().set(Font.font("Segoe UI"));
        DesignUtil.truncateTextToFit(titleText, 170, 3);
        TextFlow textFlow = new TextFlow(titleText);

        textFlow.setLayoutX(0);
        textFlow.setPrefWidth(170);
        textFlow.setPrefHeight(55);
        textFlow.setTextAlignment(TextAlignment.CENTER);


        textFlow.setLineSpacing(-3);
        Label authorLabel = new Label(book.getAuthor());
        authorLabel.setLayoutY(43);
        authorLabel.setPrefWidth(170);
        authorLabel.setStyle("-fx-font-size: 14px; -fx-underline: true;");
        authorLabel.setAlignment(Pos.CENTER);
        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, book.getDueDate());
        String daysText;
        if (daysRemaining >= 0) {
            daysText = daysRemaining + " day" + (daysRemaining == 1 ? "" : "s") + " left";
        } else {
            daysText = "Overdue by " + Math.abs(daysRemaining) + " day" + (Math.abs(daysRemaining) == 1 ? "" : "s");
        }
        Label daysRemainingLabel = new Label(daysText);

        daysRemainingLabel.setTextFill(javafx.scene.paint.Color.RED);
        daysRemainingLabel.setStyle("-fx-font-size: 14px;");
        daysRemainingLabel.fontProperty().set(Font.font("Segoe UI"));
        daysRemainingLabel.setLayoutY(60);
        daysRemainingLabel.setPrefWidth(170);
        daysRemainingLabel.setAlignment(Pos.CENTER);

        AnchorPane buttonPane = new AnchorPane();
        buttonPane.setPrefSize(170, 45);
        buttonPane.setLayoutY(225);
        buttonPane.setStyle("-fx-background-color: #ffffff;");
        buttonPane.setVisible(false);

        AnchorPane containText = new AnchorPane();
        containText.setPrefSize(170, 90);
        containText.setLayoutY(270);
        containText.setStyle("-fx-background-color: #ffffff;");
        containText.getChildren().addAll(textFlow, daysRemainingLabel, authorLabel);

        Button returnButton = new Button("QUICK RETURN");
        returnButton.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-text-fill: #000000; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand; " +
                        "-fx-border-color: #b20203; " +
                        "-fx-border-width: 2px; " +
                        "-fx-background-radius: 0; " +
                        "-fx-border-radius: 0;"
        );

        returnButton.setLayoutX(10);
        returnButton.setLayoutY(10);
        returnButton.setPrefWidth(150);
        buttonPane.getChildren().add(returnButton);

        // Animation for buttonPane sliding up
        bookPane.setOnMouseEntered(e -> {
            buttonPane.setVisible(true);
            TranslateTransition slideUp = new TranslateTransition(Duration.millis(300), buttonPane);
            slideUp.setFromY(50); // Adjust from outside the pane to its set position
            slideUp.setToY(0);
            slideUp.play();
        });
        bookPane.setOnMouseExited(e -> {
            TranslateTransition slideDown = new TranslateTransition(Duration.millis(300), buttonPane);
            slideDown.setFromY(0);
            slideDown.setToY(40);
            slideDown.setOnFinished(event -> buttonPane.setVisible(false));
            slideDown.play();
        });

        // Add all components to the anchor pane
        bookPane.getChildren().addAll(bookImageView, buttonPane, containText);
        return bookPane;
    }

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
            isAnchorPaneVisible = false;
        }
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
        userNameUser2.setText(Session.getInstance().getLoggedInUser().getName());
    }

    @FXML
    private void openBorrowed() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/Book.fxml"));
            Parent manageUserRoot = loader.load();
            Stage currentStage = (Stage) avatarUser.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageUserRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void logOut() throws IOException {
        Stage currenStage = (Stage) avatarUser.getScene().getWindow();
        Stage stage = new Stage();
        stage.setTitle("Librio");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent loginRoot = loader.load();
        stage.setScene(new Scene(loginRoot));
        stage.show();
        Session.getInstance().logout();
        currenStage.close();
    }


}
