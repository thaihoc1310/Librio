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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import librio.database.DatabaseConnection;
import librio.models.Borrow;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ResourceBundle;

public class ManageBorrowController implements Initializable {

    @FXML
    private TableView<Borrow> borrowTableView;
    @FXML
    private TableColumn<Borrow, String> borrowIdColumn;
    @FXML
    private TableColumn<Borrow, String> memberIdColumn;
    @FXML
    private TableColumn<Borrow, String> bookIdColumn;
    @FXML
    private TableColumn<Borrow, Instant> borrowDateColumn;
    @FXML
    private TableColumn<Borrow, Instant> dueDateColumn;
    @FXML
    private TableColumn<Borrow, Instant> returnDateColumn;
    @FXML
    private TableColumn<Borrow, String> statusColumn;
    @FXML
    private TableColumn<Borrow, Double> fineColumn;
    @FXML
    private TableColumn<Borrow, Void> actionColumn;
    @FXML
    private Pagination pagination;
    @FXML
    private TextField searchTextField;

    private ObservableList<Borrow> borrowList;

    private int currentPage = 0;
    private final int rowsPerPage = 11;

    private String keyword = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        borrowIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        memberIdColumn.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        bookIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        fineColumn.setCellValueFactory(new PropertyValueFactory<>("fine"));

        addButtonToTable();

        pagination.setPageFactory(this::createPage);

        // Listener cho TextField tìm kiếm
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            String trimmedValue = newValue.trim();
            loadBorrows(trimmedValue, pagination.getCurrentPageIndex());
        });
    }

    private void addButtonToTable() {
        Callback<TableColumn<Borrow, Void>, TableCell<Borrow, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Borrow, Void> call(final TableColumn<Borrow, Void> param) {
                final TableCell<Borrow, Void> cell = new TableCell<>() {

                    private final Button btnUpdate = new Button("Update");
                    private final Button btnDelete = new Button("Delete");

                    {
                        btnUpdate.setOnAction(event -> {
                            Borrow borrow = getTableView().getItems().get(getIndex());
                            // Gọi phương thức để mở form cập nhật thông tin
                            openUpdateBorrowScene(borrow);
                        });

                        btnDelete.setOnAction(event -> {
                            Borrow borrow = getTableView().getItems().get(getIndex());
                            // Gọi phương thức để xóa thông tin
                            deleteBorrow(borrow);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox managebtn = new HBox(btnUpdate, btnDelete);
                            managebtn.setSpacing(10);
                            managebtn.setAlignment(Pos.CENTER);
                            setGraphic(managebtn);
                        }
                    }
                };
                return cell;
            }
        };

        actionColumn.setCellFactory(cellFactory);
    }


    private void openUpdateBorrowScene(Borrow borrow) {
        System.out.println("Update Borrow: " + borrow.getId());
        // Logic để mở giao diện cập nhật Borrow
    }

    private void deleteBorrow(Borrow borrow) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM borrows WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, borrow.getId());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Deleted Borrow with ID: " + borrow.getId());
                borrowTableView.getItems().remove(borrow);
            } else {
                System.out.println("Failed to delete Borrow with ID: " + borrow.getId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void loadBorrows(String keyword, int pageIndex) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            borrowList = FXCollections.observableArrayList();
            int offset = pageIndex * rowsPerPage;
            String query;
            PreparedStatement statement;

            if (keyword == null || keyword.isEmpty()) {
                query = "SELECT * FROM borrows LIMIT ? OFFSET ?";
                statement = connection.prepareStatement(query);
                statement.setInt(1, rowsPerPage);
                statement.setInt(2, offset);
            } else {
                query = "SELECT * FROM borrows WHERE member_id LIKE ? OR book_id LIKE ? LIMIT ? OFFSET ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, "%" + keyword + "%");
                statement.setString(2, "%" + keyword + "%");
                statement.setInt(3, rowsPerPage);
                statement.setInt(4, offset);
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String borrowId = resultSet.getString("id");
                String memberId = resultSet.getString("member_id");
                String bookId = resultSet.getString("book_id");
                Instant borrowDate = resultSet.getTimestamp("borrow_date").toInstant();
                Instant dueDate = resultSet.getTimestamp("due_date").toInstant();
                Instant returnDate = resultSet.getTimestamp("return_date") != null ? resultSet.getTimestamp("return_date").toInstant() : null;
                double fine = resultSet.getDouble("fine");
                String status = resultSet.getString("status");

                Borrow borrow = new Borrow(borrowId, bookId, memberId, borrowDate, dueDate, returnDate, status, fine);
                borrowList.add(borrow);

            }
            borrowTableView.setItems(borrowList);
            borrowTableView.setFixedCellSize(47);
            pagination.setPageCount((int) Math.ceil((double) getTotalBorrowCount(keyword) / rowsPerPage));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getTotalBorrowCount(String keyword) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query;
            PreparedStatement statement;

            if (keyword == null || keyword.isEmpty()) {
                query = "SELECT COUNT(*) FROM borrows";
                statement = connection.prepareStatement(query);
            } else {
                query = "SELECT COUNT(*) FROM borrows WHERE member_id LIKE ? OR book_id LIKE ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, "%" + keyword + "%");
                statement.setString(2, "%" + keyword + "%");
            }

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Node createPage(int pageIndex) {
        currentPage = pageIndex;
        loadBorrows(searchTextField.getText().trim(), pageIndex);
        return new BorderPane();
    }

    @FXML
    private void openManageBookScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageBook.fxml"));
            Parent manageBookRoot = loader.load();

            Stage currentStage = (Stage) borrowTableView.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBookRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openManageUserScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageUser.fxml"));
            Parent manageUserRoot = loader.load();

            Stage currentStage = (Stage) borrowTableView.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageUserRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openAdDashboardScene() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdDashboard.fxml"));
            Parent adminDashboardRoot  = loader.load();

            Stage currentStage = (Stage) borrowTableView.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(adminDashboardRoot);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
