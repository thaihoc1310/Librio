package librio.controllers.admin;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
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
import librio.auth.Session;
import librio.database.DatabaseConnection;
import librio.models.Book;
import librio.models.User;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.isBookTitleExists;

public class UpdateBookController implements Initializable {
    private Book book;

    @FXML
    private Label authorErrorLabel;

    @FXML
    private TextField authorTextField;

    @FXML
    private ImageView bookImageView;

    private String bookImageFilePath;
    private String previousBookFilePath;

    @FXML
    private Label bookTitleErrorLabel;

    @FXML
    private TextArea bookTitleTextField;

    @FXML
    private Button cancelButton;

    @FXML
    private Label categoryErrorLabel;

    @FXML
    private TextField categoryTextField;


    @FXML
    private TextArea descriptionTextArea;

    @FXML
    private Label isbnErrorLabel;

    @FXML
    private TextField isbnTextField;

    @FXML
    private Label languageErrorLabel;

    @FXML
    private TextField languageTextField;

    @FXML
    private Label numberOfPagesErrorLabel;

    @FXML
    private TextField numberOfPagesTextField;

    @FXML
    private Label publisherErrorLabel;

    @FXML
    private TextField publisherTextField;

    @FXML
    private TextField quantityOfCopyTextField;

    @FXML
    private TextField yearPublishedTextField;

    @FXML
    private Label quantityOfCopyErrorLabel;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hideErrorLabels();
        addListeners();
    }

    public void setBook(Book book) {
        this.book = book;
        populateFields();
    }

    @FXML
    private void updateBook() {
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

        boolean validation = false;

        if(bookTitle.isEmpty()){
            bookTitleErrorLabel.setText("Title cannot be empty");
            validation = true;
        } else if(isBookTitleExists(bookTitle) && !bookTitle.equals(book.getTitle())){
            bookTitleErrorLabel.setText("Title already exists");
            validation = true;
        }

        if(isbn.isEmpty()){
            isbnErrorLabel.setText("isbn cannot be empty");
            validation = true;
        }else if (!isbn.matches("\\d{10}|\\d{13}")) {
            isbnErrorLabel.setText("isbn must be 10 or 13 digits");
            validation = true;
        }

        if(author.isEmpty()){
            authorErrorLabel.setText("Password cannot be empty");
            validation = true;
        }

        if(publisher.isEmpty()){
            publisherErrorLabel.setText("Password cannot be empty");
            validation = true;
        }

        if(category.isEmpty()){
            categoryErrorLabel.setText("Password cannot be empty");
            validation = true;
        }

        if (numberOfPages.isEmpty()) {
            numberOfPagesErrorLabel.setText("Number of pages cannot be empty");
            validation = true;
        } else if (!numberOfPages.matches("\\d+")) {
            numberOfPagesErrorLabel.setText("Number of pages must be a number");
            validation = true;
        }

        if(quantityOfCopy.isEmpty()){
            quantityOfCopyErrorLabel.setText("Quantity of copy cannot be empty");
            validation = true;
        } else if (!quantityOfCopy.matches("\\d+")) {
            quantityOfCopyErrorLabel.setText("Quantity of copy must be a non-negative number");
            validation = true;
        }

        if(language.isEmpty()){
            languageErrorLabel.setText("Password cannot be empty");
            validation = true;
        }

        if(validation) {
            return;
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "UPDATE books SET title = ?, author = ?, isbn = ?, publisher = ?, category = ?, quantity_copy = ?, year_published = ?, " +
                            "language = ?, number_of_pages = ?, description = ?, book_image = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, bookTitle);
            statement.setString(2, author);
            statement.setString(3, isbn);
            statement.setString(4, publisher);
            statement.setString(5, category);
            statement.setString(6, quantityOfCopy);
            statement.setString(7, yearPublished);
            statement.setString(8, language);
            statement.setString(9, numberOfPages);
            if (descriptionTextArea.getText().isEmpty()) {
                statement.setString(10, "No description provided!");
            }else {
                statement.setString(10, description);
            }
            statement.setString(11, bookImageFilePath != null ? bookImageFilePath : book.getImagePath());
            statement.setString(12, Session.getInstance().getLoggedInUser().getEmail());
            statement.setInt(13, book.getId());

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
                File file = new File(path);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    bookImageView.setImage(image);
                } else {
                    // Sử dụng ảnh mặc định nếu không tìm thấy file ảnh sách
                    loadDefaultBookImage();
                }
            } else {
                // Sử dụng ảnh mặc định nếu imagePath là null hoặc rỗng
                loadDefaultBookImage();
            }
        }
    }

    private void loadDefaultBookImage() {
        String projectDir = System.getProperty("user.dir");
        String booksDir = projectDir + "/src/main/resources/images/book/";
        String defaultImage = booksDir + "defaultBook.jpg";
        File defaultImageFile = new File(defaultImage);
        if (defaultImageFile.exists()) {
            Image image = new Image(defaultImageFile.toURI().toString());
            bookImageView.setImage(image);
        }
    }

    private void addListeners() {

        bookTitleTextField.setOnMouseClicked(event -> {hideErrorLabels();});
        isbnTextField.setOnMouseClicked(event -> {hideErrorLabels();});
        authorTextField.setOnMouseClicked(event -> {hideErrorLabels();});
        publisherTextField.setOnMouseClicked(event -> {hideErrorLabels();});
        categoryTextField.setOnMouseClicked(event -> {hideErrorLabels();});
        numberOfPagesTextField.setOnMouseClicked(event -> {hideErrorLabels();});
        quantityOfCopyTextField.setOnMouseClicked(event -> {hideErrorLabels();});
        languageTextField.setOnMouseClicked(event -> {hideErrorLabels();});
        descriptionTextArea.setOnMouseClicked(event -> {hideErrorLabels();});
        yearPublishedTextField.setOnMouseClicked(event -> {hideErrorLabels();});
    }

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

    private void closeStage() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void cancel() {
        clearInputFields();
        closeStage();
    }

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
