package librio.controllers.admin;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import librio.models.Book;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static librio.util.DesignUtil.loadDefaultBookImage;

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
    private Text descriptionText;
    @FXML
    private ImageView bookImageView;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Button backButton;


    public void setBook(Book book) {
        this.book = book;
        populateFields();
    }

    private void populateFields() {
        if (book != null) {
            bookIdLabel.setText(String.valueOf(book.getId()));
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
            descriptionText.setText(book.getDescription());

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
                    loadDefaultBookImage(bookImageView);
                }
            } else {
                // Sử dụng ảnh mặc định nếu imagePath là null hoặc rỗng
                loadDefaultBookImage(bookImageView);
            }
        }
    }


    @FXML
    private void back() {
        closeWindow();
    }


    private void closeWindow() {
        // Đóng cửa sổ hiện tại
        Stage stage = (Stage) isbnLabel.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scrollPane.setOnScroll(event -> {
            Node thumb = scrollPane.lookup(".thumb");

            if (thumb != null) {

                thumb.getStyleClass().add("scrolling");

                new Timeline(new KeyFrame(Duration.millis(2000), e -> {
                    thumb.getStyleClass().remove("scrolling");
                })).play();
            }
        });

    }

}
