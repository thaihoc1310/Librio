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
import librio.enums.Gender;
import librio.enums.Role;
import librio.models.User;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.getTotalUserCount;
import static librio.util.DatabaseUtil.getUserById;
import static librio.util.DesignUtil.*;

/**
 * The ManageUserController class is responsible for managing and handling user-related
 * operations within the application. It serves as the controller for user management,
 * interacting with the user interface to provide functionalities such as creating,
 * updating, viewing, and deleting users. Additionally, it facilitates navigation to
 * other scenes related to the user management lifecycle.
 */
public class ManageUserController implements Initializable {

    private final int rowsPerPage = 11;
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
    @FXML
    private ImageView avatarUser;
    @FXML
    private Label userNameUser;

    @FXML
    private StackPane stackPaneRoot;

    private ObservableList<User> userList;
    private int currentPage = 0;
    private String keyword;

    /**
     * Initializes the controller class. This method is automatically called after the FXML file
     * has been loaded. The initialization includes setting up user avatar and name, configuring table
     * column bindings, setting up pagination, and adding functionality for searching users.
     *
     * @param location The location used to resolve relative paths for the root object, or null if
     *        the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object
     *        was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAvatarAndUserName(avatarUser, userNameUser);
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

    /**
     * Adds a set of buttons (Detail, Update, Delete) to each row of a TableView's action column.
     * These buttons are capable of performing specific actions for the user entity represented by each row
     * in the user table.
     *
     * The Detail button is used to open the user detail scene for the selected user.
     * The Update button is used to open a scene where the selected user's information can be updated.
     * The Delete button is used to open a scene to confirm the deletion of the selected user.
     *
     * Each button is styled with specific width and height settings and is placed together in an HBox
     * with a spacing of 20 units between them. The buttons are centered within their TableCell.
     */
    private void addButtonToTable() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<>() {

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


                        btnUpdate.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
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


    /**
     * Loads a list of users from the database based on a search keyword and
     * pagination settings. Users are populated into an observable list which
     * is then set to a table view, also updating the pagination controls.
     *
     * @param keyword the search keyword to filter users by their name, email,
     *        or phone number. If null or empty, all users are loaded without
     *        filtering.
     * @param pageIndex the index of the page to load, used for pagination.
     *        Determines the offset in the database query to load the appropriate
     *        subset of users.
     */
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


    /**
     * Creates a new page for the pagination component by setting the current page index
     * and loading the corresponding user data.
     *
     * @param pageIndex the index of the page to be created
     * @return a new instance of a BorderPane representing the page
     */
    private Node createPage(int pageIndex) {
        currentPage = pageIndex;
        loadUsers(searchTextField.getText().trim(), pageIndex);
        return new BorderPane();
    }

    /**
     * Opens a new scene for creating a user in the application.
     * This method loads the 'CreateUser.fxml' file using an FXMLLoader,
     * sets up a new stage with a transparent style, and displays it
     * as a modal dialog.
     *
     * The current window's brightness is temporarily reduced while the
     * new user creation scene is open. The brightness and UI effect is
     * restored once the dialog is closed.
     *
     * After the scene is closed, it will refresh the user list using the
     * current search keyword and page index.
     *
     * Catches and prints an IOException if loading the FXML file fails.
     */
    @FXML
    private void openCreateUserScene() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/CreateUser.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) userTableView.getScene().getWindow();
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(createUserButton.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);
            });
            stage.showAndWait();
            loadUsers(keyword, currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the Update User Scene, allowing the user information to be modified.
     *
     * @param user The User object containing the information to be updated in the scene.
     */
    @FXML
    private void openUpdateUserScene(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UpdateUser.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) userTableView.getScene().getWindow();
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            UpdateUserController updateUserController = loader.getController();
            updateUserController.setUser(user);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);

            stage.setResizable(false);
            stage.initOwner(userTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);
            });
            stage.showAndWait();
            loadUsers(keyword, currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the user detail scene in a modal window, displaying detailed information about the specified user.
     * This method applies a dimming effect to the main window while the modal window is open.
     *
     * @param user the User object containing details to be displayed in the user detail scene
     */
    @FXML
    private void openUserDetailScene(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UserDetails.fxml"));
            Parent root = loader.load();
            UserDetailsController userDetailsController = loader.getController();
            userDetailsController.setUser(user);
            Stage currentStage = (Stage) userTableView.getScene().getWindow();
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(userTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);
            });
            stage.showAndWait();
            loadUsers(keyword, currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the "Delete User" scene for the specified user. This method loads a new FXML scene that allows
     * for the deletion of a user, applying a brightness adjustment to the current stage while the scene is open.
     *
     * @param user the User object to be passed to the delete user scene. This user is targeted for deletion,
     *             allowing the new scene to display relevant information about the user.
     */
    @FXML
    private void openDeleteUserScene(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/DeleteUser.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) userTableView.getScene().getWindow();
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);

            DeleteUserController deleteUserController = loader.getController();
            deleteUserController.setUser(user);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(userTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);
            });
            stage.showAndWait();
            loadUsers(keyword, currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the logout scene by loading the Logout.fxml file and displaying it in a modal Stage.
     * The method first sets the opacity of the main stack pane to give a dimmed background effect.
     * It positions the new stage centered over the current stage with undecorated styling and
     * ensures the logout scene is modal with respect to the current window. It also clips the
     * logout window to have rounded corners. Upon closing the modal logout scene, it refreshes
     * the user list by calling the loadUsers method with the current keyword and page index.
     * Handles IOException if the FXMLLoader fails to load the resource.
     */
    @FXML
    private void openLogOutScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Logout.fxml"));
            Parent root = loader.load();
            stackPaneRoot.setOpacity(0.45);
            Stage currentStage = (Stage) userTableView.getScene().getWindow();
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
            loadUsers(keyword, currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Opens the Manage Book scene in the application. This method switches the current scene
     * to the Manage Book view, using the provided FXML file path to load the new view.
     * It is triggered by a user action on the related UI component, such as the create user button.
     */
    @FXML
    private void openManageBookScene() {
        switchScene(createUserButton,"/fxml/admin/ManageBook.fxml");
    }

    /**
     * Opens the Manage Borrow Scene by switching the current scene to the specified FXML file.
     * This method is triggered by a UI event and transitions the user to the "Manage Borrow" interface.
     * It utilizes the switchScene utility method to change the current scene.
     */
    @FXML
    private void openManageBorrowScene() {
        switchScene(createUserButton,"/fxml/admin/ManageBorrow.fxml");
    }

    /**
     * Opens the admin dashboard scene by switching the current scene to the AdDashboard.fxml view.
     * The method leverages the switchScene utility method to handle the transition.
     * It is triggered through a JavaFX FXML mechanism, typically by a user interaction event.
     */
    @FXML
    private void openAdDashboardScene() {
        switchScene(createUserButton,"/fxml/admin/AdDashboard.fxml");
    }

    /**
     * Handles the action of opening the Profile Settings scene.
     * This method is triggered by an FXML event and performs a scene switch
     * to the Profile Settings interface, defined in the FXML file
     * located at '/fxml/admin/ProfileSettings.fxml'.
     */
    @FXML
    private void openProfileSettingsScene() {
        switchScene(createUserButton,"/fxml/admin/ProfileSettings.fxml");
    }
}
