package librio.controllers.member;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import librio.cache.ImageCache;
import librio.database.DatabaseConnection;
import librio.enums.Status;
import librio.models.Book;
import librio.models.BorrowedBook;
import librio.session.Session;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static librio.enums.Status.*;
import static librio.util.DatabaseUtil.checkIfUserRatedBook;
import static librio.util.DatabaseUtil.updateQuantityBook;
import static librio.util.DesignUtil.cropAndClipToCircle;
import static librio.util.DesignUtil.cropToAspectRatio;

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
    private VBox bookBorrowVBox;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private TilePane tilePane;
    @FXML
    private ScrollPane scrollBorrowPane;
    @FXML
    private TextField searchTextField;
    private List<BorrowedBook> borrowBookList = new ArrayList<>();
    private List<BorrowedBook> returnedBookList = new ArrayList<>();
    private boolean isAnchorPaneVisible = false;
    private BorrowedBook selectedBook;

    private ExecutorService executor;

    public static void setRatingButton(Button ratingButton, BorrowedBook book) {
        boolean isAlreadyRated = checkIfUserRatedBook(Session.getInstance().getLoggedInUser(), book);
        ratingButton.setUserData(book.getId());
        if (isAlreadyRated) {
            updateRatingButton(ratingButton, "RATED", "#A0A0A0", false);
        } else {
            updateRatingButton(ratingButton, "RATING", "#FFA500", true);
        }
    }

    public static void updateRatingButton(Button button, String text, String color, boolean isEnabled) {
        button.setText(text);
        button.setStyle("-fx-border-color: " + color + "; -fx-text-fill: " + color);
        button.setDisable(!isEnabled);
        button.setCursor(isEnabled ? Cursor.HAND : Cursor.DEFAULT);
        button.setOnMouseEntered(e -> button.setStyle("-fx-text-fill: rgba(255, 165, 0, 0.7);"));
        button.setOnMouseExited(e -> button.setStyle("-fx-text-fill: #FFA500;"));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image image = new Image(getClass().getResource("/icons/MemberIcon/more.png").toExternalForm());
        moreIcon.setFill(new ImagePattern(image));
        loadBorrowBookFromDatabase("");
        loadReturnedBookFromDatabase("");
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                searchTextField.clear();
            }
        });
        setAvatarAndUserName();
        executor = Executors.newCachedThreadPool();
    }

    private void loadBorrowBookFromDatabase(String keyword) {
        borrowBookList.clear();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query;
            String borrowUserId = Session.getInstance().getLoggedInUser().getId();
            PreparedStatement preparedStatement;
            if (keyword.isEmpty()) {
                query = "SELECT br.id AS borrow_id, b.id AS book_id, b.title, b.author, b.isbn, " +
                        "b.book_image, br.borrow_date, br.due_date, br.return_date, br.status, br.fine " +
                        "FROM books b " +
                        "JOIN borrows br ON b.isbn = br.book_isbn " +
                        "WHERE br.member_id = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, borrowUserId);
            } else {
                query = "SELECT br.id AS borrow_id, b.id AS book_id, b.title, b.author, b.isbn, " +
                        "b.book_image, br.borrow_date, br.due_date, br.return_date, br.status, br.fine " +
                        "FROM books b " +
                        "JOIN borrows br ON b.isbn = br.book_isbn " +
                        "WHERE br.member_id = ? AND title LIKE ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, borrowUserId);
                preparedStatement.setString(2, "%" + keyword + "%");
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int borrowId = resultSet.getInt("borrow_id"); // Lấy borrowId
                int bookId = resultSet.getInt("book_id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String isbn = resultSet.getString("isbn");
                String imageBook = resultSet.getString("book_image");
                String statusString = resultSet.getString("status");
                Status status = Status.valueOf(statusString);
                Double fine = resultSet.getDouble("fine");
                LocalDate borrowDate = resultSet.getDate("borrow_date").toLocalDate();
                LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();
                LocalDate returnDate = resultSet.getDate("return_date") != null
                        ? resultSet.getDate("return_date").toLocalDate()
                        : null;

                if (imageBook == null) {
                    imageBook = "defaultBook.jpg";
                }

                Book book = new Book(bookId, title, author, isbn, imageBook);
                BorrowedBook borrowedBook = new BorrowedBook(book, borrowDate, dueDate, returnDate, status, fine, borrowId);

                if (status == BORROWING || status == OVERDUE) {
                    borrowBookList.add(borrowedBook);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        displayBorrowingBooks(borrowBookList);
    }

    private void loadReturnedBookFromDatabase(String keyword) {
        returnedBookList.clear();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query;
            String borrowUserId = Session.getInstance().getLoggedInUser().getId();
            PreparedStatement preparedStatement;
            if (keyword.isEmpty()) {
                query = "SELECT br.id AS borrow_id, b.id AS book_id, b.title, b.author, b.isbn, " +
                        "b.book_image, br.borrow_date, br.due_date, br.return_date, br.status, br.fine " +
                        "FROM books b " +
                        "JOIN borrows br ON b.isbn = br.book_isbn " +
                        "WHERE br.member_id = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, borrowUserId);
            } else {
                query = "SELECT br.id AS borrow_id, b.id AS book_id, b.title, b.author, b.isbn, " +
                        "b.book_image, br.borrow_date, br.due_date, br.return_date, br.status, br.fine " +
                        "FROM books b " +
                        "JOIN borrows br ON b.isbn = br.book_isbn " +
                        "WHERE br.member_id = ? AND title LIKE ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, borrowUserId);
                preparedStatement.setString(2, "%" + keyword + "%");
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int borrowId = resultSet.getInt("borrow_id"); // Lấy borrowId
                int bookId = resultSet.getInt("book_id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String isbn = resultSet.getString("isbn");
                String imageBook = resultSet.getString("book_image");
                String statusString = resultSet.getString("status");
                Status status = Status.valueOf(statusString);
                Double fine = resultSet.getDouble("fine");
                LocalDate borrowDate = resultSet.getDate("borrow_date").toLocalDate();
                LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();
                LocalDate returnDate = resultSet.getDate("return_date") != null
                        ? resultSet.getDate("return_date").toLocalDate()
                        : null;

                if (imageBook == null) {
                    imageBook = "defaultBook.jpg";
                }

                Book book = new Book(bookId, title, author, isbn, imageBook);
                BorrowedBook borrowedBook = new BorrowedBook(book, borrowDate, dueDate, returnDate, status, fine, borrowId);

                if (status == RETURNED || status == RETURNED_LATE) {
                    returnedBookList.add(borrowedBook);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        displayReturnedBooks(returnedBookList);
    }

    private void displayBorrowingBooks(List<BorrowedBook> booksToDisplay) {

        if (!booksToDisplay.isEmpty()) {

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
                cropToAspectRatio(image, bookImageView, 218, 325);
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
                    selectedBook = book;
                    confirmPane.toFront();
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
                Label borrowIdLabel = new Label(String.valueOf(book.getBorrowId()));
                borrowIdLabel.setId("borrowIdLabel");
                borrowIdLabel.setVisible(false);
                anchorPane.getChildren().add(borrowIdLabel);
                anchorPane.getChildren().addAll(bookImageView, titleLabel, authorText, isbnLabel, separator, returnButton, gridPane, daysRemainingLabel);

                bookBorrowVBox.getChildren().add(anchorPane);

            }
        }

    }

    private void displayReturnedBooks(List<BorrowedBook> booksToDisplay) {
        if (!booksToDisplay.isEmpty()) {
            tilePane.getChildren().clear();
            for (BorrowedBook borrowedBook : booksToDisplay) {
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
                String path = booksDir + borrowedBook.getImagePath();

                Image image = ImageCache.getInstance().getImage(path, bookImageView + "defaultBook.jpg");
                cropToAspectRatio(image, bookImageView, 134, 208);

                bookImageView.setLayoutX(28);
                bookImageView.setLayoutY(20);

                Label titleLabel = new Label(borrowedBook.getTitle());
                titleLabel.setLayoutX(179);
                titleLabel.setLayoutY(19);
                titleLabel.setPrefWidth(450);
                titleLabel.setPrefHeight(10);
                titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
                titleLabel.setWrapText(true);


                Text authorText = new Text(borrowedBook.getAuthor());
                authorText.setLayoutX(179);
                authorText.setLayoutY(59);
                authorText.setOpacity(0.59);
                authorText.setWrappingWidth(300);
                authorText.setFont(Font.font(14));

                Label isbnLabel = new Label("ISBN: " + borrowedBook.getIsbn());
                isbnLabel.setLayoutX(179);
                isbnLabel.setLayoutY(64);
                isbnLabel.setFont(Font.font("System", FontPosture.ITALIC, 11));

                Separator separator = new Separator();
                separator.setLayoutX(177);
                separator.setLayoutY(95);
                separator.setPrefWidth(403);

                GridPane gridPane = new GridPane();
                gridPane.setLayoutX(179);
                gridPane.setLayoutY(100);
                gridPane.setPrefHeight(136);
                gridPane.setPrefWidth(414);

// Cấu hình các cột
                ColumnConstraints col1 = new ColumnConstraints();
                col1.setHgrow(Priority.SOMETIMES);
                col1.setPrefWidth(100);

                ColumnConstraints col2 = new ColumnConstraints();
                col2.setHgrow(Priority.SOMETIMES);
                col2.setPrefWidth(123.33);
                col2.setMaxWidth(128.67);

                ColumnConstraints col3 = new ColumnConstraints();
                col3.setHgrow(Priority.SOMETIMES);
                col3.setPrefWidth(51.33);
                col3.setMaxWidth(98.33);

                ColumnConstraints col4 = new ColumnConstraints();
                col4.setHgrow(Priority.SOMETIMES);
                col4.setPrefWidth(139.33);
                col4.setMaxWidth(145.67);

                gridPane.getColumnConstraints().addAll(col1, col2, col3, col4);

// Cấu hình các hàng
                for (int i = 0; i < 3; i++) {
                    RowConstraints row = new RowConstraints();
                    row.setVgrow(Priority.SOMETIMES);
                    row.setPrefHeight(47);
                    row.setMaxHeight(47);
                    gridPane.getRowConstraints().add(row);
                }

// Thêm các Label và dữ liệu vào GridPane
                Label borrowDateLabel = new Label("Borrow date:");
                borrowDateLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                gridPane.add(borrowDateLabel, 0, 0);

                LocalDate borrowDateTime = borrowedBook.getBorrowDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                String borrowDateText = borrowDateTime.format(formatter);
                Label borrowDate = new Label("   " + borrowDateText);
                borrowDate.setFont(Font.font("System", 16));
                gridPane.add(borrowDate, 1, 0);

                Label dueDateLabel = new Label("Due date:");
                dueDateLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                gridPane.add(dueDateLabel, 0, 1);

                LocalDate dueDateTime = borrowedBook.getDueDate();
                String dueDateText = dueDateTime.format(formatter);
                Label dueDate = new Label("   " + dueDateText);
                dueDate.setFont(Font.font("System", 16));
                gridPane.add(dueDate, 1, 1);

                Label returnDateLabel = new Label("Return date:");
                returnDateLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                gridPane.add(returnDateLabel, 0, 2);

                LocalDate returnDateTime = borrowedBook.getReturnDate();
                String returnDateText = returnDateTime != null ? returnDateTime.format(formatter) : "N/A";
                Label returnDate = new Label("   " + returnDateText);
                returnDate.setFont(Font.font("System", 16));
                gridPane.add(returnDate, 1, 2);

                Label statusLabel = new Label("Status:");
                statusLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                gridPane.add(statusLabel, 2, 0);

                String statusString = borrowedBook.getStatus().toString();
                Label status = new Label("   " + statusString);
                status.setFont(Font.font("System", 16));
                gridPane.add(status, 3, 0);

                Label fineLabel = new Label("Fine:");
                fineLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                gridPane.add(fineLabel, 2, 1);

                String fineString = borrowedBook.getFine().toString();
                Label fine = new Label("   " + fineString + " VND");
                fine.setFont(Font.font("System", 16));
                gridPane.add(fine, 3, 1);


                Button rateButton = new Button();
                rateButton.setLayoutX(500);
                rateButton.setLayoutY(210);
                rateButton.setPrefSize(100, 40);
                rateButton.getStyleClass().add("rate-button");
                setRatingButton(rateButton, borrowedBook);


                rateButton.setOnAction(e -> openRating(borrowedBook, rateButton));

                anchorPane.getChildren().addAll(bookImageView, titleLabel, authorText, isbnLabel, separator, gridPane, rateButton);

                tilePane.getChildren().add(anchorPane);
            }
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


        Image image = ImageCache.getInstance().getImage(path, avatarsDir + "Male User.png");
        cropAndClipToCircle(image, avatarUser, 23);
        cropAndClipToCircle(image, ClickAvatar, 23);
        userNameUser.setText(Session.getInstance().getLoggedInUser().getName());
        userNameUser2.setText(Session.getInstance().getLoggedInUser().getName());
    }

    @FXML
    private void cancel() {
        confirmPane.toBack();
    }

    @FXML
    private void openHomepage() {
        loadingIndicator.setVisible(true);
        Task<Parent> loadHomePageTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                return new FXMLLoader(getClass().getResource("/fxml/member/HomePage.fxml")).load();
            }

            @Override
            protected void succeeded() {
                Parent homepageRoot = getValue();
                Stage currentStage = (Stage) tabPane.getScene().getWindow();
                Scene currentScene = currentStage.getScene();
                currentScene.setRoot(homepageRoot);
                loadingIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                loadingIndicator.setVisible(false);
                getException().printStackTrace();
            }
        };

        executor.submit(loadHomePageTask);
    }

    @FXML
    void logOut() throws IOException {
        Stage currenStage = (Stage) avatarUser.getScene().getWindow();
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent loginRoot = loader.load();
        stage.setScene(new Scene(loginRoot));
        stage.show();
        Session.getInstance().logout();
        ImageCache.getInstance().clearCache();
        currenStage.close();
    }

    @FXML
    private void returnBook(BorrowedBook borrowedBook) {
        LocalDate dueDate = borrowedBook.getDueDate();
        LocalDate today = LocalDate.now();
        Status newStatus = (today.isAfter(dueDate)) ? Status.RETURNED_LATE : RETURNED;

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "UPDATE borrows SET status = ?, return_date = ? WHERE book_isbn = ? AND member_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newStatus.toString());
            preparedStatement.setString(2, today.toString());
            preparedStatement.setString(3, borrowedBook.getIsbn());
            preparedStatement.setString(4, Session.getInstance().getLoggedInUser().getId());


            int rowsAffected = preparedStatement.executeUpdate();

            updateQuantityBook(borrowedBook.getId());
            borrowBookList.remove(borrowedBook);
            borrowedBook.setStatus(newStatus);
            borrowedBook.setReturnDate(today);
            returnedBookList.add(borrowedBook);
            displayBorrowingBooks(borrowBookList);
            displayReturnedBooks(returnedBookList);

            System.out.println("Trả sách thất bại!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openRating(BorrowedBook borrowedBook, Button rateButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/RatingPage.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) tabPane.getScene().getWindow();
            RatingPageController ratingPageController = loader.getController();
            ratingPageController.setBookAndBorrowId(borrowedBook, borrowedBook.getBorrowId());
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
                setRatingButton(rateButton, borrowedBook);
            });
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void confirmReturnAction() {
        if (selectedBook != null) {
            returnBook(selectedBook);
            confirmPane.toBack();
        }
    }

    @FXML
    private void cancelReturnAction() {
        confirmPane.toBack();
        selectedBook = null;
    }

    public void scrollToBook(String borrowId) {
        Platform.runLater(() -> {
            for (Node node : bookBorrowVBox.getChildren()) {
                if (node instanceof AnchorPane) {
                    AnchorPane pane = (AnchorPane) node;
                    Label borrowIdLabel = (Label) pane.lookup("#borrowIdLabel");
                    if (borrowIdLabel != null && borrowIdLabel.getText().equals(borrowId)) {
                        Bounds paneBounds = pane.localToParent(pane.getBoundsInLocal());
                        Bounds vboxBounds = bookBorrowVBox.getBoundsInParent();
                        double contentHeight = bookBorrowVBox.getHeight();
                        double viewportHeight = scrollBorrowPane.getViewportBounds().getHeight();

                        double position = (paneBounds.getMinY() - vboxBounds.getMinY()) / (contentHeight - viewportHeight);
                        position = Math.max(0, Math.min(1, position)); // Đảm bảo giá trị trong khoảng [0, 1]

                        System.out.println("Calculated Position: " + position);
                        System.out.println("ScrollPane Vmax: " + scrollBorrowPane.getVmax());

                        scrollBorrowPane.setVvalue(position);
                        pane.setStyle(" -fx-effect: dropshadow(gaussian, rgba(148, 63, 32, 0.5), 28, 0.5, 0, 0);");
                        break;
                    }
                }
            }
        });
    }

    @FXML
    private void openEditProfileScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/AccountSetting.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) tabPane.getScene().getWindow();
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(tabPane.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);

            });
            stage.showAndWait();
            setAvatarAndUserName();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchTextField.getText().trim();
        int index = tabPane.getSelectionModel().getSelectedIndex();
        Label label = new Label("Not Found");
        label.setPrefWidth(1260);
        label.setStyle("-fx-font-size:36;" +
                "-fx-font-weight: bold;");
        label.setPadding(new Insets(100, 0, 0, 0));
        label.setOpacity(0.41);
        label.setAlignment(Pos.CENTER);
        if (index == 0) {
            bookBorrowVBox.getChildren().clear();
            bookBorrowVBox.getChildren().add(label);
            loadBorrowBookFromDatabase(keyword);
        }
        if (index == 1) {
            tilePane.getChildren().clear();
            tilePane.getChildren().add(label);
            loadReturnedBookFromDatabase(keyword);
        }
    }
}
