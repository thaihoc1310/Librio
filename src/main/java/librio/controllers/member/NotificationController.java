package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import librio.database.DatabaseConnection;
import librio.enums.Status;
import librio.models.BorrowedBook;
import librio.session.Session;
import librio.util.DesignUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationController {
    final double ITEM_HEIGHT = 145;
    final double BOOK_PANE_WIDTH = 374;
    final double BOOK_PANE_HEIGHT = 138;
    final double IMAGE_WIDTH = 90;
    final double IMAGE_HEIGHT = 120;
    @FXML
    private VBox dueSoonBox;
    @FXML
    private VBox dueBookBox;
    private List<BorrowedBook> overdueBooks = new ArrayList<>();
    private List<BorrowedBook> upcomingDueBooks = new ArrayList<>();

    @FXML
    public void initialize() {
        loadNotificationsFromDatabase();
        int totalBooks = getTotalOverdueAndUpcomingBooks();
        Session.getInstance().setTotalBooks(totalBooks);
    }

    private void loadNotificationsFromDatabase() {
        String userId = Session.getInstance().getLoggedInUser().getId();
        LocalDate today = LocalDate.now();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = """
                        SELECT br.id AS borrow_id, b.title, b.author, br.due_date, br.status, br.fine, b.book_image
                        FROM books b
                        JOIN borrows br ON b.isbn = br.book_isbn
                        WHERE br.member_id = ?
                          AND (br.status = 'BORROWING' OR br.status = 'OVERDUE')
                    """;

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int borrowId = resultSet.getInt("borrow_id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();
                String statusString = resultSet.getString("status");
                Status status = Status.valueOf(statusString);
                Double fine = resultSet.getDouble("fine");
                String imageBook = resultSet.getString("book_image");
                if (imageBook == null) {
                    imageBook = "defaultBook.jpg";
                }
                BorrowedBook book = new BorrowedBook(title, author, dueDate, status, fine, borrowId, imageBook);

                if (dueDate.isBefore(today)) {
                    overdueBooks.add(book);
                } else if ((dueDate.isEqual(today) || dueDate.isAfter(today)) && dueDate.isBefore(today.plusDays(3))) {
                    upcomingDueBooks.add(book);
                }

            }
            displayComingDueBooks(upcomingDueBooks);
            displayDueBooks(overdueBooks);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayDueBooks(List<BorrowedBook> overdueBooks) {
        if (!overdueBooks.isEmpty()) {
            dueBookBox.getChildren().clear();
            for (int i = 0; i < overdueBooks.size(); i++) {
                BorrowedBook book = overdueBooks.get(i);
                Separator separator = new Separator();
                separator.setPrefWidth(BOOK_PANE_WIDTH);
                separator.setLayoutX(3);
                AnchorPane bookPane = new AnchorPane();
                bookPane.setPrefSize(BOOK_PANE_WIDTH, BOOK_PANE_HEIGHT);
                bookPane.setMaxHeight(BOOK_PANE_HEIGHT);
                bookPane.setMinHeight(BOOK_PANE_HEIGHT);
                bookPane.setStyle("-fx-cursor: hand");
                bookPane.getStyleClass().add("book-pane");
                bookPane.setOnMouseClicked(event -> openBorrowedBooksPage(String.valueOf(book.getBorrowId())));

                ImageView bookImageView = new ImageView();
                String projectDir = System.getProperty("user.dir");
                String booksDir = projectDir + "/src/main/resources/images/book/";
                String imagePath = booksDir + book.getImagePath();
                File file = new File(imagePath);

                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    DesignUtil.cropToAspectRatio(image, bookImageView, IMAGE_WIDTH, IMAGE_HEIGHT);
                }

                bookImageView.setLayoutX(10);
                bookImageView.setLayoutY(10);

                Label titleLabel = new Label(book.getTitle());
                titleLabel.setLayoutX(104);
                titleLabel.setLayoutY(6);
                titleLabel.setPrefSize(276, 26);
                titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                titleLabel.setWrapText(true);

                Text authorText = new Text(book.getAuthor());
                authorText.setLayoutX(104);
                authorText.setLayoutY(38);
                authorText.setOpacity(0.59);
                authorText.setWrappingWidth(200);
                authorText.setFont(Font.font(12));

                Label notificationLabel = new Label("This book is due soon. Please return it on time to avoid late fees.");
                notificationLabel.setPrefSize(277, 52);
                notificationLabel.setWrapText(true);
                notificationLabel.setLayoutX(104);
                notificationLabel.setLayoutY(50);

                bookPane.getChildren().addAll(separator, bookImageView, titleLabel, authorText, notificationLabel);
                dueBookBox.getChildren().add(bookPane);
            }

        }
    }

    private void displayComingDueBooks(List<BorrowedBook> upcomingDueBooks) {
        if (!upcomingDueBooks.isEmpty()) {
            dueSoonBox.getChildren().clear();
            for (int i = 0; i < upcomingDueBooks.size(); i++) {
                BorrowedBook book = upcomingDueBooks.get(i);
                Separator separator = new Separator();
                separator.setPrefWidth(BOOK_PANE_WIDTH);
                separator.setLayoutX(3);
                AnchorPane bookPane = new AnchorPane();
                bookPane.setPrefSize(BOOK_PANE_WIDTH, BOOK_PANE_HEIGHT);
                bookPane.setMaxHeight(BOOK_PANE_HEIGHT);
                bookPane.setMinHeight(BOOK_PANE_HEIGHT);
                bookPane.getStyleClass().add("book-pane");
                bookPane.setStyle("-fx-cursor: hand");
                bookPane.setOnMouseClicked(event -> openBorrowedBooksPage(String.valueOf(book.getBorrowId())));

                ImageView bookImageView = new ImageView();
                String projectDir = System.getProperty("user.dir");
                String booksDir = projectDir + "/src/main/resources/images/book/";
                String imagePath = booksDir + book.getImagePath();
                File file = new File(imagePath);

                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    DesignUtil.cropToAspectRatio(image, bookImageView, IMAGE_WIDTH, IMAGE_HEIGHT);
                }

                bookImageView.setLayoutX(10);
                bookImageView.setLayoutY(10);

                Label titleLabel = new Label(book.getTitle());
                titleLabel.setLayoutX(104);
                titleLabel.setLayoutY(6);
                titleLabel.setPrefSize(276, 26);
                titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                titleLabel.setWrapText(true);

                Text authorText = new Text(book.getAuthor());
                authorText.setLayoutX(104);
                authorText.setLayoutY(38);
                authorText.setOpacity(0.59);
                authorText.setWrappingWidth(200);
                authorText.setFont(Font.font(12));

                Label notificationLabel = new Label("This book is due soon. Please return it on time to avoid late fees.");
                notificationLabel.setPrefSize(277, 52);
                notificationLabel.setWrapText(true);
                notificationLabel.setLayoutX(104);
                notificationLabel.setLayoutY(40);

                LocalDate dueDateValue = upcomingDueBooks.get(i).getDueDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String formattedDate = dueDateValue.format(formatter);
                Label dueDate = new Label("Due date: " + formattedDate);
                dueDate.setLayoutX(104);
                dueDate.setLayoutY(100);
                dueDate.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

                bookPane.getChildren().addAll(separator, bookImageView, titleLabel, authorText, notificationLabel, dueDate);
                dueSoonBox.getChildren().add(bookPane);
            }

        }
    }

    public int getTotalOverdueAndUpcomingBooks() {
        return overdueBooks.size() + upcomingDueBooks.size();
    }
    private void openBorrowedBooksPage(String borrowId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/Borrowed.fxml"));
            Parent root = loader.load();

            BorrowedController controller = loader.getController();
            controller.scrollToBook(borrowId);
            Stage stage = (Stage) dueSoonBox.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
