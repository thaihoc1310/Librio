package librio.database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3308/librio";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Huyd2106@";
    //Huyd2106@
    //thaihoc285
    //Giang2002@
    // Phương thức kết nối cơ sở dữ liệu
    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        return connection;
    }
}
