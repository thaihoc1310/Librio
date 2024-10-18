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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import librio.models.Gender;
import librio.models.Role;
import librio.models.User;
import librio.database.DatabaseConnection;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.PreparedStatement;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.getTotalUserCount;
import static librio.util.DatabaseUtil.getUserById;

public class ManageUserController implements Initializable {

    @FXML
    private TableView<User> userTableView;
    @FXML
    private TableColumn<User, String> idColumn;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> phoneNumberColumn;
    @FXML
    private TableColumn<User, Gender> genderColumn;
    @FXML
    private TableColumn<User, Role> roleColumn;
    @FXML
    private TableColumn<User, Void> actionColumn;
    @FXML
    private Button createUserButton;
    @FXML
    private Pagination pagination;
    @FXML
    private TextField searchTextField;

    private ObservableList<User> userList;

    private int currentPage = 0;
    private final int rowsPerPage = 11;

    private String keyword;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        pagination.setPageFactory(this::createPage);
        addButtonToTable();

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            String trimmedValue = newValue.trim();
            keyword = trimmedValue;
            loadUsers(trimmedValue, pagination.getCurrentPageIndex());
        });
    }

    private void addButtonToTable() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<>() {

                    private final Button btnDelete = new Button("Delete");
                    private final Button btnDetail = new Button("Detail");
                    private final Button btnUpdate = new Button("Update");

                    {
                        btnUpdate.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            // Fetch updated user data from the database
                            User updatedUser = getUserById(user.getId());
                            openUpdateUserScene(updatedUser);
                        });

                        btnDetail.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            User selectedUser = getUserById(user.getId());
                            openUserDetailScene(selectedUser);
                        });

                        btnDelete.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            User selectedUser = getUserById(user.getId());
                            openDeleteUserScene(selectedUser);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // Tạo một HBox để chứa các nút
                            HBox hbox = new HBox(20, btnDetail, btnUpdate, btnDelete);
                            hbox.setAlignment(Pos.CENTER);
                            setGraphic(hbox);
                        }
                    }
                };
            }
        };
        actionColumn.setCellFactory(cellFactory);
    }

    void loadUsers(String keyword, int pageIndex) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            userList = FXCollections.observableArrayList();
            int offset = pageIndex * rowsPerPage;
            String query;
            PreparedStatement statement;

            if (keyword == null || keyword.isEmpty()) {
                query = "SELECT * FROM users LIMIT ? OFFSET ?";
                statement = connection.prepareStatement(query);
                statement.setInt(1, rowsPerPage);
                statement.setInt(2, offset);
            } else {
                query = "SELECT * FROM users WHERE name LIKE ? OR email LIKE ? OR phone_number LIKE ? LIMIT ? OFFSET ?";
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
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String phoneNumber = resultSet.getString("phone_number");
                Gender gender = Gender.valueOf(resultSet.getString("gender").toUpperCase());
                Role role = Role.valueOf(resultSet.getString("role").toUpperCase());
                User user = new User(id, name, email, phoneNumber, gender, role);
                userList.add(user);
            }
            userTableView.setItems(userList);
            userTableView.setFixedCellSize(47);
            pagination.setPageCount((int) Math.ceil((double) getTotalUserCount(keyword) / rowsPerPage));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private Node createPage(int pageIndex) {
        currentPage = pageIndex;
        loadUsers(searchTextField.getText().trim(), pageIndex);
        return new BorderPane();
    }

    @FXML
    private void openCreateUserScene() {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/CreateUser.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Create New User");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initOwner(createUserButton.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            // Hiển thị scene
            stage.showAndWait();
            loadUsers(keyword,currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openUpdateUserScene(User user) {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UpdateUser.fxml"));
            Parent root = loader.load();

            // Tạo controller và truyền ManageUserController và User vào
            UpdateUserController updateUserController = loader.getController();
            updateUserController.setUser(user);
            // Tạo stage mới cho scene
            Stage stage = new Stage();
            stage.setTitle("Update User");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initOwner(userTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            // Hiển thị scene
            stage.showAndWait();
            loadUsers(keyword,currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openUserDetailScene(User user) {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UserDetails.fxml"));
            Parent root = loader.load();

            // Tạo controller và truyền ManageUserController và User vào
            UserDetailsController userDetailsController = loader.getController();
            userDetailsController.setUser(user);

            // Tạo stage mới cho scene
            Stage stage = new Stage();
            stage.setTitle("User detail");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initOwner(userTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            // Hiển thị scene
            stage.showAndWait();
            loadUsers(keyword,currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openDeleteUserScene(User user) {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/DeleteUser.fxml"));
            Parent root = loader.load();

            // Tạo controller và truyền ManageUserController và User vào
            DeleteUserController deleteUserController = loader.getController();
            deleteUserController.setUser(user);

            // Tạo stage mới cho scene
            Stage stage = new Stage();
            stage.setTitle("Delete User");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initOwner(userTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            // Hiển thị scene
            stage.showAndWait();
            loadUsers(keyword,currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
