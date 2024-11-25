package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import librio.cache.ImageCache;
import librio.controllers.auth.LogoutController;
import librio.session.Session;
import librio.database.DatabaseConnection;
import librio.util.DatabaseUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static librio.util.DesignUtil.cropAndClipToCircle;

public class AdDashboardController implements Initializable {

    @FXML
    Button openManageBookButton;

    @FXML
    private ImageView avatarUser;

    @FXML
    private Label userNameUser;

    @FXML
    private StackPane stackPaneRoot;

    @FXML
    private Label totalBooksLabel;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label totalBorrowsLabel;

    @FXML
    private Label totalBookCopyLabel;

    @FXML
    private PieChart pieChart;

    @FXML
    private BarChart<String, Number> barChart;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAvatarAndUserName();
        addDataToDashboardCardAndChart();
    }

    private List<PieChart.Data> getCategoryData() {
        List<PieChart.Data> categoryData = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT category, SUM(quantity_copy) AS quantity FROM books GROUP BY category";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            int totalQuantity = 0;
            Map<String, Integer> categoryQuantities = new HashMap<>();

            while (resultSet.next()) {
                String category = resultSet.getString("category");
                int quantity = resultSet.getInt("quantity");
                categoryQuantities.put(category, quantity);
                totalQuantity += quantity;
            }

            for (Map.Entry<String, Integer> entry : categoryQuantities.entrySet()) {
                String category = entry.getKey();
                int quantity = entry.getValue();
                double percentage = ((double) quantity / totalQuantity) * 100;


                PieChart.Data data = new PieChart.Data(category + ": " + String.format("%.2f", percentage) + "%", quantity);
                categoryData.add(data);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryData;
    }

    public void addDataToDashboardCardAndChart() {
        int borrowedBooks = DatabaseUtil.getTotalBorrowedBooks();
        int availableBooks = DatabaseUtil.getAvailableBooks();
        int totalCopyBooks = borrowedBooks + availableBooks;
        int totalBooks = DatabaseUtil.getTotalBooks();
        int totalUsers = DatabaseUtil.getTotalUsers();

        //Add data to Card
        totalBooksLabel.setText(String.valueOf(totalBooks));
        totalBookCopyLabel.setText(String.valueOf(totalCopyBooks));
        totalUsersLabel.setText(String.valueOf(totalUsers));
        totalBorrowsLabel.setText(String.valueOf(borrowedBooks));

        //Add data to Pie Chart
        pieChart.getData().clear();
        pieChart.setLabelLineLength(20);
        List<PieChart.Data> categoryData = getCategoryData();
        pieChart.getData().addAll(categoryData);
        pieChart.setLegendVisible(false);



        //Add data to bar Chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month-Year");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Books Borrowed");

        barChart.setTitle("Books Borrowed in Last 12 Months");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        barChart.setLegendVisible(false);

        List<String> monthYearLabels = getMonthYearLabels();
        List<Integer> borrowCounts = getBorrowCounts();

        List<String> allMonths = getAllMonthsInLast12Months();
        Collections.reverse(allMonths);

        for (String month : allMonths) {
            int borrowCount = 0;
            int index = monthYearLabels.indexOf(month);
            if (index != -1) {
                borrowCount = borrowCounts.get(index);
            }
            series.getData().add(new XYChart.Data<>(month, borrowCount));
        }

        // Thêm series vào BarChart
        barChart.getData().add(series);
    }

    //Take months
    private List<String> getAllMonthsInLast12Months() {
        List<String> months = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        for (int i = 0; i < 12; i++) {
            LocalDate monthDate = currentDate.minusMonths(i);
            String monthYear = monthDate.format(DateTimeFormatter.ofPattern("MM-yyyy"));
            months.add(monthYear);
        }
        return months;
    }

    //Query to get month, year, borrows data;
    private List<String> getMonthYearLabels() {
        List<String> monthYearLabels = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT YEAR(borrow_date) AS year, MONTH(borrow_date) AS month " +
                    "FROM borrows " +
                    "WHERE borrow_date >= CURDATE() - INTERVAL 12 MONTH " +
                    "GROUP BY YEAR(borrow_date), MONTH(borrow_date) " +
                    "ORDER BY year DESC, month DESC";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int year = resultSet.getInt("year");
                int month = resultSet.getInt("month");
                String monthYear = String.format("%02d-%d", month, year);
                monthYearLabels.add(monthYear);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return monthYearLabels;
    }

    private List<Integer> getBorrowCounts() {
        List<Integer> borrowCounts = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) AS borrow_count " +
                    "FROM borrows " +
                    "WHERE borrow_date >= CURDATE() - INTERVAL 12 MONTH " +
                    "GROUP BY YEAR(borrow_date), MONTH(borrow_date) " +
                    "ORDER BY YEAR(borrow_date) DESC, MONTH(borrow_date) DESC";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                borrowCounts.add(resultSet.getInt("borrow_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return borrowCounts;
    }


    public void setAvatarAndUserName() {
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + Session.getInstance().getLoggedInUser().getAvatar();

        Image image = ImageCache.getInstance().getImage(path,avatarsDir + "Male User.png");
        avatarUser.setImage(image);
        userNameUser.setText(Session.getInstance().getLoggedInUser().getName());
    }

    @FXML
    private void openManageBookScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageBook.fxml"));
            Parent manageBookRoot = loader.load();

            Stage currentStage = (Stage) openManageBookButton.getScene().getWindow();
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

            Stage currentStage = (Stage) openManageBookButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageUserRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openManageBorrowScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageBorrow.fxml"));
            Parent manageBorrowRoot = loader.load();

            Stage currentStage = (Stage) openManageBookButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBorrowRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openProfileSettingsScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ProfileSettings.fxml"));
            Parent manageBorrowRoot = loader.load();

            Stage currentStage = (Stage) openManageBookButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBorrowRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openLogOutScene() {
        try {
            // Tải FXML của scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Logout.fxml"));
            Parent root = loader.load();
            stackPaneRoot.setOpacity(0.45);
            Stage currentStage = (Stage) openManageBookButton.getScene().getWindow();

            LogoutController logoutController = loader.getController();
            logoutController.setOwnerStage(currentStage);
            logoutController.setStackPaneRoot(stackPaneRoot);
            // Tạo stage mới cho scene
            Stage stage = new Stage();
            stage.setTitle("Logout");
            stage.setScene(new Scene(root));
            Rectangle clip = new Rectangle();
            clip.setWidth(424);
            clip.setHeight(204);
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            root.setClip(clip);
            stage.setResizable(false);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initOwner(currentStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnShown(event -> {
                stage.setX(currentStage.getX() + (currentStage.getWidth() - stage.getWidth()) / 2);
                stage.setY(currentStage.getY() + (currentStage.getHeight() - stage.getHeight()) / 2);
            });
            // Hiển thị scene
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
