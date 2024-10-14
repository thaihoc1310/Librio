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

import static librio.controllers.admin.CreateUserController.cropAndClipToCircle;

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
    private Label categoryLabel;
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
    private ImageView bookImageImageView;

    @FXML
    private Button backButton;
    private ManageBookController manageBookController;


    public void setBook(Book book) {
        this.book = book;
        populateFields();
    }

    public void setManageBookController(ManageBookController manageBookController) {
        this.manageBookController = manageBookController;
    }

    private void populateFields() {
        if (book != null) {
            bookIdLabel.setText(book.getId());
            bookTitleLabel.setText(book.getTitle());
            authorLabel.setText(book.getAuthor());
            categoryLabel.setText(book.getCategory());
            publisherLabel.setText(book.getPublisher());
            yearPublishedLabel.setText(book.getYearPublished());
            languageLabel.setText(book.getLanguage());
            numberOfPagesLabel.setText(book.getNumberOfPages());
            descriptionLabel.setText(book.getDescription());

//            String imagePath = "/images/book_covers/" + book.getImagePath(); // Đường dẫn ảnh sách (giả định)
//            loadImage(imagePath, bookImageImageView);  // Gọi phương thức loadImage để gán ảnh
        }
    }

    private void loadImage(String imagePath, ImageView imageView) {
        File file = new File(imagePath);
        if (file.exists()) {
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
        } else {
            // Load ảnh mặc định
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/book/harryPottersample.png"));
            imageView.setImage(defaultImage);
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
