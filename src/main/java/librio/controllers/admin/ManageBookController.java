package librio.controllers.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import librio.controllers.auth.LogoutController;
import librio.session.Session;
import librio.database.DatabaseConnection;
import librio.models.Book;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.*;
import static librio.util.DesignUtil.cropAndClipToCircle;

/**
 * The `ManageBookController` class is responsible for managing the book-related
 * functionalities within the application. It handles the initialization
 * and configuration of the book table view, pagination, and search functionalities.
 */
public class ManageBookController implements Initializable {
    @FXML
    private TableView<Book> bookTableView;
    @FXML
    private TableColumn<Book, String> idColumn;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, String> categoryColumn;
    @FXML
    private TableColumn<Book, String> isbnColumn;
    @FXML
    private TableColumn<Book, String> averageRatingColumn;
    @FXML
    private TableColumn<Book, Void> actionColumn;
    @FXML
    private Button addBookButton;
    @FXML
    private StackPane stackPaneRoot;
    @FXML
    private Button addBookApiButton;
    @FXML
    private Pagination pagination;
    @FXML
    private TextField searchTextField;
    @FXML
    private ImageView avatarUser;
    @FXML
    private Label userNameUser;

    private ObservableList<Book> bookList;

    private int currentPage = 0;
    private final int rowsPerPage = 11;

    private String keyword = null;

    public String getKeyword(){
        return keyword;
    }

    public int getCurrentPage(){
        return currentPage;
    }

    /**
     * Initializes the controller class. This method is automatically called after the fxml file
     * has been loaded. It sets up the avatar and username, configures the table columns,
     * pagination, and search functionality.
     *
     * @param location The location used to resolve relative paths for the root object,
     *                 or null if the location is not known.
     * @param resources The resources used to localize the root object,
     *                  or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAvatarAndUserName();
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        averageRatingColumn.setCellValueFactory(new PropertyValueFactory<>("averageOfRating"));
        pagination.setPageFactory(this::createPage);
        addButtonToTable();


        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            String trimmedValue = newValue.trim();
            keyword = trimmedValue;
            loadBooks(trimmedValue, pagination.getCurrentPageIndex());
        });
    }

    /**
     * Adds a column of buttons ('Detail', 'Update', 'Delete') to the table for each row.
     * Each button has specific actions associated with it:
     * the 'Detail' button opens the book detail scene,
     * the 'Update' button opens the update book scene,
     * and the 'Delete' button opens the delete book scene.
     * <p>
     * The method defines a custom cell factory for the action column in the book table.
     * It initializes the buttons and sets their action events to handle the row's specific book.
     */
    private void addButtonToTable() {
        Callback<TableColumn<Book, Void>, TableCell<Book, Void>> cellFactory = new Callback<TableColumn<Book, Void>, TableCell<Book, Void>>() {
            @Override
            public TableCell<Book, Void> call(final TableColumn<Book, Void> param) {
                final TableCell<Book, Void> cell = new TableCell<Book, Void>() {

                    private final Button btnDelete = new Button("Delete");
                    private final Button btnDetail = new Button("Detail");
                    private final Button btnUpdate = new Button("Update");

                    {
                        btnDetail.setPrefWidth(70);
                        btnDetail.setPrefHeight(30);

                        btnUpdate.setPrefWidth(70);
                        btnUpdate.setPrefHeight(30);

                        btnDelete.setPrefWidth(70);
                        btnDelete.setPrefHeight(30);

                        btnDetail.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            Book selectedBook = getBookByIsbn(book.getIsbn());
                            openBookDetailScene(selectedBook);
                        });

                        btnUpdate.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            Book selectedBook = getBookByIsbn(book.getIsbn());
                            openUpdateBookScene(selectedBook);
                        });

                        btnDelete.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            Book selectedBook = getBookByIsbn(book.getIsbn());
                            openDeleteBookScene(selectedBook);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox hbox = new HBox(20, btnDetail, btnUpdate, btnDelete);
                            hbox.setAlignment(Pos.CENTER);
                            setGraphic(hbox);
                        }
                    }
                };
                return cell;
            }
        };
        actionColumn.setCellFactory(cellFactory);
    }


    /**
     * Loads books from the database into the book list based on the provided keyword and page index.
     * The books are then displayed in the table view and the pagination is updated.
     *
     * @param keyword   The keyword used to filter books by title, ISBN, or category.
     *                  If null or empty, all books are retrieved.
     * @param pageIndex The index of the page to be displayed. The index is used
     *                  to calculate the offset for the SQL query.
     */
    public void loadBooks(String keyword, int pageIndex) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            bookList = FXCollections.observableArrayList();
            int offset = pageIndex * rowsPerPage;
            String query;
            PreparedStatement statement;

            if (keyword == null || keyword.isEmpty()) {
                query = "SELECT * FROM books LIMIT ? OFFSET ?";
                statement = connection.prepareStatement(query);
                statement.setInt(1, rowsPerPage);
                statement.setInt(2, offset);
            } else {
                query = "SELECT * FROM books WHERE title LIKE ? OR isbn LIKE ? OR category LIKE ? LIMIT ? OFFSET ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, "%" + keyword + "%");
                statement.setString(2, "%" + keyword + "%");
                statement.setString(3, "%" + keyword + "%");
                statement.setInt(4, rowsPerPage);
                statement.setInt(5, offset);
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Integer id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String isbn = resultSet.getString("isbn");
                String category = resultSet.getString("category");
                String averageOfRating = resultSet.getString("average_of_rating");
                Book book;
                if (averageOfRating != null) {
                   book = new Book(id, title, isbn, author, category, Double.parseDouble(averageOfRating));
                }else {
                    book = new Book(id, title, isbn, author, category, 0.0);
                }

                bookList.add(book);
            }

            bookTableView.setItems(bookList);
            bookTableView.setFixedCellSize(47);
            pagination.setPageCount((int) Math.ceil((double) getTotalBookCount(keyword) / rowsPerPage));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates and returns a new page for the pagination control.
     *
     * @param pageIndex The index of the page to be created.
     * @return A node representing the newly created page.
     */
    private Node createPage(int pageIndex) {
        currentPage = pageIndex;
        loadBooks(searchTextField.getText().trim(), pageIndex);
        return new BorderPane();
    }

    /**
     * Sets the avatar and username for the currently logged-in user.
     * <p>
     * This method retrieves the current project's directory and constructs the path
     * to the user's avatar image. It first attempts to load the user's custom avatar.
     * If the custom avatar does not exist, it loads a default avatar image.
     * <p>
     * The avatar image is cropped and clipped to a circle and set to the avatarUser ImageView.
     * The user's name is retrieved and set to the userNameUser Label.
     */
    public void setAvatarAndUserName(){
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + Session.getInstance().getLoggedInUser().getAvatar();

        File file = new File(path);
        if (file.exists()) {
            Image image = new Image(file.toURI().toString());
            cropAndClipToCircle(image, avatarUser, 38.5);
        } else {
            String defaultImage = avatarsDir + "Male User.png";
            File defaultImageFile = new File(defaultImage);
            Image image = new Image(defaultImageFile.toURI().toString());
            cropAndClipToCircle(image, avatarUser, 38.5);
        }
        userNameUser.setText(Session.getInstance().getLoggedInUser().getName());
    }

    /**
     * Opens the "Add Book" scene in a new modal window.
     *
     * <p>This method loads the CreateBook.fxml file to display the UI for adding a new book.
     * It applies a brightness effect to the current stage to indicate it's inactive,
     * initializes and displays a new stage with the loaded scene, and waits for the
     * stage to close. Once the "Add Book" scene is closed, it resets the brightness effect
     * of the current stage and reloads the book data to reflect any new additions.
     *
     * <p>If an IOException occurs during loading of the FXML resource, the exception is caught and
     * printed to the stack trace.
     */
    @FXML
    private void openAddBookScene() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/CreateBook.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) bookTableView.getScene().getWindow();
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(addBookButton.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);
            });
            stage.showAndWait();

            loadBooks(keyword,currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the detailed view for a selected book in a new modal window.
     * This method loads the BookDetail.fxml file to display the book's details.
     * It applies a brightness effect to the current stage to indicate it's inactive,
     * initializes and displays a new stage with the loaded scene, and waits for the
     * stage to close. Once the detailed view is closed, it resets the brightness
     * effect of the current stage and reloads the book data.
     *
     * If an IOException occurs during loading of the FXML resource, the exception is caught and printed to the stack trace.
     *
     * @param book The book object containing details to be viewed in the detailed scene.
     */
    @FXML
    private void openBookDetailScene(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/BookDetail.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) bookTableView.getScene().getWindow();
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            BookDetailController bookDetailController = loader.getController();
            bookDetailController.setBook(book);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(bookTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);
            });
            stage.showAndWait();
            loadBooks(keyword,currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the "Update Book" scene in a new modal window.
     * <p>
     * This method loads the UpdateBook.fxml file to display the UI for updating an existing book.
     * It applies a brightness effect to the current stage to indicate it's inactive,
     * initializes and displays a new stage with the loaded scene, and waits for the
     * stage to close. Once the "Update Book" scene is closed, it resets the brightness effect
     * of the current stage and reloads the book data to reflect any updates made.
     * <p>
     * If an IOException occurs during loading of the FXML resource, the exception is caught and
     * printed to the stack trace.
     *
     * @param book The book object containing details to be updated in the "Update Book" scene.
     */
    @FXML
    private void openUpdateBookScene(Book book) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UpdateBook.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) bookTableView.getScene().getWindow();
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            UpdateBookController updateBookController = loader.getController();
            updateBookController.setBook(book);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(bookTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);
            });
            stage.showAndWait();

            loadBooks(keyword,currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the "Delete Book" scene in a new modal window.
     * <p>
     * This method loads the DeleteBook.fxml file to display the UI for deleting the selected book.
     * It sets a brightness effect on the current stage to indicate it is inactive,
     * initializes and displays a new stage with the loaded scene, and waits for the
     * stage to close. Once the "Delete Book" scene is closed, it resets the brightness
     * effect of the current stage and reloads the book data.
     * <p>
     * Catches IOException if any occurs during the loading of the FXML resource and prints it to the stack trace.
     *
     * @param book The book object to be deleted, providing details required in the scene.
     */
    @FXML
    private void openDeleteBookScene(Book book) {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/DeleteBook.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) bookTableView.getScene().getWindow();
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            DeleteBookController deleteBookController = loader.getController();
            deleteBookController.setBook(book);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(bookTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);
            });   stage.showAndWait();
            loadBooks(keyword,currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the "Add Book via API" scene in a new modal window.
     * <p>
     * This method loads the `AddBookApi.fxml` file to display the UI for adding a new book through an API.
     * It dims the current stage to indicate it is inactive, initializes and displays a new stage with the
     * loaded scene, and waits for the stage to close. Once the "Add Book via API" scene is closed, it resets
     * the brightness effect of the current stage and reloads the book data to reflect any new additions.
     * <p>
     * If an IOException occurs during loading of the FXML resource, the exception is caught and printed to the stack trace.
     */
    @FXML
    private void openAddBookApiScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AddBookApi.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) bookTableView.getScene().getWindow();
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(bookTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);
            });
            stage.showAndWait();
            loadBooks(keyword,currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the "Manage User" scene.
     * <p>
     * This method is responsible for transitioning the current scene to the "Manage User" scene.
     * It loads the ManageUser.fxml layout resource and updates the current scene's root node to display the Manage User UI.
     * <p>
     * If an IOException occurs during the loading of the FXML resource, the exception is caught and printed to the stack trace.
     */
    @FXML
    private void openManageUserScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageUser.fxml"));
            Parent manageUserRoot  = loader.load();

            Stage currentStage = (Stage) addBookButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageUserRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the "Manage Borrow" scene.
     * <p>
     * This method is responsible for transitioning the current scene to the "Manage Borrow" scene.
     * It loads the ManageBorrow.fxml layout resource and updates the current scene's root node
     * to display the Manage Borrow UI.
     * <p>
     * If an IOException occurs during the loading of the FXML resource, the exception is caught
     * and printed to the stack trace.
     */
    @FXML
    private void openManageBorrowScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageBorrow.fxml"));
            Parent manageBorrowRoot  = loader.load();

            Stage currentStage = (Stage) addBookButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBorrowRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the advertisement dashboard scene.
     * <p>
     * This method transitions the current scene to the "Ad Dashboard" scene by loading
     * the AdDashboard.fxml layout resource and updating the current scene's root node
     * to display the advertisement dashboard UI.
     * <p>
     * Catches IOException if any occurs during the loading of the FXML resource and
     * prints it to the stack trace.
     */
    @FXML
    private void openAdDashboardScene() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdDashboard.fxml"));
            Parent adminDashBoardRoot  = loader.load();

            Stage currentStage = (Stage) addBookButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(adminDashBoardRoot);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Opens the "Profile Settings" scene.
     * <p>
     * This method is responsible for transitioning the current scene to the "Profile Settings" scene.
     * It loads the ProfileSettings.fxml layout resource and updates the current scene's root node to display
     * the Profile Settings UI.
     * <p>
     * If an IOException occurs during the loading of the FXML resource, the exception is caught and printed to the stack trace.
     */
    @FXML
    private void openProfileSettingsScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ProfileSettings.fxml"));
            Parent manageBorrowRoot = loader.load();

            Stage currentStage = (Stage) addBookButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBorrowRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the logout scene in a modal window, setting up the necessary styling and positioning.
     * This method is an event handler mapped to an FXML element.
     * It loads the logout screen from the specified FXML file, configures the logout controller,
     * and displays the scene in a new modal stage.
     * The main application window gets dimmed while the logout modal is open.
     * When the modal is closed, it reloads the book data with the current keyword and page settings.
     * <p>
     * Handles any IOException that occurs during the FXML loading process by printing the stack trace.
     */
    @FXML
    private void openLogOutScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Logout.fxml"));
            Parent root = loader.load();
            stackPaneRoot.setOpacity(0.45);
            Stage currentStage = (Stage) bookTableView.getScene().getWindow();
            LogoutController logoutController = loader.getController();
            logoutController.setOwnerStage(currentStage);
            logoutController.setStackPaneRoot(stackPaneRoot);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            Rectangle clip = new Rectangle();
            clip.setWidth(424);
            clip.setHeight(204);
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            root.setClip(clip);

            stage.setResizable(false);
            stage.initOwner(currentStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnShown(event -> {
                stage.setX(currentStage.getX() + (currentStage.getWidth() - stage.getWidth()) / 2);
                stage.setY(currentStage.getY() + (currentStage.getHeight() - stage.getHeight()) / 2);
            });
            stage.showAndWait();
            loadBooks(keyword, currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

