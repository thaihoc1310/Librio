package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
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
import java.time.format.DateTimeFormatter;
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
    private AnchorPane confirmPane;
    @FXML
    private Button returnBookButton;
    @FXML
    private VBox bookBorrowVBox;
    @FXML
    private TilePane tilePane;
    private List<BorrowedBook> borrowBookList = new ArrayList<>();
    private List<BorrowedBook> returnedBookList = new ArrayList<>();
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
            String borrowUserId = Session.getInstance().getLoggedInUser().getId();
            PreparedStatement preparedStatement;
            query = "SELECT b.id, b.title, b.author, b.isbn," +
                    " b.book_image,br.borrow_date, br.due_date, br.return_date, br.status, br.fine FROM books b " +
                    " JOIN borrows br on b.isbn = br.book_isbn" +
                    " WHERE br.member_id= " + borrowUserId;
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
                Double fine = resultSet.getDouble("fine");
                LocalDate borrowDate = resultSet.getDate("borrow_date").toLocalDate();
                LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();
                LocalDate returnDate = null;
                if (resultSet.getDate("return_date") != null) {
                    returnDate = resultSet.getDate("return_date").toLocalDate();
                }

                if (imageBook == null) {
                    imageBook = "defaultBook.jpg";
                }

                if (status == BORROWING || status == OVERDUE) {
                    Book book = new Book(id, title, author, isbn, imageBook);
                    borrowBookList.add(new BorrowedBook(book, borrowDate, dueDate, null,  status, fine));
                } else {
                    Book returnedBook = new Book(id, title, author, isbn, imageBook);
                    returnedBookList.add(new BorrowedBook(returnedBook, borrowDate, dueDate, returnDate, status, fine));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        displayBorrowingBooks(borrowBookList);
        displayReturnedBooks(returnedBookList);
    }

    private void displayBorrowingBooks(List<BorrowedBook> booksToDisplay) {
        bookBorrowVBox.getChildren().clear();

        for (BorrowedBook book : booksToDisplay) {
            AnchorPane anchorPane = new AnchorPane();
            anchorPane.setMinHeight(405);
            anchorPane.setPrefHeight(405);
            anchorPane.setMaxHeight(405);
            anchorPane.setPrefWidth(1240);
            anchorPane.getStyleClass().add("whitePane");

            ImageView bookImageView = new ImageView();
            bookImageView.getStyleClass().add("avatar-view");
            String projectDir = System.getProperty("user.dir");
            String booksDir = projectDir + "/src/main/resources/images/book/";
            String path = booksDir + book.getImagePath();
            File file = new File(path);
            Image image = new Image(file.toURI().toString());
            DesignUtil.cropToAspectRatio(image, bookImageView, 218, 325);
            bookImageView.setLayoutX(38);
            bookImageView.setLayoutY(38);

            Label titleLabel = new Label(book.getTitle());
            titleLabel.setLayoutX(321);
            titleLabel.setLayoutY(30);
            titleLabel.setPrefWidth(800);
            titleLabel.setPrefHeight(10);
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
            titleLabel.setWrapText(true);


            Text authorText = new Text(book.getAuthor());
            authorText.setLayoutX(321);
            authorText.setLayoutY(78);
            authorText.setOpacity(0.59);
            authorText.setWrappingWidth(333);
            authorText.setFont(Font.font(18));

            Label isbnLabel = new Label("ISBN: " + book.getIsbn());
            isbnLabel.setLayoutX(321);
            isbnLabel.setLayoutY(87);
            isbnLabel.setFont(Font.font("System", FontPosture.ITALIC, 12));

            Separator separator = new Separator();
            separator.setLayoutX(313);
            separator.setLayoutY(126);
            separator.setPrefWidth(801);

            Button returnButton = new Button("Return");
            returnButton.setLayoutX(321);
            returnButton.setLayoutY(323);
            returnButton.setPrefSize(200, 31);
            returnButton.getStyleClass().add("button-borrow");
            returnButton.setOnAction(event -> {
                confirmPane.toFront();
            });
            returnBookButton.setOnAction(event -> {
                returnBook(book);
                confirmPane.toBack();
            });

            GridPane gridPane = new GridPane();
            gridPane.setLayoutX(321);
            gridPane.setLayoutY(151);
            gridPane.setPrefWidth(230);

            ColumnConstraints col1 = new ColumnConstraints();
            col1.setHgrow(Priority.SOMETIMES);
            col1.setPrefWidth(100);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setHgrow(Priority.SOMETIMES);
            col2.setPrefWidth(100);

            gridPane.getColumnConstraints().addAll(col1, col2);

            for (int i = 0; i < 4; i++) {
                RowConstraints row = new RowConstraints();
                row.setVgrow(Priority.SOMETIMES);
                row.setPrefHeight(30);
                gridPane.getRowConstraints().add(row);
            }

            Label borrowDateLabel = new Label("Borrow date:");
            borrowDateLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            gridPane.add(borrowDateLabel, 0, 0);

            LocalDate borrowDateTime = book.getBorrowDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String borrowDateText = borrowDateTime.format(formatter);
            Label borrowDate = new Label("   " + borrowDateText);
            borrowDate.setFont(Font.font("System", 16));
            gridPane.add(borrowDate, 1, 0);

            Label dueDateLabel = new Label("Due date:");
            dueDateLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            gridPane.add(dueDateLabel, 0, 1);

            LocalDate dueDateTime = book.getDueDate();
            String dueDateText = dueDateTime.format(formatter);
            Label dueDate = new Label("   " + dueDateText);
            dueDate.setFont(Font.font("System", 16));
            gridPane.add(dueDate, 1, 1);

            Label statusLabel = new Label("Status:");
            statusLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            gridPane.add(statusLabel, 0, 2);

            String statusString = book.getStatus().toString();
            Label status = new Label("   " + statusString);
            status.setFont(Font.font("System", 16));
            gridPane.add(status, 1, 2);

            Label fineLabel = new Label("Fine:");
            fineLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            gridPane.add(fineLabel, 0, 3);

            String fineString = book.getFine().toString();
            Label fine = new Label("   " + fineString + " VND");
            fine.setFont(Font.font("System", 16));
            gridPane.add(fine, 1, 3);

            LocalDate today = LocalDate.now();
            long daysRemaining = ChronoUnit.DAYS.between(today, book.getDueDate());
            String daysText;
            if (daysRemaining >= 0) {
                daysText = daysRemaining + " day" + (daysRemaining == 1 ? "" : "s") + " left";
            } else {
                daysText = "Overdue by " + Math.abs(daysRemaining) + " day" + (Math.abs(daysRemaining) == 1 ? "" : "s");
            }
            Label daysRemainingLabel = new Label(daysText);
            daysRemainingLabel.setFont(Font.font("System", 16));
            daysRemainingLabel.setTextFill(javafx.scene.paint.Color.RED);


            daysRemainingLabel.setLayoutX(321);
            daysRemainingLabel.setLayoutY(285);

            anchorPane.getChildren().addAll(bookImageView, titleLabel, authorText, isbnLabel, separator, returnButton, gridPane, daysRemainingLabel);

            bookBorrowVBox.getChildren().add(anchorPane);

        }

    }

    private void displayReturnedBooks(List<BorrowedBook> booksToDisplay) {
        tilePane.getChildren().clear();
        for (BorrowedBook book : booksToDisplay) {
            AnchorPane anchorPane = new AnchorPane();
            anchorPane.setMinHeight(278);
            anchorPane.setPrefHeight(278);
            anchorPane.setMaxHeight(278);
            anchorPane.setPrefWidth(634);
            anchorPane.getStyleClass().add("whitePane");

            ImageView bookImageView = new ImageView();
            bookImageView.getStyleClass().add("avatar-view");
            String projectDir = System.getProperty("user.dir");
            String booksDir = projectDir + "/src/main/resources/images/book/";
            String path = booksDir + book.getImagePath();
            File file = new File(path);
            Image image = new Image(file.toURI().toString());
            DesignUtil.cropToAspectRatio(image, bookImageView, 134, 208);
            bookImageView.setLayoutX(28);
            bookImageView.setLayoutY(20);

            Label titleLabel = new Label(book.getTitle());
            titleLabel.setLayoutX(179);
            titleLabel.setLayoutY(19);
            titleLabel.setPrefWidth(450);
            titleLabel.setPrefHeight(10);
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
            titleLabel.setWrapText(true);


            Text authorText = new Text(book.getAuthor());
            authorText.setLayoutX(179);
            authorText.setLayoutY(59);
            authorText.setOpacity(0.59);
            authorText.setWrappingWidth(300);
            authorText.setFont(Font.font(14));

            Label isbnLabel = new Label("ISBN: " + book.getIsbn());
            isbnLabel.setLayoutX(179);
            isbnLabel.setLayoutY(64);
            isbnLabel.setFont(Font.font("System", FontPosture.ITALIC, 11));

            Separator separator = new Separator();
            separator.setLayoutX(177);
            separator.setLayoutY(95);
            separator.setPrefWidth(403);

            GridPane gridPane = new GridPane();
            gridPane.setLayoutX(179);
            gridPane.setLayoutY(114);
            gridPane.setPrefWidth(250);

            ColumnConstraints col1 = new ColumnConstraints();
            col1.setHgrow(Priority.SOMETIMES);
            col1.setPrefWidth(100);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setHgrow(Priority.SOMETIMES);
            col2.setPrefWidth(150);

            gridPane.getColumnConstraints().addAll(col1, col2);

            for (int i = 0; i < 5; i++) {
                RowConstraints row = new RowConstraints();
                row.setVgrow(Priority.SOMETIMES);
                row.setPrefHeight(30);
                gridPane.getRowConstraints().add(row);
            }

            Label borrowDateLabel = new Label("Borrow date:");
            borrowDateLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            gridPane.add(borrowDateLabel, 0, 0);

            LocalDate borrowDateTime = book.getBorrowDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String borrowDateText = borrowDateTime.format(formatter);
            Label borrowDate = new Label("   " + borrowDateText);
            borrowDate.setFont(Font.font("System", 16));
            gridPane.add(borrowDate, 1, 0);

            Label dueDateLabel = new Label("Due date:");
            dueDateLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            gridPane.add(dueDateLabel, 0, 1);

            LocalDate dueDateTime = book.getDueDate();
            String dueDateText = dueDateTime.format(formatter);
            Label dueDate = new Label("   " + dueDateText);
            dueDate.setFont(Font.font("System", 16));
            gridPane.add(dueDate, 1, 1);

            Label returnDateLabel = new Label("Return date:");
            returnDateLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            gridPane.add(returnDateLabel, 0, 2);

            LocalDate returnDateTime = book.getReturnDate();
            String returnDateText = returnDateTime != null ? returnDateTime.format(formatter) : "N/A";
            Label returnDate = new Label("   " + returnDateText);
            returnDate.setFont(Font.font("System", 16));
            gridPane.add(returnDate, 1, 2);

            Label statusLabel = new Label("Status:");
            statusLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            gridPane.add(statusLabel, 0, 3);

            String statusString = book.getStatus().toString();
            Label status = new Label("   " + statusString);
            status.setFont(Font.font("System", 16));
            gridPane.add(status, 1, 3);

            Label fineLabel = new Label("Fine:");
            fineLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            gridPane.add(fineLabel, 0, 4);

            String fineString = book.getFine().toString();
            Label fine = new Label("   " + fineString + " VND");
            fine.setFont(Font.font("System", 16));
            gridPane.add(fine, 1, 4);

            anchorPane.getChildren().addAll(bookImageView, titleLabel, authorText, isbnLabel, separator, gridPane);

            tilePane.getChildren().add(anchorPane);
        }
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
    private void cancel() {
        confirmPane.toBack();
    }

    @FXML
    private void openHomepage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/Homepage.fxml"));
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

    @FXML
    private void returnBook(BorrowedBook borrowedBook) {
        LocalDate dueDate = borrowedBook.getDueDate();
        LocalDate today = LocalDate.now();
        Status newStatus = (today.isAfter(dueDate)) ? Status.RETURNED_LATE : Status.RETURNED;

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "UPDATE borrows SET status = ?, return_date = ? WHERE book_isbn = ? AND member_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newStatus.toString());
            preparedStatement.setString(2, today.toString());
            preparedStatement.setString(3, borrowedBook.getIsbn());
            preparedStatement.setString(4, Session.getInstance().getLoggedInUser().getId());


            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                borrowBookList.remove(borrowedBook);
                borrowedBook.setStatus(newStatus);
                borrowedBook.setReturnDate(today);
                returnedBookList.add(borrowedBook);

                displayBorrowingBooks(borrowBookList);
                displayReturnedBooks(returnedBookList);
            } else {
                System.out.println("Trả sách thất bại!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
