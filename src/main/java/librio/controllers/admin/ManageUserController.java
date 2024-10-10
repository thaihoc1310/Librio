package librio.controllers.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
import java.util.Arrays;
import java.util.ResourceBundle;

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

    private ObservableList<User> userList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        loadUsersFromDatabase();
        addButtonToTable();
    }

    private void addButtonToTable() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                final TableCell<User, Void> cell = new TableCell<>() {

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
                            hbox.setStyle("-fx-padding: 0 0 0 20;");
                            setGraphic(hbox);
                        }
                    }
                };
                return cell;
            }
        };
        actionColumn.setCellFactory(cellFactory);
    }

    private User getUserById(String userId) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String id = resultSet.getString("id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String phoneNumber = resultSet.getString("phone_number");
                String address = resultSet.getString("address") ;
                Gender gender = Gender.valueOf(resultSet.getString("gender").toUpperCase());
                Role role = Role.valueOf(resultSet.getString("role").toUpperCase());
                String avatar = resultSet.getString("avatar");

                return new User(id, name, email, phoneNumber, address, gender, role, avatar);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loadUsersFromDatabase() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            userList = FXCollections.observableArrayList();
            String query = "SELECT * FROM users";
            PreparedStatement statement = connection.prepareStatement(query);
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void openCreateUserScene() {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/CreateUser.fxml"));
            Parent root = loader.load();

            CreateUserController createUserController = loader.getController();
            createUserController.setManageUserController(this);

            // Tạo stage mới cho scene
            Stage stage = new Stage();
            stage.setTitle("Create New User");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initOwner(createUserButton.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            // Hiển thị scene
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openUpdateUserScene(User user) {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UpdateUser.fxml"));
            Parent root = loader.load();

            // Tạo controller và truyền ManageUserController và User vào
            UpdateUserController updateUserController = loader.getController();
            updateUserController.setManageUserController(this);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openUserDetailScene(User user) {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UserDetails.fxml"));
            Parent root = loader.load();

            // Tạo controller và truyền ManageUserController và User vào
            UserDetailsController userDetailsController = loader.getController();
            userDetailsController.setManageUserController(this);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDeleteUserScene(User user) {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/DeleteUser.fxml"));
            Parent root = loader.load();

            // Tạo controller và truyền ManageUserController và User vào
            DeleteUserController deleteUserController = loader.getController();
            deleteUserController.setManageUserController(this);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
