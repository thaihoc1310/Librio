package librio.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import librio.models.Gender;
import librio.models.User;
import librio.database.DatabaseConnection;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.PreparedStatement;
import java.util.ResourceBundle;

public class ManageUserController implements Initializable {

    @FXML
    private TableView<User> userTableView;
    @FXML
    private TableColumn<User, String> idColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> phoneNumberColumn;
    @FXML
    private TableColumn<User, Void> actionColumn;
    @FXML
    private TextField searchTextField;

    private ObservableList<User> userList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        loadUsersFromDatabase();
        addActionButtonsToTable();
    }
    private void addActionButtonsToTable() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                final TableCell<User, Void> cell = new TableCell<>() {

                    private final Button editButton = new Button("Sửa");
                    private final Button deleteButton = new Button("Xóa");
                    private final Button addButton = new Button("Thêm");
                    private final HBox actionButtons = new HBox(addButton,editButton, deleteButton);

                    {
                        actionButtons.setSpacing(10);
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(actionButtons);
                        }
                    }
                };
                return cell;
            }
        };
        actionColumn.setCellFactory(cellFactory);
    }
    private void loadUsersFromDatabase() {
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

                User user = new User(id, name, email, phoneNumber);
                userList.add(user);
            }

            userTableView.setItems(userList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterUsers(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            userTableView.setItems(userList);
        } else {
            ObservableList<User> filteredList = FXCollections.observableArrayList();
            for (User user : userList) {
                if (user.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        user.getEmail().toLowerCase().contains(keyword.toLowerCase())) {
                    filteredList.add(user);
                }
            }
            userTableView.setItems(filteredList);
        }
    }


}
