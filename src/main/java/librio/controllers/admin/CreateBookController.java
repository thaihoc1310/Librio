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
import librio.database.DatabaseConnection;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CreateBookController implements Initializable {
    private ManageBookController manageBookController;


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
    private Button createBookButton;

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
    private Button uploadImageButton;

    @FXML
    private TextField yearPublishedTextField;

    @FXML
    private Label quantityOfCopyErrorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hideErrorLabels();
        addListeners();
    }

    public void setManageBookController(ManageBookController manageBookController) {
        this.manageBookController = manageBookController;
    }

    @FXML
    private void createBook() {
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
            bookTitleErrorLabel.setText("Name cannot be empty");
            validation = true;
        }

        if(isbn.isEmpty()){
            isbnErrorLabel.setText("Password cannot be empty");
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

        if(numberOfPages.isEmpty()){
            numberOfPagesErrorLabel.setText("Password cannot be empty");
            validation = true;
        }

        if(quantityOfCopy.isEmpty()){
            quantityOfCopyErrorLabel.setText("Password cannot be empty");
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
            String query = "INSERT INTO books (title, author, isbn, publisher, category, quantity_copy, year_published, language, number_of_pages, description, book_image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            statement.setString(10, description);
            statement.setString(11, bookImageFilePath);
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                String projectDir = System.getProperty("user.dir");
                String booksDir = projectDir + "/src/main/resources/images/book/";
                if(previousBookFilePath != null){
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



    private void addListeners() {

        bookTitleTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                bookTitleErrorLabel.setText("Name cannot be empty");
            } else {
                bookTitleErrorLabel.setText("");
            }
        });

        isbnTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                isbnErrorLabel.setText("Name cannot be empty");
            } else {
                isbnErrorLabel.setText("");
            }
        });

        authorTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                authorErrorLabel.setText("Name cannot be empty");
            } else {
                authorErrorLabel.setText("");
            }
        });

        publisherTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                publisherErrorLabel.setText("Name cannot be empty");
            } else {
                publisherErrorLabel.setText("");
            }
        });

        categoryTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                categoryErrorLabel.setText("Name cannot be empty");
            } else {
                categoryErrorLabel.setText("");
            }
        });

        numberOfPagesTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                numberOfPagesErrorLabel.setText("Name cannot be empty");
            } else {
                numberOfPagesErrorLabel.setText("");
            }
        });

        quantityOfCopyTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                quantityOfCopyErrorLabel.setText("Name cannot be empty");
            } else {
                quantityOfCopyErrorLabel.setText("");
            }
        });

        languageTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                languageErrorLabel.setText("Name cannot be empty");
            } else {
                languageErrorLabel.setText("");
            }
        });
    }

    @FXML
    private void uploadImage() {
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
