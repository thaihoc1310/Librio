package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import librio.cache.ImageCache;
import librio.database.DatabaseConnection;
import librio.models.Book;
import librio.session.Session;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.getBookByIsbn;
import static librio.util.DatabaseUtil.isBookTitleExists;
import static librio.util.DesignUtil.loadDefaultBookImage;

/**
 * Populates the form fields with data from the current book instance.
 *
 * This method uses the attributes of the current Book object to
 * populate corresponding UI components, such as text fields and
 * labels, allowing users to see and edit existing book data.
 * It fills in fields like title, author, ISBN, and more with
 * the book's stored data.
 */
public class UpdateBookController implements Initializable {
    @FXML
    protected Label authorErrorLabel;
    @FXML
    protected Label bookTitleErrorLabel;
    @FXML
    protected Label categoryErrorLabel;
    @FXML
    protected Label isbnErrorLabel;
    @FXML
    protected Label languageErrorLabel;
    @FXML
    protected Label numberOfPagesErrorLabel;
    @FXML
    protected Label publisherErrorLabel;
    @FXML
    protected Label quantityOfCopyErrorLabel;
    @FXML
    protected TextField authorTextField;
    @FXML
    protected TextField categoryTextField;
    @FXML
    protected TextField isbnTextField;
    @FXML
    protected TextField languageTextField;
    @FXML
    protected TextField numberOfPagesTextField;
    @FXML
    protected TextField publisherTextField;
    @FXML
    protected TextField quantityOfCopyTextField;
    @FXML
    protected TextField yearPublishedTextField;
    @FXML
    protected TextArea bookTitleTextField;
    @FXML
    protected TextArea descriptionTextArea;
    @FXML
    protected Button cancelButton;
    @FXML
    protected ImageView bookImageView;

    private Book book;

    private String bookImageFilePath;

    private String previousBookFilePath;

    /**
     * Initializes the UpdateBookController by hiding all error labels and adding necessary input field listeners.
     *
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hideErrorLabels();
        addListeners();
    }

    /**
     * Sets the current book instance to the given book and populates the fields with its data.
     *
     * @param book the Book object containing the information to be set and displayed
     */
    public void setBook(Book book) {
        this.book = book;
        populateFields();
    }

    /**
     * Updates the details of an existing book in the database. It retrieves input values
     * from text fields to update the book's attributes such as title, ISBN, author,
     * publisher, category, number of pages, quantity of copies, language, year published,
     * and description. This method checks for the validity of input data and ensures
     * they adhere to specific constraints (e.g., non-empty fields, valid ISBN format).
     *
     * If the input data is valid, the method constructs an SQL update query to modify
     * the book record in the database. Additionally, it handles the updating of the
     * book's image file if a new image upload is detected, ensuring the old image is
     * removed from the directory if replaced.
     *
     * The method includes error handling for SQL and other exceptions, and manages UI
     * updates such as clearing input fields and closing the window if the update is
     * successful. Validation errors are displayed by setting appropriate error messages
     * on designated labels.
     */
    @FXML
    protected void updateBook() {
        String bookTitle = bookTitleTextField.getText();
        String isbn = isbnTextField.getText();
        String author = authorTextField.getText();
        String publisher = publisherTextField.getText();
        String category = categoryTextField.getText();
        String numberOfPages = numberOfPagesTextField.getText();
        String quantityOfCopy = quantityOfCopyTextField.getText();
        String language = languageTextField.getText();
        String yearPublished = yearPublishedTextField.getText();
        String description = descriptionTextArea.getText();

        Book book = getBookByIsbn(isbn);

        if (book == null) {
            return;
        }

        int totalBorrowedBooks = book.getQuantityCopy() - book.getAvailableCopy();

        boolean validation = false;

        if (bookTitle.isEmpty()) {
            bookTitleErrorLabel.setText("Title cannot be empty!");
            validation = true;
        } else if (isBookTitleExists(bookTitle) && !bookTitle.equals(book.getTitle())) {
            bookTitleErrorLabel.setText("Title already exists!");
            validation = true;
        }

        if (isbn.isEmpty()) {
            isbnErrorLabel.setText("isbn cannot be empty!");
            validation = true;
        } else if (!isbn.matches("\\d{10}|\\d{13}")) {
            isbnErrorLabel.setText("isbn must be 10 or 13 digits!");
            validation = true;
        }

        if (author.isEmpty()) {
            authorErrorLabel.setText("Author cannot be empty!");
            validation = true;
        }

        if (publisher.isEmpty()) {
            publisherErrorLabel.setText("Publisher cannot be empty!");
            validation = true;
        }

        if (category.isEmpty()) {
            categoryErrorLabel.setText("Category cannot be empty!");
            validation = true;
        }

        if (numberOfPages.isEmpty()) {
            numberOfPagesErrorLabel.setText("Number of pages cannot be empty!");
            validation = true;
        } else if (!numberOfPages.matches("\\d+")) {
            numberOfPagesErrorLabel.setText("Number of pages must be a number!");
            validation = true;
        }

        if (quantityOfCopy.isEmpty()) {
            quantityOfCopyErrorLabel.setText("Quantity of copy cannot be empty!");
            validation = true;
        } else if (!quantityOfCopy.matches("\\d+")) {
            quantityOfCopyErrorLabel.setText("Quantity of copy must be a non-negative number!");
            validation = true;
        } else if (Integer.parseInt(quantityOfCopy) < totalBorrowedBooks) {
            quantityOfCopyErrorLabel.setText("Invalid quantity of copy!");
            validation = true;
        }

        if (language.isEmpty()) {
            languageErrorLabel.setText("Password cannot be empty!");
            validation = true;
        }

        if (validation) {
            return;
        }

        String availableCopy = String.valueOf(Integer.parseInt(quantityOfCopy) - totalBorrowedBooks);

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "UPDATE books SET title = ?, author = ?, isbn = ?, publisher = ?, category = ?, quantity_copy = ?, available_copy = ? ,year_published = ?, " +
                    "language = ?, number_of_pages = ?, description = ?, book_image = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, bookTitle);
            statement.setString(2, author);
            statement.setString(3, isbn);
            statement.setString(4, publisher);
            statement.setString(5, category);
            statement.setString(6, quantityOfCopy);
            statement.setString(7, availableCopy);
            statement.setString(8, yearPublished);
            statement.setString(9, language);
            statement.setString(10, numberOfPages);
            if (descriptionTextArea.getText().isEmpty()) {
                statement.setString(11, "No description provided!");
            } else {
                statement.setString(11, description);
            }
            statement.setString(12, bookImageFilePath != null ? bookImageFilePath : book.getImagePath());
            statement.setString(13, Session.getInstance().getLoggedInUser().getEmail());
            statement.setInt(14, book.getId());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                if (previousBookFilePath != null && bookImageFilePath != null) {
                    String projectDir = System.getProperty("user.dir");
                    String booksDir = projectDir + "/src/main/resources/images/book/";
                    if (book.getImagePath() != null && !book.getImagePath().isEmpty()) {
                        File oldFile = new File(booksDir + book.getImagePath());
                        if (oldFile.exists()) {
                            boolean deleted = oldFile.delete();
                            if (!deleted) {
                                System.out.println("Không thể xóa tệp ảnh cũ: " + oldFile.getAbsolutePath());
                            }
                        }
                    }
                    Files.copy(Paths.get(previousBookFilePath), Paths.get(booksDir + bookImageFilePath));
                }
                clearInputFields();
                closeStage();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears all error labels related to the book details form.
     *
     * This method sets the text of various error labels used in the
     * UpdateBookController to an empty string, effectively hiding
     * them from the user interface. It is typically invoked to reset
     * the error state of the form fields.
     *
     * This method affects the following labels:
     * - Book Title Error
     * - ISBN Error
     * - Author Error
     * - Publisher Error
     * - Category Error
     * - Number of Pages Error
     * - Quantity of Copies Error
     * - Language Error
     */
    private void hideErrorLabels() {
        bookTitleErrorLabel.setText("");
        isbnErrorLabel.setText("");
        authorErrorLabel.setText("");
        publisherErrorLabel.setText("");
        categoryErrorLabel.setText("");
        numberOfPagesErrorLabel.setText("");
        quantityOfCopyErrorLabel.setText("");
        languageErrorLabel.setText("");
    }

    /**
     * Populates the text fields and image view with the information from the current book object.
     * This method is used to display the details of a book in the appropriate UI components
     * such as text fields and an image view when a book is selected or edited.
     *
     * If the book object is not null, this method sets the text fields for the title, ISBN,
     * author, category, publisher, year of publication, language, number of pages, description,
     * and quantity of copies with their respective values from the book object.
     *
     * The method also handles the display of the book image. If a valid image path is provided
     * in the book object, it attempts to retrieve and display the image using an image cache
     * mechanism. If the image path is null or empty, a default book image is loaded and displayed.
     */
    private void populateFields() {
        if (book != null) {
            bookTitleTextField.setText(book.getTitle());
            isbnTextField.setText(book.getIsbn());
            authorTextField.setText(book.getAuthor());
            categoryTextField.setText(book.getCategory());
            publisherTextField.setText(book.getPublisher());
            yearPublishedTextField.setText(book.getYearPublished());
            languageTextField.setText(book.getLanguage());
            numberOfPagesTextField.setText(book.getNumberOfPages());
            descriptionTextArea.setText(book.getDescription());
            numberOfPagesTextField.setText(book.getNumberOfPages());
            quantityOfCopyTextField.setText(String.valueOf(book.getQuantityCopy()));

            if (book.getImagePath() != null && !book.getImagePath().isEmpty()) {
                String projectDir = System.getProperty("user.dir");
                String booksDir = projectDir + "/src/main/resources/images/book/";
                String path = booksDir + book.getImagePath();

                Image image = ImageCache.getInstance().getImage(path, projectDir + "defaultBook.jpg");
                bookImageView.setImage(image);
            } else {
                loadDefaultBookImage(bookImageView);
            }
        }
    }

    /**
     * Adds event listeners to various text fields and text areas to handle
     * mouse click events. When a mouse click event occurs on any of these UI
     * components, the error labels associated with the book details form are
     * hidden by invoking the {@code hideErrorLabels()} method. This is typically
     * used to clear any error messages displayed after a user interacts with
     * the text fields or text areas.
     *
     * Components with listeners include:
     * - bookTitleTextField
     * - isbnTextField
     * - authorTextField
     * - publisherTextField
     * - categoryTextField
     * - numberOfPagesTextField
     * - quantityOfCopyTextField
     * - languageTextField
     * - descriptionTextArea
     * - yearPublishedTextField
     */
    private void addListeners() {
        bookTitleTextField.setOnMouseClicked(event -> hideErrorLabels());
        isbnTextField.setOnMouseClicked(event -> hideErrorLabels());
        authorTextField.setOnMouseClicked(event -> hideErrorLabels());
        publisherTextField.setOnMouseClicked(event -> hideErrorLabels());
        categoryTextField.setOnMouseClicked(event -> hideErrorLabels());
        numberOfPagesTextField.setOnMouseClicked(event -> hideErrorLabels());
        quantityOfCopyTextField.setOnMouseClicked(event -> hideErrorLabels());
        languageTextField.setOnMouseClicked(event -> hideErrorLabels());
        descriptionTextArea.setOnMouseClicked(event -> hideErrorLabels());
        yearPublishedTextField.setOnMouseClicked(event -> hideErrorLabels());
    }

    /**
     * Handles the action of uploading an image file to set as the book's image.
     * This method opens a file chooser dialog allowing the user to select an image file.
     * Supported image formats are PNG, JPG, and JPEG. Upon successful selection, the image
     * is displayed in the designated image view, and the file path is stored.
     * It also resets any displayed error labels related to previous operations.
     */
    @FXML
    private void uploadImage() {
        hideErrorLabels();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            bookImageFilePath = System.currentTimeMillis() + "_" + selectedFile.getName();
            previousBookFilePath = selectedFile.getAbsolutePath();
            bookImageView.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    /**
     * Closes the current stage (window) associated with the cancelButton.
     * This method is intended to be called to terminate the display of the current
     * JavaFX window or dialog, effectively hiding it from the user.
     */
    private void closeStage() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Cancels the current operation by clearing all input fields and closing the current stage.
     * This method is typically called when the cancel button is activated by the user.
     */
    @FXML
    private void cancel() {
        clearInputFields();
        closeStage();
    }

    /**
     * Clears the text from all input fields and text areas associated with book details.
     * This method is typically used to reset the input fields to their default
     * empty state after an action such as updating or canceling an update operation.
     */
    private void clearInputFields() {
        bookTitleTextField.clear();
        isbnTextField.clear();
        authorTextField.clear();
        publisherTextField.clear();
        categoryTextField.clear();
        numberOfPagesTextField.clear();
        quantityOfCopyTextField.clear();
        languageTextField.clear();
        yearPublishedTextField.clear();
        descriptionTextArea.clear();
    }
}
