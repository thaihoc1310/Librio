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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import librio.controllers.auth.Session;
import librio.database.DatabaseConnection;
import librio.models.Borrow;
import librio.models.Status;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.getBorrowById;
import static librio.util.DatabaseUtil.getTotalBorrowCount;
import static librio.util.DesignUtil.cropAndClipToCircle;

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
    private ImageView avatarUser;
    @FXML
    private Label userNameUser;
    private ObservableList<Borrow> borrowList;

    private int currentPage = 0;
    private final int rowsPerPage = 11;

    private String keyword = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAvatarAndUserName();
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
                            Borrow dbBorrow = getBorrowById(borrow.getId());
                            openBorrowDetailScene(dbBorrow);
                        });

                        btnUpdate.setOnAction(event -> {
                            Borrow borrow = getTableView().getItems().get(getIndex());
                            Borrow dbBorrow = getBorrowById(borrow.getId());
                            openUpdateBorrowScene(dbBorrow);
                        });

                        btnDelete.setOnAction(event -> {
                            Borrow borrow = getTableView().getItems().get(getIndex());
                            Borrow dbBorrow = getBorrowById(borrow.getId());
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
                query = "SELECT * FROM borrows WHERE status LIKE ? OR book_isbn LIKE ? LIMIT ? OFFSET ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, "%" + keyword + "%");
                statement.setString(2, "%" + keyword + "%");
                statement.setInt(3, rowsPerPage);
                statement.setInt(4, offset);
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String borrowId = resultSet.getString("id");
                String email = resultSet.getString("email");
                String bookIsbn = resultSet.getString("book_isbn");
                LocalDate borrowDate = resultSet.getDate("borrow_date").toLocalDate();
                LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();
                LocalDate returnDate = resultSet.getDate("return_date") != null ? resultSet.getDate("return_date").toLocalDate() : null;
                Double fine = resultSet.getDouble("fine");
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


    private Node createPage(int pageIndex) {
        currentPage = pageIndex;
        loadBorrows(searchTextField.getText().trim(), pageIndex);
        return new BorderPane();
    }

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

    @FXML
    private void openBorrowDetailScene(Borrow borrow) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/BorrowDetail.fxml"));
            Parent root  = loader.load();
            BorrowDetailController borrowDetailController = loader.getController();
            borrowDetailController.setBorrow(borrow);

            Stage stage = new Stage();
            stage.setTitle("Open Borrow Delete Scene");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initOwner(borrowTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            // Hiển thị scene
            stage.showAndWait();
            loadBorrows(keyword,currentPage);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    private void openDeleteBorrowScene(Borrow borrow) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/DeleteBorrow.fxml"));
            Parent root  = loader.load();
            DeleteBorrowController deleteBorrowController = loader.getController();
            deleteBorrowController.setBorrow(borrow);

            Stage stage = new Stage();
            stage.setTitle("Open Borrow Detail");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initOwner(borrowTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            // Hiển thị scene
            stage.showAndWait();
            loadBorrows(keyword,currentPage);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    private void openUpdateBorrowScene(Borrow borrow) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UpdateBorrow.fxml"));
            Parent root  = loader.load();
            UpdateBorrowController updateBorrowController = loader.getController();
            updateBorrowController.setBorrow(borrow);

            Stage stage = new Stage();
            stage.setTitle("Update Borrow");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.initOwner(borrowTableView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            // Hiển thị scene
            stage.showAndWait();
            loadBorrows(keyword,currentPage);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
