package librio.util;

import javafx.scene.control.ComboBox;
import librio.database.DatabaseConnection;
import librio.enums.Gender;
import librio.enums.Role;
import librio.enums.Status;
import librio.models.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class DatabaseUtil {

    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static boolean isEmailExists(String email) {
        boolean exists = false;
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                exists = resultSet.getInt(1) > 0;
                //resultSet.getInt => get result of count(*)
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exists;
    }

    public static User getUserById(String userId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String id = resultSet.getString("id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");
                String phoneNumber = resultSet.getString("phone_number");
                String address = resultSet.getString("address");
                Gender gender = Gender.valueOf(resultSet.getString("gender").toUpperCase());
                Role role = Role.valueOf(resultSet.getString("role").toUpperCase());
                String avatar = resultSet.getString("avatar");
                LocalDate birthOfDate = resultSet.getDate("birth_of_date").toLocalDate();
                return new User(id, name, email, password, phoneNumber, address, gender, role, avatar, birthOfDate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User getUserByEmail(String userEmail) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE email = ?")) {
            statement.setString(1, userEmail);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String id = resultSet.getString("id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");
                String phoneNumber = resultSet.getString("phone_number");
                String address = resultSet.getString("address");
                Gender gender = Gender.valueOf(resultSet.getString("gender").toUpperCase());
                Role role = Role.valueOf(resultSet.getString("role").toUpperCase());
                String avatar = resultSet.getString("avatar");
                LocalDate birthOfDate = resultSet.getDate("birth_of_date").toLocalDate();
                return new User(id, name, email, password, phoneNumber, address, gender, role, avatar, birthOfDate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Book getBookByIsbn(String bookIsbn) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM books WHERE isbn = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, bookIsbn);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Integer id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String isbn = resultSet.getString("isbn");
                String publisher = resultSet.getString("publisher");
                String category = resultSet.getString("category");
                Integer quantityCopy = resultSet.getInt("quantity_copy");
                Integer availableCopy = resultSet.getInt("available_copy");
                Double averageOfRating = resultSet.getDouble("average_of_rating");
                String yearPublished = resultSet.getString("year_published");
                String language = resultSet.getString("language");
                String numberOfPages = resultSet.getString("number_of_pages");
                String description = resultSet.getString("description");
                String bookImage = resultSet.getString("book_image");
                return new Book(id, title, author, isbn, category, publisher, quantityCopy, availableCopy, averageOfRating, yearPublished, language, numberOfPages, description, bookImage);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteBorrow(Borrow borrow) {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            String checkBorrowQuery = "SELECT id, status, book_isbn FROM Borrows WHERE id = ?";
            try (PreparedStatement checkBorrowStmt = connection.prepareStatement(checkBorrowQuery)) {
                checkBorrowStmt.setInt(1, borrow.getId());
                ResultSet borrowResult = checkBorrowStmt.executeQuery();

                if (borrowResult.next()) {
                    String status = borrowResult.getString("status");
                    String bookIsbn = borrowResult.getString("book_isbn");


                    String deleteFeedbackQuery = "DELETE FROM Feedbacks WHERE borrow_id = ?";
                    try (PreparedStatement deleteFeedbackStmt = connection.prepareStatement(deleteFeedbackQuery)) {
                        deleteFeedbackStmt.setInt(1, borrow.getId());
                        deleteFeedbackStmt.executeUpdate();
                    }

                    if (status.equals("BORROWING") || status.equals("OVERDUE")) {
                        String updateBookQuery = "UPDATE Books SET available_copy = available_copy + 1 WHERE isbn = ?";
                        try (PreparedStatement updateBookStmt = connection.prepareStatement(updateBookQuery)) {
                            updateBookStmt.setString(1, bookIsbn);
                            updateBookStmt.executeUpdate();
                        }
                    }

                    String deleteBorrowQuery = "DELETE FROM Borrows WHERE id = ?";
                    try (PreparedStatement deleteBorrowStmt = connection.prepareStatement(deleteBorrowQuery)) {
                        deleteBorrowStmt.setInt(1, borrow.getId());
                        int rowsAffected = deleteBorrowStmt.executeUpdate();

                        if (rowsAffected > 0) {
                            System.out.println("Deleted Borrow with ID: " + borrow.getId());
                        }
                    }
                } else {
                    System.out.println("No Borrow found with ID: " + borrow.getId());
                }
            }

            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException closeEx) {
                closeEx.printStackTrace();
            }
        }
    }


    public static void deleteUser(User user) {
        List<String> bookIsbn = new ArrayList<>();
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            String checkMemberQuery = "SELECT id FROM Members WHERE id = ?";
            try (PreparedStatement checkMemberStmt = connection.prepareStatement(checkMemberQuery)) {
                checkMemberStmt.setString(1, user.getId());
                ResultSet memberResult = checkMemberStmt.executeQuery();

                if (memberResult.next()) {

                    String getIsbnQuery = "SELECT b.isbn FROM books b " +
                            "JOIN borrows br ON b.isbn = br.book_isbn " +
                            "WHERE br.member_id = ?";
                    try (PreparedStatement getIsbnStmt = connection.prepareStatement(getIsbnQuery)) {
                        getIsbnStmt.setString(1, user.getId());
                        ResultSet isbnResult = getIsbnStmt.executeQuery();

                        while (isbnResult.next()) {
                            bookIsbn.add(isbnResult.getString("isbn"));
                        }
                    }

                    String deleteFeedbackQuery = "DELETE FROM Feedbacks WHERE borrow_id IN (SELECT id FROM Borrows WHERE member_id = ?)";
                    try (PreparedStatement deleteFeedbackStmt = connection.prepareStatement(deleteFeedbackQuery)) {
                        deleteFeedbackStmt.setString(1, user.getId());
                        deleteFeedbackStmt.executeUpdate();
                    }

                    String deleteBorrowQuery = "DELETE FROM Borrows WHERE member_id = ?";
                    try (PreparedStatement deleteBorrowStmt = connection.prepareStatement(deleteBorrowQuery)) {
                        deleteBorrowStmt.setString(1, user.getId());
                        deleteBorrowStmt.executeUpdate();
                    }

                    String deleteMemberQuery = "DELETE FROM Members WHERE id = ?";
                    try (PreparedStatement deleteMemberStmt = connection.prepareStatement(deleteMemberQuery)) {
                        deleteMemberStmt.setString(1, user.getId());
                        deleteMemberStmt.executeUpdate();
                    }
                }
            }

            // Kiểm tra xem user có phải là thủ thư không
            String checkLibrarianQuery = "SELECT id FROM Librarians WHERE id = ?";
            try (PreparedStatement checkLibrarianStmt = connection.prepareStatement(checkLibrarianQuery)) {
                checkLibrarianStmt.setString(1, user.getId());
                ResultSet librarianResult = checkLibrarianStmt.executeQuery();

                if (librarianResult.next()) {
                    // Xóa thông tin thủ thư
                    String deleteLibrarianQuery = "DELETE FROM Librarians WHERE id = ?";
                    try (PreparedStatement deleteLibrarianStmt = connection.prepareStatement(deleteLibrarianQuery)) {
                        deleteLibrarianStmt.setString(1, user.getId());
                        deleteLibrarianStmt.executeUpdate();
                    }
                }
            }

            String deleteUserQuery = "DELETE FROM Users WHERE id = ?";
            try (PreparedStatement deleteUserStmt = connection.prepareStatement(deleteUserQuery)) {
                deleteUserStmt.setString(1, user.getId());
                int rowsAffected = deleteUserStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Đã xóa người dùng có ID: " + user.getId());
                } else {
                    System.out.println("Không thể xóa người dùng có ID: " + user.getId());
                }
            }

            connection.commit();
            for (String isbn : bookIsbn) {
                System.out.println(isbn);
                updateBookAverageRating(isbn);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }



    public static void deleteBook(Book book) {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            String deleteFeedbackQuery = "DELETE FROM Feedbacks WHERE borrow_id IN (SELECT id FROM Borrows WHERE book_isbn = ?)";
            try (PreparedStatement deleteFeedbackStmt = connection.prepareStatement(deleteFeedbackQuery)) {
                deleteFeedbackStmt.setString(1, book.getIsbn());
                deleteFeedbackStmt.executeUpdate();
            }

            String deleteBorrowQuery = "DELETE FROM Borrows WHERE book_isbn = ?";
            try (PreparedStatement deleteBorrowStmt = connection.prepareStatement(deleteBorrowQuery)) {
                deleteBorrowStmt.setString(1, book.getIsbn());
                deleteBorrowStmt.executeUpdate();
            }

            String deleteBookQuery = "DELETE FROM Books WHERE id = ?";
            try (PreparedStatement deleteBookStmt = connection.prepareStatement(deleteBookQuery)) {
                deleteBookStmt.setInt(1, book.getId());
                deleteBookStmt.executeUpdate();
            }


            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    public static int getTotalUserCount(String keyword) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query;
            PreparedStatement statement;

            if (keyword == null || keyword.isEmpty()) {
                query = "SELECT COUNT(*) FROM users";
                statement = connection.prepareStatement(query);
            } else {
                query = "SELECT COUNT(*) FROM users WHERE name LIKE ? OR email LIKE ? OR phone_number LIKE ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, "%" + keyword + "%");
                statement.setString(2, "%" + keyword + "%");
                statement.setString(3, "%" + keyword + "%");
            }

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getTotalBorrowCount(String keyword) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query;
            PreparedStatement statement;

            if (keyword == null || keyword.isEmpty()) {
                query = "SELECT COUNT(*) FROM borrows";
                statement = connection.prepareStatement(query);
            } else {
                query = "SELECT COUNT(*) FROM borrows WHERE status LIKE ? OR book_isbn LIKE ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, "%" + keyword + "%");
                statement.setString(2, "%" + keyword + "%");
            }

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getTotalBookCount(String keyword) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query;
            PreparedStatement statement;

            if (keyword == null || keyword.isEmpty()) {
                query = "SELECT COUNT(*) FROM books";
                statement = connection.prepareStatement(query);
            } else {
                query = "SELECT COUNT(*) FROM books WHERE title LIKE ? OR isbn LIKE ? OR category LIKE ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, "%" + keyword + "%");
                statement.setString(2, "%" + keyword + "%");
                statement.setString(3, "%" + keyword + "%");
            }

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean isBookTitleExists(String bookTitle) {
        boolean exists = false;
        String query = "SELECT COUNT(*) FROM books WHERE title = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, bookTitle);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                exists = resultSet.getInt(1) > 0;
                //resultSet.getInt => get result of count(*)
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exists;
    }

    public static boolean isIsbnExists(String isbn) {
        boolean exists = false;
        String query = "SELECT COUNT(*) FROM books WHERE isbn = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, isbn);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                exists = resultSet.getInt(1) > 0;
                //resultSet.getInt => get result of count(*)
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exists;
    }

    public static Borrow getBorrowById(String borrowId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM borrows WHERE id = ?")) {
            statement.setString(1, borrowId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Integer id = resultSet.getInt("id");
                String memberId = resultSet.getString("member_id");
                String bookIsbn = resultSet.getString("book_isbn");
                LocalDate borrowDate = resultSet.getDate("borrow_date").toLocalDate();
                LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();
                LocalDate returnDate = resultSet.getDate("return_date") != null ? resultSet.getDate("return_date").toLocalDate() : null;
                Status status = Status.valueOf(resultSet.getString("status"));
                Double fine = resultSet.getDouble("fine");

                return new Borrow(id, bookIsbn, memberId, borrowDate, dueDate, returnDate, status, fine);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User authenticate(String username, String password) {
        String querry = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(querry)) {
            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Role role = Role.valueOf(resultSet.getString("role").toUpperCase());
                String id = resultSet.getString("id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String pass = resultSet.getString("password");
                String phoneNumber = resultSet.getString("phone_number");
                String address = resultSet.getString("address");
                Gender gender = Gender.valueOf(resultSet.getString("gender").toUpperCase());
                String avatar = resultSet.getString("avatar");
                LocalDate birthOfDate = resultSet.getDate("birth_of_date").toLocalDate();
                if (role.equals(Role.LIBRARIAN)) {
                    return new Librarian(id, name, email, pass, phoneNumber, address, gender, role, avatar, birthOfDate);
                } else if (role.equals(Role.MEMBER)) {
                    try (PreparedStatement memberStatement = connection.prepareStatement("SELECT * FROM members WHERE id = ? ")) {
                        memberStatement.setString(1, id);
                        ResultSet memberResultSet = memberStatement.executeQuery();
                        if (memberResultSet.next()) {
                            long fineAmount = memberResultSet.getLong("fine_amount");
                            long totalBookBorrowed = memberResultSet.getLong("total_books_borrowed");
                            return new Member(id, name, email, pass, phoneNumber, address, gender, role, avatar, birthOfDate, fineAmount, totalBookBorrowed);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isBookAlreadyBorrowedByUser(String userId, String isbn) {
        String query = "SELECT COUNT(*) FROM borrows WHERE member_id = ? AND book_isbn = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, userId);
            statement.setString(2, isbn);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void startAutoUpdate() {
        if (scheduler.isShutdown() || scheduler.isTerminated()) {
            scheduler = Executors.newScheduledThreadPool(1);
        }
        updateBookBorrowedStatus();
        scheduler.scheduleAtFixedRate(DatabaseUtil::updateBookBorrowedStatus, 1, 1, TimeUnit.HOURS);
    }


    public static void stopAutoUpdate() {
        scheduler.shutdown();
    }

    public static void updateBookBorrowedStatus() {
        try {
            String query = "SELECT id, due_date, return_date FROM borrows WHERE status in ('BORROWING', 'OVERDUE')";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    int borrowId = resultSet.getInt("id");
                    LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();
                    Date returnDateSql = resultSet.getDate("return_date");
                    LocalDate returnDate = (returnDateSql != null) ? ((java.sql.Date) returnDateSql).toLocalDate() : null;
                    String status = "BORROWING";

                    long overDueDays = 0;

                    if (returnDate == null && LocalDate.now().isAfter(dueDate)) {
                        status = "OVERDUE";
                        overDueDays = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
                    } else if (returnDate != null) {
                        status = returnDate.isAfter(dueDate) ? "RETURNED_LATED" : "RETURNED";
                        if (status.equals("RETURNED_LATED")) {
                            overDueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
                        }
                    }

                    double fine = (overDueDays * 5000 < 100000) ? overDueDays * 5000 : 100000;

                    String updateQuery = "UPDATE borrows SET status = ?, fine = ? WHERE id = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setString(1, status);
                    updateStatement.setDouble(2, fine);
                    updateStatement.setInt(3, borrowId);
                    updateStatement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateBookAverageRating(String bookIsbn) {
        String updateAverageRatingQuery = "UPDATE Books " +
                "SET average_of_rating = ( " +
                "    SELECT COALESCE(AVG(rating), 0) " +
                "    FROM Feedbacks " +
                "    WHERE book_id = Books.id " +
                ") " +
                "WHERE isbn = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(updateAverageRatingQuery)) {

            updateStatement.setString(1, bookIsbn);
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static int getAvailableBooks() {
        String query = "SELECT SUM(available_copy) FROM books";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            int bookCount = 0;
            if (resultSet.next()) {
                bookCount = resultSet.getInt(1);
            }
            return bookCount;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getTotalBorrowedBooks() {
        String query = "SELECT COUNT(*) FROM borrows WHERE status in('BORROWING', 'OVERDUE')";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            int bookCount = 0;
            if (resultSet.next()) {
                bookCount = resultSet.getInt(1);
            }
            return bookCount;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getTotalBooks() {
        String query = "SELECT COUNT(*) FROM books";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            int bookCount = 0;
            if (resultSet.next()) {
                bookCount = resultSet.getInt(1);
            }
            return bookCount;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getTotalUsers() {
        String query = "SELECT COUNT(*) FROM users";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            int userCount = 0;
            if (resultSet.next()) {
                userCount = resultSet.getInt(1);
            }
            return userCount;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean checkIfUserBorrowedBook(User user, Book book) {
        return countUserBorrowingBook(user, book) > 0;
    }

    private static int countUserBorrowingBook(User user, Book book) {
        String query = "SELECT COUNT(*) FROM borrows WHERE member_id = ? AND book_isbn = ? AND (status = 'BORROWING' OR status = 'OVERDUE')";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getId());
            statement.setString(2, book.getIsbn());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int countUserBorrowedBook(User user, Book book) {
        String query = "SELECT COUNT(*) FROM borrows WHERE member_id = ? AND book_isbn = ? AND (status = 'RETURNED' OR status = 'RETURNED_LATE')";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getId());
            statement.setString(2, book.getIsbn());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean checkIfUserRatedBook(User user, BorrowedBook book) {
        String query = "SELECT 1 FROM feedbacks WHERE member_id = ? AND book_id = ? AND borrow_id = ? LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getId());
            statement.setInt(2, book.getId());
            statement.setInt(3, book.getBorrowId());
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getAvailableCopyByIsbn(String isbn) {
        String query = "SELECT available_copy FROM books WHERE isbn = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, isbn);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("available_copy");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void updateUserPassword(String userId, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, newPassword);
            statement.setString(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static boolean checkIfUserBorrowingBook(User user) {
        String query = "SELECT 1 FROM borrows WHERE member_id = ? AND status IN ('OVERDUE', 'BORROWING') LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getId());
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkIfBookIsBorrowed(Book book) {
        String query = "SELECT 1 FROM borrows WHERE book_isbn = ? AND status IN ('OVERDUE', 'BORROWING') LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, book.getIsbn());
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<String> loadSuggestionsFromDatabase(String query, ComboBox<String> filterBox) {
        List<String> suggestions = new ArrayList<>();
        String filter = filterBox.getValue();
        String sqlQuery = "SELECT DISTINCT " + filter + " FROM books WHERE " + filter + " LIKE ? LIMIT 10";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, "%" + query + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                suggestions.add(resultSet.getString(filter.toLowerCase()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suggestions;
    }

    public static void updateQuantityBook(int bookId) {
        String query = "UPDATE books SET available_copy = available_copy + 1 WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, bookId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void decreaseQuantityBook(int bookId) {
        String query = "UPDATE books SET available_copy = available_copy - 1 WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, bookId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
