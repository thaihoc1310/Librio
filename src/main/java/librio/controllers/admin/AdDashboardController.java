package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import librio.controllers.auth.LogoutController;
import librio.database.DatabaseConnection;
import librio.util.DatabaseUtil;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static librio.util.DesignUtil.setAvatarAndUserName;
import static librio.util.DesignUtil.switchScene;

/**
 * The AdDashboardController class serves as the controller for the administrative dashboard
 * within the application. This class is responsible for initializing the dashboard interface,
 * updating and displaying statistical data related to books and users, and configuring visual
 * elements like charts and labels for a comprehensive user experience.
 *
 * Implements the Initializable interface to set up UI components and data upon loading.
 */
public class AdDashboardController implements Initializable {
    @FXML
    private Button openManageBookButton;
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


    /**
     * Initializes the AdDashboardController by setting up the user's avatar and username,
     * and populating the dashboard with current data for display in the cards and charts.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAvatarAndUserName(avatarUser, userNameUser);
        addDataToDashboardCardAndChart();
    }

    /**
     * Retrieves category data for a pie chart visualization. The method queries
     * the database to get the sum of book quantities grouped by category.
     * It calculates the percentage contribution of each category towards the total
     * quantity and creates a list of PieChart.Data instances representing this
     * information.
     *
     * @return a list of PieChart.Data objects where each object contains the category
     *         name with its percentage representation and the total quantity for that
     *         category in the dataset.
     */
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

    /**
     * Updates the dashboard by adding and displaying current data related
     * to books and user statistics on cards and charts within the dashboard interface.
     *
     * This method performs the following actions:
     *
     * 1. Retrieves statistical data related to books and users:
     *    - Total borrowed books.
     *    - Available books.
     *    - Combined total of borrowed and available books.
     *    - Total books in the database.
     *    - Total number of users.
     *
     * 2. Updates the labels on the dashboard cards with the retrieved data:
     *    - Updates the total books label.
     *    - Updates the total book copies label.
     *    - Updates the total users label.
     *    - Updates the total borrows label.
     *
     * 3. Configures and populates a pie chart:
     *    - Clears previous data and sets the label line length.
     *    - Retrieves and adds new category data to the chart.
     *    - Sets the legend visibility to false.
     *
     * 4. Configures and populates a bar chart displaying the number of books
     *    borrowed over the last 12 months:
     *    - Configures the X and Y axes with appropriate labels.
     *    - Sets chart title and legend visibility.
     *    - Populates the chart with monthly borrow counts from the past 12 months.
     *    - Adds the data series to the chart.
     */
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

    /**
     * Retrieves a list of month-year strings representing the last 12 months
     * from the current date. Each entry in the list is formatted as "MM-yyyy".
     *
     * @return a list of strings where each string represents a month and year
     *         in the format "MM-yyyy" for the past 12 months including the
     *         current month.
     */
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

    /**
     * Retrieves a list of formatted month-year labels for the past 12 months.
     * This method queries the database to fetch distinct year and month values
     * from the "borrows" table for entries dated within the last 12 months.
     * The results are grouped and ordered in descending order from the current date.
     *
     * @return a List of strings representing the month-year labels in the format "MM-YYYY",
     *         ordered from the most recent month to the earliest within the last 12 months.
     */
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

    /**
     * Retrieves the monthly borrow counts for the past 12 months from the database.
     * This method calculates the number of books borrowed each month over the last
     * year and returns a list containing these counts, ordered from the latest month
     * to the earliest.
     *
     * @return a list of integers where each integer represents the count of books
     *         borrowed for a specific month, ordered from the most recent month to
     *         the least recent within the last 12 months.
     */
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

    /**
     * Opens the Manage Book scene within the admin dashboard.
     * This method switches the current view to the ManageBook.fxml
     * UI when the associated button is clicked. It uses the
     * switchScene method to manage the transition, taking in the
     * button that initiated the action and the path to the FXML file
     * for the Manage Book interface.
     */
    @FXML
    private void openManageBookScene() {
        switchScene(openManageBookButton,"/fxml/admin/ManageBook.fxml");
    }

    /**
     * Opens the manage user scene for the admin dashboard. This method switches
     * the current view to the ManageUser.fxml scene when the associated button
     * is clicked. It utilizes the switchScene method to handle the scene transition,
     * passing the button that triggered the action and the path to the FXML resource.
     */
    @FXML
    private void openManageUserScene() {
        switchScene(openManageBookButton,"/fxml/admin/ManageUser.fxml");
    }

    /**
     * Opens the manage borrow scene within the admin dashboard. This method is
     * triggered by interacting with the openManageBookButton. It utilizes the
     * switchScene method to change the current view to the ManageBorrow.fxml file,
     * ensuring a seamless transition within the application.
     */
    @FXML
    private void openManageBorrowScene() {
        switchScene(openManageBookButton,"/fxml/admin/ManageBorrow.fxml");
    }

    /**
     * Opens the profile settings scene for the admin dashboard. This method
     * switches the current view to the ProfileSettings.fxml scene when the
     * associated button is clicked. It utilizes the switchScene method to
     * handle the scene transition, passing the button that triggered the
     * action and the path to the FXML resource.
     */
    @FXML
    private void openProfileSettingsScene() {
        switchScene(openManageBookButton,"/fxml/admin/ProfileSettings.fxml");
    }

    /**
     * Opens the logout scene in a modal window. The method loads the Logout.fxml
     * file, initializes the LogoutController, and displays the logout window as a
     * modal dialog centered over the current window. It sets the opacity of the
     * main application window to 0.45 while the logout window is active.
     * When the logout window is closed, the main application window regains full
     * opacity.
     *
     * This method catches and prints any IOException that occurs during the
     * loading of the FXML file.
     */
    @FXML
    private void openLogOutScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Logout.fxml"));
            Parent root = loader.load();
            stackPaneRoot.setOpacity(0.45);
            Stage currentStage = (Stage) openManageBookButton.getScene().getWindow();

            LogoutController logoutController = loader.getController();
            logoutController.setOwnerStage(currentStage);
            logoutController.setStackPaneRoot(stackPaneRoot);
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
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
