package librio.database;

import java.sql.*;

public class DatabaseInitializer {
    public static void initializeDatabase() {
        try (Connection connection = DatabaseConnection.getConnection(); Statement statement = connection.createStatement()) {
            String createUserTable = "CREATE TABLE IF NOT EXISTS Users (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "name VARCHAR(255) NOT NULL," +
                    "email VARCHAR(255) UNIQUE NOT NULL," +
                    "password VARCHAR(255) NOT NULL," +
                    "phone_number VARCHAR(20)," +
                    "address VARCHAR(255)," +
                    "birth_of_date DATE," +
                    "gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL," +
                    "role ENUM('MEMBER', 'LIBRARIAN') NOT NULL," +
                    "avatar VARCHAR(255)," +
                    "created_by VARCHAR(255)," +
                    "created_at TIMESTAMP," +
                    "updated_by VARCHAR(255)," +
                    "updated_at TIMESTAMP" +
                    ");";

            String createBookTable = "CREATE TABLE IF NOT EXISTS Books (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "title VARCHAR(255) NOT NULL," +
                    "author VARCHAR(255)," +
                    "isbn VARCHAR(255) UNIQUE NOT NULL," +
                    "publisher VARCHAR(255)," +
                    "category VARCHAR(255)," +
                    "quantity_copy INT," +
                    "available_copy INT DEFAULT 0," +
                    "average_of_rating DOUBLE," +
                    "year_published INT," +
                    "language VARCHAR(50)," +
                    "number_of_pages INT," +
                    "description TEXT," +
                    "book_image VARCHAR(255)," +
                    "created_by VARCHAR(255)," +
                    "created_at TIMESTAMP," +
                    "updated_by VARCHAR(255)," +
                    "updated_at TIMESTAMP" +
                    ");";


            String createMemberTable = "CREATE TABLE IF NOT EXISTS Members (" +
                    "id INT PRIMARY KEY," +
                    "fine_amount BIGINT," +
                    "total_books_borrowed BIGINT," +
                    "FOREIGN KEY (id) REFERENCES Users(id)" +
                    ");";

            String createLibrarianTable = "CREATE TABLE IF NOT EXISTS Librarians (" +
                    "id INT PRIMARY KEY," +
                    "FOREIGN KEY (id) REFERENCES Users(id)" +
                    ");";

            String createBorrowTable = "CREATE TABLE IF NOT EXISTS Borrows (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "member_id INT NOT NULL," +
                    "book_isbn VARCHAR(255) NOT NULL," +
                    "borrow_date DATE," +
                    "due_date DATE," +
                    "return_date DATE," +
                    "status ENUM('RETURNED', 'BORROWING', 'OVERDUE', 'RETURNED_LATE') NOT NULL," +
                    "fine DOUBLE," +
                    "created_by VARCHAR(255)," +
                    "created_at TIMESTAMP," +
                    "updated_by VARCHAR(255)," +
                    "updated_at TIMESTAMP," +
                    "FOREIGN KEY (book_isbn) REFERENCES Books(isbn)," +
                    "FOREIGN KEY (member_id) REFERENCES Members(id)" +
                    ");";


            String createFeedbackTable = "CREATE TABLE IF NOT EXISTS Feedbacks (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "book_id INT," +
                    "member_id INT," +
                    "borrow_id INT," +
                    "rating INT," +
                    "about VARCHAR(255)," +
                    "created_by VARCHAR(255)," +
                    "created_at TIMESTAMP," +
                    "updated_by VARCHAR(255)," +
                    "updated_at TIMESTAMP," +
                    "FOREIGN KEY (book_id) REFERENCES Books(id)," +
                    "FOREIGN KEY (member_id) REFERENCES Members(id)," +
                    "FOREIGN KEY (borrow_id) REFERENCES Borrows(id)" +
                    ");";

            String createNotificationTable = "CREATE TABLE IF NOT EXISTS Notifications (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "member_id INT NOT NULL," +
                    "borrow_id INT NOT NULL," +
                    "notification_type ENUM('BORROW_SUCCESS', 'OVERDUE', 'RETURN_SUCCESS', 'ALMOST_DUE') NOT NULL," +
                    "message TEXT NOT NULL," +
                    "is_read BOOLEAN DEFAULT FALSE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (member_id) REFERENCES Members(id)," +
                    "FOREIGN KEY (borrow_id) REFERENCES Borrows(id)" +
                    ");";

            statement.execute(createUserTable);
            statement.execute(createBookTable);
            statement.execute(createMemberTable);
            statement.execute(createLibrarianTable);
            statement.execute(createBorrowTable);
            statement.execute(createFeedbackTable);
            statement.execute(createNotificationTable);

            if (isTableEmpty("Users")) {
                String resetAutoIncrementUser = "ALTER TABLE Users AUTO_INCREMENT = 1";
                statement.execute(resetAutoIncrementUser);
                String insertUsers = "INSERT INTO Users (name, email, password, phone_number, address, birth_of_date, gender, role, created_by, created_at) VALUES " +
                        "('Hoc Admin', 'admin@example.com', '111111', '0344281310', 'HA NOI', '2005-10-13', 'MALE', 'LIBRARIAN', 'admin@example.com', CURRENT_TIMESTAMP);";
                statement.execute(insertUsers);
                String insertLibrarians = "INSERT INTO Librarians (id) VALUES (1);";
                statement.execute(insertLibrarians);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isTableEmpty(String tableName) throws SQLException {
        String checkEmptyQuery = "SELECT 1 FROM " + tableName + " LIMIT 1";
        try (PreparedStatement preparedStatement = DatabaseConnection.getConnection().prepareStatement(checkEmptyQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            return !resultSet.next();
        }
    }
}