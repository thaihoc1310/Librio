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
import librio.cache.ImageCache;
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
import static librio.util.DesignUtil.*;

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


    /**
     * Initializes the ManageBookController by setting up the avatar and username,
     * configuring table column property factories, setting up pagination, and adding
     * interactive buttons to the table.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAvatarAndUserName(avatarUser, userNameUser);
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
     * Adds a column of buttons to the table for each book entry. The buttons
     * include "Detail", "Update", and "Delete". Each button is set to perform
     * an action when clicked:
     *
     * - The "Detail" button opens the book detail scene for the selected book.
     * - The "Update" button opens the update book scene for the selected book.
     * - The "Delete" button opens the delete book scene for the selected book.
     *
     * The buttons are displayed in a horizontal box with a set alignment and spacing.
     * The method configures the cell factory for the action column to include these buttons.
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
     * Loads a list of books from the database based on the provided keyword and pagination index.
     *
     * @param keyword    The keyword used to filter the books by title, ISBN, or category. If null or empty, all books are retrieved.
     * @param pageIndex  The index of the page of results to retrieve, used for pagination.
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
                    Double rating = 0.0;
                    try {
                        rating = Double.parseDouble(averageOfRating);  // Cố gắng chuyển thành số
                    } catch (NumberFormatException e) {
                        rating = 0.0;
                    }
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
     * This method updates the current page index and loads books for that page
     * based on the current search keyword.
     *
     * @param pageIndex the index of the page to be created
     * @return a new Node representing the page that was created
     */
    private Node createPage(int pageIndex) {
        currentPage = pageIndex;
        loadBooks(searchTextField.getText().trim(), pageIndex);
        return new BorderPane();
    }

    /**
     * Opens a new scene for adding a book within the application. The method loads
     * the FXML layout for the 'CreateBook' scene and displays it in a new transparent
     * modal window. The brightness of the current window is temporarily adjusted to
     * indicate modal focus on the new scene. Once the modal is closed, the brightness
     * is reset, and a method to reload the list of books is called to refresh any changes.
     *
     * The method performs the following actions:
     * 1. Loads the 'CreateBook.fxml' layout using FXMLLoader.
     * 2. Applies a darkening effect to the current window to highlight the modal window.
     * 3. Configures and shows a new modal stage that is non-resizable and
     *    transparent in style, ensuring it remains on top of the current window.
     * 4. Waits for the modal window to close (blocking the current window).
     * 5. Resets the brightness effect on the current window upon closure of the modal.
     * 6. Reloads the book list to reflect any updates that may have occurred.
     *
     * Handles IOException during the FXML loading process by printing the stack trace.
     *
     * This method is annotated with @FXML to indicate its use with JavaFX scene graphs.
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
     * Opens the Book Detail scene in a new modal window with the details of the specified book.
     * Adjusts the brightness of the current stage to highlight the modal window, and resets it
     * when the modal window is closed.
     *
     * @param book the Book object containing the details to be displayed in the Book Detail scene
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
     * Opens the Update Book scene, allowing the user to edit the details of the selected book.
     * The current window is dimmed while the update scene is active.
     *
     * @param book the Book object containing details to be updated
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
     * Opens the Delete Book scene in a new modal window. This method is triggered by a
     * specific user action to initiate the process of deleting a book record. It applies a
     * semi-transparent effect to the current stage while the Delete Book window is active.
     *
     * @param book the Book object to be deleted, which is passed to the DeleteBookController
     *             for display and reference within the Delete Book scene.
     */
    @FXML
    private void openDeleteBookScene(Book book) {
        try {
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
     * Opens a new scene to add a book via an external API.
     * <ul>
     *   <li>Loads the 'AddBookApi.fxml' file to display the UI for adding books via an API.</li>
     *   <li>Adjusts the brightness of the current window to indicate a modal dialog is open.</li>
     *   <li>Initializes a new stage with transparent styling and sets it as a modal window.</li>
     *   <li>On closing the modal, resets the brightness effect on the root node of the current scene.</li>
     *   <li>Reloads the list of books upon closing the modal window to reflect any new additions.</li>
     * </ul>
     * Catches and logs an IOException if the FXMLLoader fails to load the FXML file.
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
     * Opens the "Manage User" scene within the application.
     * This method is triggered by an event associated with the corresponding UI component
     * and changes the current scene to display the user management interface.
     * The scene is defined in the FXML file located at "/fxml/admin/ManageUser.fxml".
     */
    @FXML
    private void openManageUserScene() {
        switchScene(addBookButton,"/fxml/admin/ManageUser.fxml");
    }

    /**
     * Opens the Manage Borrow scene in the application.
     * <br>
     * This method is triggered when the associated UI action occurs, typically when the user clicks
     * the button associated with managing borrowing tasks in the application UI. It switches the current
     * scene to the Manage Borrow interface.
     * <br>
     * The method utilizes the {@code switchScene} utility function to load and display the FXML layout
     * defined in the ManageBorrow.fxml file. It updates the current stage with the loaded FXML scene,
     * ensuring the UI transitions smoothly to the borrowing management interface.
     */
    @FXML
    private void openManageBorrowScene() {
        switchScene(addBookButton,"/fxml/admin/ManageBorrow.fxml");
    }


    /**
     * Opens the advertisement dashboard scene.
     *
     * This method is triggered by a user action in the UI, specifically
     * when the associated button is clicked. It uses the `switchScene`
     * method to change the current scene to the advertisement dashboard
     * by loading the FXML layout defined in "/fxml/admin/AdDashboard.fxml".
     */
    @FXML
    private void openAdDashboardScene() {
        switchScene(addBookButton,"/fxml/admin/AdDashboard.fxml");
    }


    /**
     * Opens the Profile Settings scene.
     * This method is triggered by an FXML event and calls the switchScene method
     * to load and set the ProfileSettings.fxml file as the current scene.
     * It uses the addBookButton to obtain the current stage and switch the scene.
     */
    @FXML
    private void openProfileSettingsScene() {
        switchScene(addBookButton,"/fxml/admin/ProfileSettings.fxml");
    }


    /**
     * Opens the logout dialog window with reduced opacity for the main application window.
     * This method is responsible for loading the 'Logout.fxml' file, setting up the stage,
     * and displaying it as a modal dialog. The logout dialog is centered relative to the
     * current application window and is styled with rounded corners.
     * The method waits for the user interaction before resuming execution, at which point
     * it reloads the list of books using the current search keyword and page index.
     *
     * Handles potential IOExceptions that can occur during the FXMLLoader loading process.
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

    public ObservableList<Book> getBookList() {
        return bookList;
    }

    public void setBookList(ObservableList<Book> bookList) {
        this.bookList = bookList;
    }
}

