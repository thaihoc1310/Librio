package librio.controllers.admin;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import librio.models.Book;

/**
 * @author WINDOWS 10
 */
public class BookDetailController implements Initializable {
    private Book book;

    @FXML
    private Label bookIdLabel;
    @FXML
    private Label bookTitleLabel;
    @FXML
    private Label authorLabel;
    @FXML
    private Label isbnLabel;
    @FXML
    private Label categoryLabel;
    @FXML
    private Label averageOfRatingLabel;
    @FXML
    private Label quantityOfCopyLabel;
    @FXML
    private Label publisherLabel;
    @FXML
    private Label yearPublishedLabel;
    @FXML
    private Label languageLabel;
    @FXML
    private Label numberOfPagesLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private ImageView bookImageView;

    @FXML
    private Button backButton;


    public void setBook(Book book) {
        this.book = book;
        populateFields();
    }

    private void populateFields() {
        if (book != null) {
            bookIdLabel.setText(book.getId());
            bookTitleLabel.setText(book.getTitle());
            authorLabel.setText(book.getAuthor());
            isbnLabel.setText(book.getIsbn());
            categoryLabel.setText(book.getCategory());
            quantityOfCopyLabel.setText(String.valueOf(book.getQuantityCopy()));
            averageOfRatingLabel.setText(String.valueOf(book.getAverageOfRating()));
            publisherLabel.setText(book.getPublisher());
            yearPublishedLabel.setText(book.getYearPublished());
            languageLabel.setText(book.getLanguage());
            numberOfPagesLabel.setText(book.getNumberOfPages());
            descriptionLabel.setText(book.getDescription());

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

    @FXML
    private void back() {
        closeWindow();
    }


    private void closeWindow() {
        // Đóng cửa sổ hiện tại
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        backButton.setCursor(Cursor.HAND);
    }
}
