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
import librio.models.Borrow;
import librio.enums.Status;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.getBorrowById;
import static librio.util.DatabaseUtil.getTotalBorrowCount;
import static librio.util.DesignUtil.*;

/**
 * The ManageBorrowController class is responsible for handling the user interface
 * logic for managing borrow records within the application. It provides functionalities
 * such as viewing, updating, deleting, and creating borrow records. The class
 * interacts with various components of the JavaFX framework, such as TableView,
 * Pagination, and TextField, to provide a responsive and interactive experience.
 */
public class ManageBorrowController implements Initializable {

    @FXML
    private TableView<Borrow> borrowTableView;
    @FXML
    private TableColumn<Borrow, String> borrowIdColumn;
    @FXML
    private TableColumn<Borrow, String> emailColumn;
    @FXML
    private TableColumn<Borrow, String> bookIsbnColumn;
    @FXML
    private TableColumn<Borrow, LocalDate> borrowDateColumn;
    @FXML
    private TableColumn<Borrow, LocalDate> dueDateColumn;
    @FXML
    private TableColumn<Borrow, LocalDate> returnDateColumn;
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
    @FXML
    private Button createBorrowButton;
    @FXML
    private StackPane stackPaneRoot;
    @FXML
    private Label userNameUser;
    @FXML
    private ImageView avatarUser;


    private ObservableList<Borrow> borrowList;


    private int currentPage = 0;
    private final int rowsPerPage = 11;

    private String keyword = null;

    /**
     * Initializes the controller class. This method is automatically called after the FXML file has been loaded.
     * It sets up the avatar and user name, configures table columns, adds buttons to the table, sets up pagination,
     * and attaches a listener for the search text field.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAvatarAndUserName(avatarUser, userNameUser);
        borrowIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        bookIsbnColumn.setCellValueFactory(new PropertyValueFactory<>("bookIsbn"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        fineColumn.setCellValueFactory(new PropertyValueFactory<>("fine"));

        addButtonToTable();

        pagination.setPageFactory(this::createPage);

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            String trimmedValue = newValue.trim();
            keyword = trimmedValue;
            loadBorrows(trimmedValue, pagination.getCurrentPageIndex());
        });
    }

    /**
     * Adds action buttons to the "actionColumn" in the table view for each row.
     * The buttons include:
     * - Detail: Opens a detailed view of the borrow record.
     * - Update: Opens an update form for the borrow record.
     * - Delete: Opens a confirmation dialog to delete the borrow record.
     *
     * Each button is set with a fixed width and height and their corresponding actions
     * retrieve the borrow record associated with the row, then execute a specific method
     * for further actions based on the button clicked.
     *
     * The method uses a callback for the cell factory of the "actionColumn" to generate
     * custom cells that contain three buttons aligned in a horizontal box (HBox).
     * The buttons are only added when the cell is not empty.
     */
    private void addButtonToTable() {
        Callback<TableColumn<Borrow, Void>, TableCell<Borrow, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Borrow, Void> call(final TableColumn<Borrow, Void> param) {
                return new TableCell<>() {

                    private final Button btnDetail = new Button("Detail");
                    private final Button btnUpdate = new Button("Update");
                    private final Button btnDelete = new Button("Delete");

                    {

                        btnDetail.setPrefWidth(70);
                        btnDetail.setPrefHeight(30);

                        btnUpdate.setPrefWidth(70);
                        btnUpdate.setPrefHeight(30);

                        btnDelete.setPrefWidth(70);
                        btnDelete.setPrefHeight(30);

                        btnDetail.setOnAction(event -> {
                            Borrow borrow = getTableView().getItems().get(getIndex());
                            Borrow dbBorrow = getBorrowById(String.valueOf(borrow.getId()));
                            openBorrowDetailScene(dbBorrow);
                        });

                        btnUpdate.setOnAction(event -> {
                            Borrow borrow = getTableView().getItems().get(getIndex());
                            Borrow dbBorrow = getBorrowById(String.valueOf(borrow.getId()));
                            openUpdateBorrowScene(dbBorrow);
                        });

                        btnDelete.setOnAction(event -> {
                            Borrow borrow = getTableView().getItems().get(getIndex());
                            Borrow dbBorrow = getBorrowById(String.valueOf(borrow.getId()));
                            openDeleteBorrowScene(dbBorrow);
                        });

                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox hbox = new HBox(20,btnDetail,btnUpdate, btnDelete);
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
     * Loads a list of borrow records from the database into the borrowList,
     * filters these records by a keyword if provided, and paginates the results.
     *
     * @param keyword a string used to filter borrow records based on status, book ISBN, or user email.
     *                If null or empty, no filtering is applied.
     * @param pageIndex the index of the page to load, used to calculate the offset for pagination.
     */
    void loadBorrows(String keyword, int pageIndex) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            borrowList = FXCollections.observableArrayList();
            int offset = pageIndex * rowsPerPage;
            String query;
            PreparedStatement statement;

            if (keyword == null || keyword.isEmpty()) {
                query = "SELECT * FROM borrows b JOIN users u ON b.member_id = u.id " +
                        "ORDER BY b.id LIMIT ? OFFSET ?";
                statement = connection.prepareStatement(query);
                statement.setInt(1, rowsPerPage);
                statement.setInt(2, offset);
            } else {
                query = "SELECT * FROM borrows b JOIN users u ON b.member_id = u.id " +
                        "WHERE b.status LIKE ? OR b.book_isbn LIKE ? OR u.email LIKE ?" +
                        "ORDER BY b.id LIMIT ? OFFSET ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, "%" + keyword + "%");
                statement.setString(2, "%" + keyword + "%");
                statement.setString(3, "%" + keyword + "%");
                statement.setInt(4, rowsPerPage);
                statement.setInt(5, offset);
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Integer borrowId = resultSet.getInt("id");
                String email = resultSet.getString("email");
                String bookIsbn = resultSet.getString("book_isbn");
                LocalDate borrowDate = resultSet.getDate("borrow_date").toLocalDate();
                LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();
                LocalDate returnDate = resultSet.getDate("return_date") != null ? resultSet.getDate("return_date").toLocalDate() : null;
                double fine = resultSet.getDouble("fine");
                Status status = Status.valueOf(resultSet.getString("status"));

                Borrow borrow = new Borrow(borrowId, bookIsbn, email, borrowDate, dueDate, returnDate, status, fine);
                borrowList.add(borrow);

            }
            borrowTableView.setItems(borrowList);
            borrowTableView.setFixedCellSize(47);
            pagination.setPageCount((int) Math.ceil((double) getTotalBorrowCount(keyword) / rowsPerPage));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Creates a new page with the specified page index and loads borrow data.
     *
     * @param pageIndex the index of the page to be created. This index is used to determine the offset for loading borrow data.
     * @return a new Node representing the page, specifically a BorderPane in this case.
     */
    private Node createPage(int pageIndex) {
        currentPage = pageIndex;
        loadBorrows(searchTextField.getText().trim(), pageIndex);
        return new BorderPane();
    }

    /**
     * Opens the "Manage Book" scene within the application.
     *
     * This method switches the current scene to the "Manage Book" interface using
     * the {@code switchScene} utility method. The method is triggered as a JavaFX action
     * event and is linked to a specific UI control via the {@code @FXML} annotation.
     * It utilizes the {@code createBorrowButton} as a reference for the current window
     * before loading the new FXML layout for managing books.
     */
    @FXML
    private void openManageBookScene() {
        switchScene(createBorrowButton,"/fxml/admin/ManageBook.fxml");
    }

    /**
     * Opens the Manage User scene by switching the current scene to the
     * scene defined in the ManageUser.fxml file. This is triggered by the
     * associated FXML element, typically a button or menu item, within the
     * JavaFX application.
     */
    @FXML
    private void openManageUserScene() {
        switchScene(createBorrowButton,"/fxml/admin/ManageUser.fxml");
    }

    /**
     * Opens the administrative dashboard scene within the application.
     *
     * This method is linked to a specific JavaFX UI component via the `@FXML` annotation
     * and is responsible for navigating to the administrative dashboard by switching
     * the current scene to the one specified by the resource path "/fxml/admin/AdDashboard.fxml".
     *
     * The scene switch is facilitated by the static `switchScene` method, which changes the root node
     * of the current stage to the one defined in the specified FXML file.
     */
    @FXML
    private void openAdDashboardScene() {
        switchScene(createBorrowButton,"/fxml/admin/AdDashboard.fxml");
    }

    /**
     * Opens the Borrow Detail Scene, displaying detailed information about the specified borrow record.
     *
     * @param borrow the Borrow object containing details of the borrow record to be displayed
     */
    @FXML
    private void openBorrowDetailScene(Borrow borrow) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/BorrowDetail.fxml"));
            Parent root  = loader.load();
            BorrowDetailController borrowDetailController = loader.getController();
            borrowDetailController.setBorrow(borrow);
            Stage currentStage = (Stage) borrowTableView.getScene().getWindow();
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(borrowTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);
            });
            stage.showAndWait();
            loadBorrows(keyword,currentPage);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Opens the Delete Borrow scene to facilitate the deletion of a borrow record.
     * The method loads the "DeleteBorrow.fxml" user interface, applies visual effects
     * to the current stage to indicate modal interaction, and ensures that the
     * current borrow record details are passed to the DeleteBorrowController.
     * It pauses the execution of the application until the new window is closed.
     *
     * @param borrow the Borrow object representing the record to be potentially deleted.
     */
    @FXML
    private void openDeleteBorrowScene(Borrow borrow) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/DeleteBorrow.fxml"));
            Parent root  = loader.load();
            DeleteBorrowController deleteBorrowController = loader.getController();
            deleteBorrowController.setBorrow(borrow);
            Stage currentStage = (Stage) borrowTableView.getScene().getWindow();
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(borrowTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);
            });
            stage.showAndWait();
            loadBorrows(keyword,currentPage);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Opens the scene to update borrow details for a specified borrow record.
     * The scene is loaded from an FXML file and displayed in a new stage.
     *
     * @param borrow the borrow record that needs to be updated, which is passed to the controller of the new scene
     */
    @FXML
    private void openUpdateBorrowScene(Borrow borrow) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UpdateBorrow.fxml"));
            Parent root  = loader.load();
            UpdateBorrowController updateBorrowController = loader.getController();
            updateBorrowController.setBorrow(borrow);

            Stage stage = new Stage();

            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initOwner(borrowTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            // Hiển thị scene
            stage.showAndWait();
            loadBorrows(keyword,currentPage);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Opens a new modal scene for creating a new borrow record.
     * This method dims the current scene and displays the Create Borrow scene
     * as a modal dialog. It waits for the dialog to close before restoring the
     * brightness of the current scene. After the dialog is closed, the list of
     * borrows is reloaded to reflect any changes made.
     *
     * Exception Handling:
     * Any IOException that occurs during the loading of the FXML resource is
     * caught and its stack trace is printed.
     */
    @FXML
    private void openCreateBorrowScene() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/CreateBorrow.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) borrowTableView.getScene().getWindow();
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25);
            currentStage.getScene().getRoot().setEffect(colorAdjust);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initOwner(createBorrowButton.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(event -> {
                colorAdjust.setBrightness(0);
                currentStage.getScene().getRoot().setEffect(null);
            });
            stage.showAndWait();
            loadBorrows(keyword,currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the Profile Settings scene in the application. This method is
     * triggered by the associated JavaFX button. The scene switch is
     * facilitated by the switchScene method, which loads the
     * ProfileSettings.fxml file and sets it as the current root of the scene.
     */
    @FXML
    private void openProfileSettingsScene() {
        switchScene(createBorrowButton,"/fxml/admin/ProfileSettings.fxml");
    }

    /**
     * Opens the logout confirmation scene as a modal dialog. Adjusts the opacity of the main interface
     * to signal modal presence and handles the setup for the LogoutController.
     *
     * It sets the current window as the owner of the newly created dialog and positions the dialog in
     * the center of the current window. The dialog is styled without the usual window decorations.
     *
     * Updates the application state by calling the `loadBorrows` method once the dialog is closed.
     * If an exception occurs while loading the FXML file, it logs the stack trace for debugging purposes.
     */
    @FXML
    private void openLogOutScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Logout.fxml"));
            Parent root = loader.load();
            stackPaneRoot.setOpacity(0.45);
            Stage currentStage = (Stage) userNameUser.getScene().getWindow();
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
            loadBorrows(keyword, currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}