package librio.controllers.member;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import librio.auth.Session;
import librio.database.DatabaseConnection;
import librio.models.Book;
import librio.util.DesignUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static librio.util.DesignUtil.cropAndClipToCircle;

public class BookController implements Initializable {

    @FXML
    private TilePane tilePane;
    @FXML
    private ImageView ClickAvatar;
    @FXML
    private Pane overlayPane;
    @FXML
    private AnchorPane mainAnchorPane;
    @FXML
    private TextField searchTextField;
    @FXML
    private ComboBox<String> filterBox;
    @FXML
    private AnchorPane menuPane;
    @FXML
    private ImageView avatarUser;
    @FXML
    private Label userNameUser;
    @FXML
    private Label userNameUser2;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ImageView searchButton;
    @FXML
    private Circle moreIcon;

    private int offsetIndex = 0;

    private boolean isAnchorPaneVisible = false;

    private List<Book> bookList = new ArrayList<>();

    private String keyword = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image image = new Image(getClass().getResource("/icons/MemberIcon/more.png").toExternalForm());
        moreIcon.setFill(new ImagePattern(image));
        setAvatarAndUserName();
        filterBox.getItems().addAll("Title", "Author", "Category", "Language", "Publisher", "Year published", "ISBN");
        filterBox.getSelectionModel().selectFirst();
        overlayPane.setVisible(false);
//        overlayPane.setOnMouseClicked(event -> cancelBookDetail());
        loadBooksFromDatabase();

    }

    /**
     * Tải danh sách sách từ cơ sở dữ liệu
     */
    private void loadBooksFromDatabase() {
        bookList.clear();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String selectedFilter = filterBox.getValue();
            String query;
            PreparedStatement preparedStatement;

            if (keyword == null || keyword.isEmpty()) {
                query = "SELECT id, title, author, isbn, category, publisher, quantity_copy, average_of_rating, year_published, language, number_of_pages, description, book_image FROM books LIMIT ? OFFSET ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, 20);
                preparedStatement.setInt(2, offsetIndex);
            } else {
                query = "SELECT * FROM books WHERE " + getFilter(selectedFilter) + " LIKE ? LIMIT ? OFFSET ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, "%" + keyword + "%");
                preparedStatement.setInt(2, 20);
                preparedStatement.setInt(3, offsetIndex);
            }

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

                bookList.add(book);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        displayBooks(bookList);
    }

    private String getFilter(String filter) {
        switch (filter) {
            case "Author":
                return "author";
            case "Category":
                return "category";
            case "Language":
                return "language";
            case "Publisher":
                return "publisher";
            case "Year published":
                return "year_published";
            case "ISBN":
                return "isbn";
            case "Title":
            default:
                return "title";
        }
    }

    @FXML
    private void handleSearch() {
        bookList.clear();
        offsetIndex = 0;
        keyword = searchTextField.getText().trim();
        loadBooksFromDatabase();
    }

    /**
     * Hiển thị danh sách các cuốn sách trong TilePane
     */
    private void displayBooks(List<Book> booksToDisplay) {
        if (offsetIndex == 0) {
            tilePane.getChildren().clear();
        } else {
            if (!tilePane.getChildren().isEmpty() && tilePane.getChildren().get(tilePane.getChildren().size() - 1) instanceof Button) {
                tilePane.getChildren().remove(tilePane.getChildren().size() - 1);
            }
        }
        for (Book book : booksToDisplay) {
            AnchorPane bookPane = createBookPane(book);
            tilePane.getChildren().add(bookPane);
        }
        if (!booksToDisplay.isEmpty() && booksToDisplay.size() % 4 == 0) {
            Button moreButton = new Button("More");
            moreButton.setPrefHeight(32.0);
            moreButton.setPrefWidth(667.0);
            moreButton.setStyle("-fx-background-color: #72311c; -fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-background-radius: 5px; -fx-font-size: 15px;");
            moreButton.setOnMouseEntered(event -> moreButton.setStyle("-fx-background-color: #4c2113; -fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-background-radius: 5px; -fx-font-size: 15px; -fx-cursor: hand;"));
            moreButton.setOnMouseExited(event -> moreButton.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-background-radius: 5px; -fx-font-size: 15px;-fx-background-color: #72311c;"));
            moreButton.setOnAction(event -> {
                offsetIndex += 20;
                loadBooksFromDatabase();
            });
            tilePane.getChildren().add(moreButton);
        }
        tilePane.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double paneWidth = newWidth.doubleValue();
            adjustBookPaneLayout(paneWidth);
        });

        adjustBookPaneLayout(tilePane.getWidth());
    }


    private void adjustBookPaneLayout(double tilePaneWidth) {
        if (tilePaneWidth <= 0) return;

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


        ImageView bookCover = new ImageView();
        bookCover.setX(29);
        bookCover.setY(1);

        String projectDir = System.getProperty("user.dir");
        String booksDir = projectDir + "/src/main/resources/images/book/";
        String path = booksDir + book.getImagePath();
        File file = new File(path);
        Image image = new Image(file.toURI().toString());
        DesignUtil.cropToAspectRatio(image,bookCover,215,314);

        bookCover.setPickOnBounds(true);
        bookCover.setPreserveRatio(true);
        bookPane.getChildren().add(bookCover);

        TextFlow bookInfo = new TextFlow();
        Label titleLabel = new Label(book.getTitle());

        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(215);
        titleLabel.setStyle("-fx-font-weight: 200;");
        titleLabel.setMaxHeight(48);

        bookInfo.getChildren().addAll(titleLabel);
        bookInfo.setMaxWidth(Double.MAX_VALUE);

        AnchorPane.setLeftAnchor(bookInfo, 29.0);
        AnchorPane.setRightAnchor(bookInfo, 29.0);
        AnchorPane.setTopAnchor(bookInfo, 320.0);
        bookPane.getChildren().add(bookInfo);


        HBox starBox = new HBox(5);

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
                Rectangle clip = new Rectangle(15 * decimalPart, 15); // Cắt theo phần thập phân
                fullStar.setClip(clip);
            } else {
                starPane.getChildren().add(emptyStar);
            }
            starBox.getChildren().add(starPane);
        }

        AnchorPane.setTopAnchor(starBox, 370.0);
        AnchorPane.setLeftAnchor(starBox, 29.0);
        AnchorPane.setRightAnchor(starBox, 29.0);
        bookPane.getChildren().add(starBox);
        bookPane.setOnMouseClicked(event -> openBookDetailScene(book));

        return bookPane;
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

    private void openBookDetailScene(Book book){
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/BookDetail.fxml"));
            Parent root = loader.load();
            BookDetailController bookDetailController = loader.getController();
            bookDetailController.setBook(book);
            // Tạo stage mới cho scene
            Stage stage = new Stage();
            stage.setTitle("Librio");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initOwner(searchTextField.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            // Hiển thị scene
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
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


    @FXML
    void logOut() throws IOException {
        Stage currenStage = (Stage) searchTextField.getScene().getWindow();
        Stage stage = new Stage();
        stage.setTitle("Librio");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent loginRoot  = loader.load();
        stage.setScene(new Scene(loginRoot));
        stage.show();
        Session.getInstance().logout();
        currenStage.close();
    }
    @FXML
    private void openBorrowed() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/Borrowed.fxml"));
            Parent manageUserRoot  = loader.load();
            Stage currentStage = (Stage) avatarUser.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageUserRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openEditProfileScene(){
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/AccountSetting.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Edit Profile");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initOwner(searchTextField.getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            // Hiển thị scene
            stage.showAndWait();
            setAvatarAndUserName();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
