package librio.database;

import java.sql.*;

public class DatabaseInitializer {
    public static boolean isTableEmpty(String tableName) throws SQLException {
        String checkEmptyQuery = "SELECT 1 FROM " + tableName + " LIMIT 1";
        try (PreparedStatement preparedStatement = DatabaseConnection.getConnection().prepareStatement(checkEmptyQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            return !resultSet.next();  // Nếu có dòng, resultSet.next() trả về true, nghĩa là bảng không trống
        }
    }

    // Phương thức để tự động tạo bảng nếu chưa tồn tại
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
                    "member_id INT," +
                    "book_isbn VARCHAR(255)," +
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
                    "rating INT," +
                    "about VARCHAR(255)," +
                    "created_by VARCHAR(255)," +
                    "created_at TIMESTAMP," +
                    "updated_by VARCHAR(255)," +
                    "updated_at TIMESTAMP," +
                    "FOREIGN KEY (book_id) REFERENCES Books(id)," +
                    "FOREIGN KEY (member_id) REFERENCES Members(id)" +
                    ");";


            // Execute the SQL statements
            statement.execute(createUserTable);
            statement.execute(createBookTable);
            statement.execute(createMemberTable);
            statement.execute(createLibrarianTable);
            statement.execute(createBorrowTable);
            statement.execute(createFeedbackTable);

            if (isTableEmpty("Users") && isTableEmpty("Books")) {
                String resetAutoIncrementUser = "ALTER TABLE Users AUTO_INCREMENT = 1";
                statement.execute(resetAutoIncrementUser);
                String insertUsers = "INSERT INTO Users (name, email, password, phone_number, address, birth_of_date, gender, role, created_by, created_at) VALUES " +
                        "('John Doe', 'john.doe@example.com', 'password123', '1234567890', '123 Main St', '1999-01-01', 'MALE', 'MEMBER', 'admin@example.com', CURRENT_TIMESTAMP)," +
                        "('Jane Smith', 'jane.smith@example.com', 'password123', '0987654321', '456 Elm St', '2005-08-02', 'FEMALE', 'MEMBER', 'admin@example.com', CURRENT_TIMESTAMP)," +
                        "('Alice Johnson', 'alice.johnson@example.com', 'password123', '1112223333', '789 Maple St', '1980-05-21', 'FEMALE', 'MEMBER', 'admin@example.com', CURRENT_TIMESTAMP)," +
                        "('Bob Brown', 'bob.brown@example.com', 'password123', '4445556666', '101 Pine St', '2000-12-11', 'MALE', 'MEMBER', 'admin@example.com', CURRENT_TIMESTAMP)," +
                        "('Hoc Admin', 'admin@example.com', '111111', '0344281310', 'HA NOI', '2005-10-13', 'MALE', 'LIBRARIAN', 'admin@example.com', CURRENT_TIMESTAMP);";
                statement.execute(insertUsers);

                String resetAutoIncrementBook = "ALTER TABLE Books AUTO_INCREMENT = 1";
                statement.execute(resetAutoIncrementBook);
                String insertBooks = "INSERT INTO Books (title, author, isbn, publisher, category, quantity_copy, average_of_rating, created_by, created_at, year_published, language, number_of_pages, book_image, description) VALUES " +
                        "('Effective Java', 'Joshua Bloch', '9780134685991', 'Addison-Wesley', 'Programming', 5, 5, 'admin@example.com', CURRENT_TIMESTAMP, 2008, 'English', 416, NULL, 'A comprehensive guide to best practices in Java programming. This book covers in-depth principles for writing effective Java code. It includes multiple chapters on best practices for designing robust and maintainable systems.')," +
                        "('Clean Code', 'Robert C. Martin', '9780132350884', 'Prentice Hall', 'Programming', 3, 4, 'admin@example.com', CURRENT_TIMESTAMP, 2008, 'English', 464, NULL, 'A handbook of agile software craftsmanship. The book emphasizes the importance of writing clean, readable code. Through practical examples, readers learn how to refactor messy code into clean and understandable designs.')," +
                        "('The Pragmatic Programmer', 'Andrew Hunt', '9780201616224', 'Addison-Wesley', 'Programming', 5, 4, 'admin@example.com', CURRENT_TIMESTAMP, 1999, 'English', 352, NULL, 'From Journeyman to Master - a practical guide for programmers. This book provides practical advice on software development, covering a range of topics from debugging techniques to project management. It’s an essential read for any aspiring software engineer.')," +
                        "('Design Patterns', 'Erich Gamma', '9780201633610', 'Addison-Wesley', 'Software Engineering', 2, 0, 'admin@example.com', CURRENT_TIMESTAMP, 1994, 'English', 395, NULL, 'Elements of Reusable Object-Oriented Software. This book introduces classic design patterns that are widely used in software development. It helps developers understand how to apply reusable solutions to common design problems.')," +
                        "('Refactoring', 'Martin Fowler', '9780201485677', 'Addison-Wesley', 'Software Engineering', 3, 0, 'admin@example.com', CURRENT_TIMESTAMP, 1999, 'English', 464, NULL, 'Improving the design of existing code. Refactoring helps developers clean up messy codebases and make them more maintainable. The book emphasizes best practices for enhancing code quality.')," +
                        "('Code Complete', 'Steve McConnell', '9780735619678', 'Microsoft Press', 'Programming', 4, 0, 'admin@example.com', CURRENT_TIMESTAMP, 2004, 'English', 960, NULL, 'A practical handbook of software construction. This extensive guide provides detailed insight into coding best practices. Topics such as debugging, testing, and performance optimization are thoroughly covered.')," +
                        "('Introduction to Algorithms', 'Thomas H. Cormen', '9780262033848', 'MIT Press', 'Algorithms', 6, 0, 'admin@example.com', CURRENT_TIMESTAMP, 2009, 'English', 1312, NULL, 'The most comprehensive introduction to algorithms. This book provides in-depth coverage of many important algorithms. It’s widely used in computer science courses and is known for its clarity and rigor.')," +
                        "('Java Concurrency in Practice', 'Brian Goetz', '9780321349606', 'Addison-Wesley', 'Programming', 2, 0, 'admin@example.com', CURRENT_TIMESTAMP, 2006, 'English', 384, NULL, 'A definitive guide to concurrency in Java. The book explains the complexities of concurrent programming. Readers will learn practical techniques for writing thread-safe and efficient Java programs.')," +
                        "('The Mythical Man-Month', 'Frederick P. Brooks Jr.', '9780201835953', 'Addison-Wesley', 'Software Engineering', 3, 0, 'admin@example.com', CURRENT_TIMESTAMP, 1975, 'English', 322, NULL, 'Essays on software engineering. This book discusses the pitfalls of large-scale software project management. It emphasizes the challenges of coordination and communication in software teams.')," +
                        "('You Dont Know JS', 'Kyle Simpson', '9781491904244', 'O Reilly Media', 'JavaScript', 5, 0, 'admin@example.com', CURRENT_TIMESTAMP, 2014, 'English', 278, NULL, 'A series on JavaScript programming. This book delves into the intricacies of JavaScript. It provides a deep understanding of how the language works, from the basics to advanced topics.')," +
                        "('Domain-Driven Design', 'Eric Evans', '9780321125217', 'Addison-Wesley', 'Software Design', 2, 0, 'admin@example.com', CURRENT_TIMESTAMP, 2003, 'English', 560, NULL, 'Tackling complexity in the heart of software. This book provides insight into designing complex software systems. It focuses on aligning business goals with technical implementation.')," +
                        "('Clean Architecture', 'Robert C. Martin', '9780134494166', 'Prentice Hall', 'Software Architecture', 3, 0, 'admin@example.com', CURRENT_TIMESTAMP, 2017, 'English', 432, NULL, 'A guide to creating sustainable architecture for software systems. This book discusses the principles of building robust software architectures. It covers topics such as modularity, design patterns, and architectural practices.');";
                statement.execute(insertBooks);

                String insertMembers = "INSERT INTO Members (id, fine_amount, total_books_borrowed) VALUES " +
                        "(1, 0, 4)," +
                        "(2, 0, 3)," +
                        "(3, 50000, 2)," +
                        "(4, 0, 1);";
                statement.execute(insertMembers);

                String insertLibrarians = "INSERT INTO Librarians (id) VALUES (5);";
                statement.execute(insertLibrarians);

                String insertBorrows = "INSERT INTO Borrows (member_id, book_isbn, borrow_date, due_date, return_date, status, fine, created_by, created_at) VALUES " +
                        "(1, 9780134685991, CURRENT_TIMESTAMP - INTERVAL 10 DAY, CURRENT_TIMESTAMP + INTERVAL 4 DAY, CURRENT_TIMESTAMP, 'RETURNED', 0, 'admin@example.com', CURRENT_TIMESTAMP)," +
                        "(2, 9780132350884, CURRENT_TIMESTAMP - INTERVAL 10 DAY, CURRENT_TIMESTAMP + INTERVAL 4 DAY, CURRENT_TIMESTAMP, 'RETURNED', 0, 'admin@example.com', CURRENT_TIMESTAMP)," +
                        "(3, 9780201616224, CURRENT_TIMESTAMP - INTERVAL 15 DAY, CURRENT_TIMESTAMP - INTERVAL 1 DAY, CURRENT_TIMESTAMP, 'RETURNED_LATE', 50000, 'admin@example.com', CURRENT_TIMESTAMP)," +
                        "(1, 9780201633610, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 14 DAY, NULL, 'BORROWING', NULL, 'admin@example.com', CURRENT_TIMESTAMP)," +
                        "(2, 9780201485677, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 14 DAY, NULL, 'BORROWING', NULL, 'admin@example.com', CURRENT_TIMESTAMP)," +
                        "(3, 9780735619678, CURRENT_TIMESTAMP - INTERVAL 15 DAY, CURRENT_TIMESTAMP - INTERVAL 1 DAY , NULL, 'OVERDUE', NULL, 'admin@example.com', CURRENT_TIMESTAMP)," +
                        "(1, 9780262033848, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 14 DAY, NULL, 'BORROWING', NULL, 'admin@example.com', CURRENT_TIMESTAMP)," +
                        "(4, 9780321349606, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 14 DAY, NULL, 'BORROWING', NULL, 'admin@example.com', CURRENT_TIMESTAMP)," +
                        "(2, 9780201835953, CURRENT_TIMESTAMP - INTERVAL 15 DAY, CURRENT_TIMESTAMP - INTERVAL 1 DAY , NULL, 'OVERDUE', NULL, 'admin@example.com', CURRENT_TIMESTAMP)," +
                        "(1, 9781491904244, CURRENT_TIMESTAMP - INTERVAL 15 DAY, CURRENT_TIMESTAMP - INTERVAL 1 DAY, CURRENT_TIMESTAMP, 'RETURNED_LATE', NULL, 'admin@example.com', CURRENT_TIMESTAMP);";
                statement.execute(insertBorrows);

                String insertFeedbacks = "INSERT INTO Feedbacks (book_id, member_id, rating, about, created_by, created_at) VALUES " +
                        "(1, 1, 5, 'Great book for learning Java!', 'john.doe@example.com', CURRENT_TIMESTAMP)," +
                        "(2, 2, 4, 'Very helpful for clean coding practices.', 'jane.smith@example.com', CURRENT_TIMESTAMP)," +
                        "(3, 3, 5, 'Excellent resource for programmers.', 'alice.johnson@example.com', CURRENT_TIMESTAMP);";
                statement.execute(insertFeedbacks);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
