package librio.controllers.admin;

import javafx.beans.property.StringProperty;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import librio.controllers.admin.BookDetailController;
import librio.database.DatabaseConnection;
import librio.models.Book;
import librio.models.Gender;
import librio.models.Role;
import librio.models.User;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.getTotalBookCount;
import static librio.util.DatabaseUtil.getTotalUserCount;

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
    private TableColumn<Book, String> languageColumn;
    @FXML
    private TableColumn<Book, String> publisherColumn;
    @FXML
    private TableColumn<Book, Void> actionColumn;
    @FXML
    private Button addBookButton;
    @FXML
    private Pagination pagination;
    @FXML
    private TextField searchTextField;

    private ObservableList<Book> bookList;

    private int currentPage = 0;
    private final int rowsPerPage = 11;

    private String keyword = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Thiết lập dữ liệu cho các cột
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        languageColumn.setCellValueFactory(new PropertyValueFactory<>("language"));
        publisherColumn.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        pagination.setPageFactory(this::createPage);
        addButtonToTable();


        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            String trimmedValue = newValue.trim();
            keyword = trimmedValue;
            loadBooks(trimmedValue, pagination.getCurrentPageIndex());
        });
    }

    private void addButtonToTable() {
        Callback<TableColumn<Book, Void>, TableCell<Book, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Book, Void> call(final TableColumn<Book, Void> param) {
                final TableCell<Book, Void> cell = new TableCell<>() {

                    private final Button btnDelete = new Button("Remove");
                    private final Button btnDetail = new Button("Detail");
                    private final Button btnUpdate = new Button("Edit");

                    {
                        btnDetail.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            Book selectedBook = getBookById(book.getId());
                            openBookDetailScene(selectedBook);
                        });

                        btnUpdate.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            Book selectedBook = getBookById(book.getId());
                            openUpdateBookScene(selectedBook);
                            // Xử lý logic cập nhật sách
                        });

                        btnDelete.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            Book selectedBook = getBookById(book.getId());
                            openDeleteBookScene(selectedBook);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // Tạo HBox để chứa các nút
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

    private Book getBookById(String bookId) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM books WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, bookId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String id = resultSet.getString("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String isbn = resultSet.getString("isbn");
                String publisher = resultSet.getString("publisher");
                String category = resultSet.getString("category");
                Integer quantityCopy = resultSet.getInt("quantity_copy");
                String yearPublished = resultSet.getString("year_published");
                String language = resultSet.getString("language");
                String numberOfPages = resultSet.getString("number_of_pages");
                String description = resultSet.getString("description");
                String bookImage = resultSet.getString("book_image");
                return new Book(id, title, author, isbn, category, publisher, quantityCopy ,yearPublished, language, numberOfPages, description, bookImage);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

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
                String id = resultSet.getString("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String publisher = resultSet.getString("publisher");
                String category = resultSet.getString("category");
                String language = resultSet.getString("language");
                Book book = new Book(id, title, author, category, language, publisher);
                bookList.add(book);
            }

            bookTableView.setItems(bookList);
            bookTableView.setFixedCellSize(47);
            pagination.setPageCount((int) Math.ceil((double) getTotalBookCount(keyword) / rowsPerPage));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Node createPage(int pageIndex) {
        currentPage = pageIndex;
        loadBooks(searchTextField.getText().trim(), pageIndex);
        return new BorderPane();
    }

    @FXML
    private void openAddBookScene() {
        try {
            // Tải FXML của scene thêm sách mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/CreateBook.fxml"));
            Parent root = loader.load();

            // Tạo stage mới cho scene
            Stage stage = new Stage();
            stage.setTitle("Add New Book");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initOwner(addBookButton.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);

            // Hiển thị scene
            stage.showAndWait();

            // Sau khi đóng cửa sổ thêm sách, tải lại danh sách sách từ database
            loadBooks(keyword,currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openBookDetailScene(Book book) {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/BookDetail.fxml"));
            Parent root = loader.load();

            // Lấy controller của BookDetailController và truyền dữ liệu
            BookDetailController bookDetailController = loader.getController();
            bookDetailController.setBook(book);

            // Tạo stage mới cho scene
            Stage stage = new Stage();
            stage.setTitle("Book Detail");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initOwner(bookTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);

            // Hiển thị scene
            stage.showAndWait();
            loadBooks(keyword,currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openUpdateBookScene(Book book) {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UpdateBook.fxml"));
            Parent root = loader.load();

            // Lấy controller của BookDetailController và truyền dữ liệu
            UpdateBookController updateBookController = loader.getController();
            updateBookController.setBook(book);

            // Tạo stage mới cho scene
            Stage stage = new Stage();
            stage.setTitle("Update Book");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initOwner(bookTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);

            // Hiển thị scene
            stage.showAndWait();

            loadBooks(keyword,currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openDeleteBookScene(Book book) {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/DeleteBook.fxml"));
            Parent root = loader.load();

            // Tạo controller và truyền ManageUserController và User vào
            DeleteBookController deleteBookController = loader.getController();
            deleteBookController.setBook(book);

            // Tạo stage mới cho scene
            Stage stage = new Stage();
            stage.setTitle("Delete User");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initOwner(bookTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            // Hiển thị scene
            stage.showAndWait();
            loadBooks(keyword,currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
}

