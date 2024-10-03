package librio.database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/librio";  // URL của cơ sở dữ liệu (localhost và cổng 3306 là mặc định cho MySQL)
    private static final String USERNAME = "root";  // Tên đăng nhập vào cơ sở dữ liệu
    private static final String PASSWORD = "thaihoc285";  // Mật khẩu của cơ sở dữ liệu

    // Phương thức kết nối cơ sở dữ liệu
    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        return connection;
    }
}
