package librio.controllers.admin;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import librio.database.DatabaseConnection;
import librio.models.Book;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static librio.util.DatabaseUtil.*;

public class CreateBookController implements Initializable {

    private boolean openedFromApi = false;

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
    private Button uploadImageButton;

    @FXML
    private TextField yearPublishedTextField;

    @FXML
    private Label yearPublishedErrorLabel;

    @FXML
    private Label quantityOfCopyErrorLabel;

    private Book apiBook;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hideErrorLabels();
        addListeners();
    }

    public interface BookAddedListener {
        void onBookAdded();
    }

    public void setBook(Book book) {
        this.apiBook = book;
        populateFields();
    }

    private String downloadImage(String imageUrl) {
        String savedImagePath = "defaultBook.jpg";
        try {
            String projectDir = System.getProperty("user.dir");
            String imageDir = projectDir + "/src/main/resources/images/book/";
            String imageName = System.currentTimeMillis() + "_book_image.jpg";
            savedImagePath = imageDir + imageName;

            InputStream in = new URL(imageUrl).openStream();
            Files.copy(in, Paths.get(savedImagePath), StandardCopyOption.REPLACE_EXISTING);
            in.close();

            return imageName;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return savedImagePath;
    }

    public void populateFields() {
        if (apiBook != null) {
            // Đặt giá trị của các trường từ đối tượng apiBook
            bookTitleTextField.setText(apiBook.getTitle());
            isbnTextField.setText(apiBook.getIsbn().equals("Unknown ISBN") ? "Unknown ISBN" : apiBook.getIsbn().substring(7));
            authorTextField.setText(apiBook.getAuthor());
            publisherTextField.setText(apiBook.getPublisher());
            categoryTextField.setText(apiBook.getCategory());
            languageTextField.setText(getFullLanguageName(apiBook.getLanguage()));
            yearPublishedTextField.setText(apiBook.getYearPublished().contains("Unknown") ? "Unknown" : apiBook.getYearPublished().substring(0,4));
            descriptionTextArea.setText(apiBook.getDescription());
            numberOfPagesTextField.setText(apiBook.getNumberOfPages());
            openedFromApi = true;


//            if (apiBook.getImagePath() != null && !apiBook.getImagePath().isEmpty()) {
//                bookImageView.setImage(new Image(apiBook.getImagePath()));
//            }

            if (apiBook.getImagePath().equals("defaultBook.jpg")) {
                bookImageView.setImage(new Image(getClass().getResource("/images/book/defaultBook.jpg").toExternalForm()));
                bookImageFilePath = null;
            } else {
                bookImageView.setImage(new Image(apiBook.getImagePath()));
                bookImageFilePath = apiBook.getImagePath();
            }



            bookTitleTextField.setEditable(false);
            isbnTextField.setEditable(false);
            authorTextField.setEditable(false);
            publisherTextField.setEditable(false);
            categoryTextField.setEditable(false);
            languageTextField.setEditable(false);
            yearPublishedTextField.setEditable(false);
            descriptionTextArea.setEditable(false);
            numberOfPagesTextField.setEditable(false);
        }
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
        String averageOfRating = "0.0";

        if (apiBook != null && apiBook.getImagePath() != null && !apiBook.getImagePath().isEmpty()&&!apiBook.getImagePath().equals("defaultBook.jpg")) {
            bookImageFilePath = downloadImage(apiBook.getImagePath());
        }

        boolean validation = false;

        if(bookTitle.isEmpty()){
            bookTitleErrorLabel.setText("Title cannot be empty");
            validation = true;
        }

        if(isbn.isEmpty()){
            isbnErrorLabel.setText("isbn cannot be empty");
            validation = true;
        }else if (!isbn.matches("\\d{10}|\\d{13}")) {
            isbnErrorLabel.setText("isbn must be 10 or 13 digits");
            validation = true;
        } else if (isIsbnExists(isbn)) {
            isbnErrorLabel.setText("isbn already exists");
        }

        if(author.isEmpty()){
            authorErrorLabel.setText("Author must not be empty");
            validation = true;
        }

        if(publisher.isEmpty()){
            publisherErrorLabel.setText("Publisher must not be empty");
            validation = true;
        }

        if(category.isEmpty()){
            categoryErrorLabel.setText("Category cannot be empty");
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
            quantityOfCopyErrorLabel.setText("Quantity of copy must be a number");
            validation = true;
        }

        if(language.isEmpty()){
            languageErrorLabel.setText("Language cannot be empty");
            validation = true;
        }

        if (!yearPublished.isEmpty() ){
            if(!yearPublished.matches("\\d++") && !yearPublished.equals("Unknown")){
                yearPublishedErrorLabel.setText("Year published must be a number or Unknown");
                validation = true;
            }
        }else{
            yearPublished = null;
        }

        if(validation) {
            return;
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO books (title, author, isbn, publisher, category, quantity_copy, average_of_rating, year_published, language, number_of_pages, description, book_image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, bookTitle);
            statement.setString(2, author);
            statement.setString(3, isbn);
            statement.setString(4, publisher);
            statement.setString(5, category);
            statement.setString(6, quantityOfCopy);
            statement.setString(7, averageOfRating);
            if (yearPublished == null || yearPublished.equals("Unknown")) {
                statement.setNull(8, java.sql.Types.INTEGER);
            } else {
                statement.setInt(8, Integer.parseInt(yearPublished));
            }
            statement.setString(9, language);
            statement.setString(10, numberOfPages);
            if (descriptionTextArea.getText().isEmpty()) {
                statement.setString(11, "No description provided!");
            }else {
                statement.setString(11, description);
            }

            statement.setString(12, (bookImageFilePath == null || bookImageFilePath.equals("defaultBook.jpg")) ? null : bookImageFilePath);
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
        yearPublishedErrorLabel.setText("");
    }



    private void addListeners() {

        bookTitleTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                bookTitleErrorLabel.setText("Book title cannot be empty");
            } else {
                bookTitleErrorLabel.setText("");
            }
        });

        isbnTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                isbnErrorLabel.setText("isbn cannot be empty");
            } else if(!newValue.matches("\\d{10}|\\d{13}")){
                isbnErrorLabel.setText("isbn must be 10 or 13 digits");
            }
            else {
                isbnErrorLabel.setText("");
            }
        });

        authorTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                authorErrorLabel.setText("Author cannot be empty");
            } else {
                authorErrorLabel.setText("");
            }
        });

        publisherTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                publisherErrorLabel.setText("Publisher cannot be empty");
            } else {
                publisherErrorLabel.setText("");
            }
        });

        categoryTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                categoryErrorLabel.setText("Category cannot be empty");
            } else {
                categoryErrorLabel.setText("");
            }
        });

        numberOfPagesTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                numberOfPagesErrorLabel.setText("Number of pages cannot be empty");
            } else if(!newValue.matches("\\d+")) {
                numberOfPagesErrorLabel.setText("Number of pages must be a number");
            }else {
                numberOfPagesErrorLabel.setText("");
            }
        });

        quantityOfCopyTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                quantityOfCopyErrorLabel.setText("Quantity of copy cannot be empty");
            } else if(!newValue.matches("\\d+")) {
                quantityOfCopyErrorLabel.setText("Quantity of copy must be a number");
            } else {
                quantityOfCopyErrorLabel.setText("");
            }
        });

        languageTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                languageErrorLabel.setText("Language cannot be empty");
            } else {
                languageErrorLabel.setText("");
            }
        });

        yearPublishedTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.trim().isEmpty()){
                if (!newValue.matches("\\d+") && !newValue.equals("Unknown")){
                    yearPublishedErrorLabel.setText("Year published must be a \nnumber or Unknown");
                }else {
                    yearPublishedErrorLabel.setText("");
                }
            }
            else {
                yearPublishedErrorLabel.setText("");
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

    private String getFullLanguageName(String isoCode) {
        return LANGUAGE_MAP.getOrDefault(isoCode, isoCode);
    }

    private static final Map<String, String> LANGUAGE_MAP = new HashMap<>();

    static {
        LANGUAGE_MAP.put("ar", "Arabic");
        LANGUAGE_MAP.put("bn", "Bengali");
        LANGUAGE_MAP.put("bg", "Bulgarian");
        LANGUAGE_MAP.put("ca", "Catalan");
        LANGUAGE_MAP.put("zh", "Chinese");
        LANGUAGE_MAP.put("hr", "Croatian");
        LANGUAGE_MAP.put("cs", "Czech");
        LANGUAGE_MAP.put("da", "Danish");
        LANGUAGE_MAP.put("nl", "Dutch");
        LANGUAGE_MAP.put("en", "English");
        LANGUAGE_MAP.put("eo", "Esperanto");
        LANGUAGE_MAP.put("tl", "Filipino");
        LANGUAGE_MAP.put("fi", "Finnish");
        LANGUAGE_MAP.put("fr", "French");
        LANGUAGE_MAP.put("de", "German");
        LANGUAGE_MAP.put("el", "Greek");
        LANGUAGE_MAP.put("hi", "Hindi");
        LANGUAGE_MAP.put("hu", "Hungarian");
        LANGUAGE_MAP.put("id", "Indonesian");
        LANGUAGE_MAP.put("it", "Italian");
        LANGUAGE_MAP.put("ja", "Japanese");
        LANGUAGE_MAP.put("jw", "Javanese");
        LANGUAGE_MAP.put("km", "Khmer");
        LANGUAGE_MAP.put("ko", "Korean");
        LANGUAGE_MAP.put("la", "Latin");
        LANGUAGE_MAP.put("mk", "Macedonian");
        LANGUAGE_MAP.put("ml", "Malayalam");
        LANGUAGE_MAP.put("mr", "Marathi");
        LANGUAGE_MAP.put("ne", "Nepali");
        LANGUAGE_MAP.put("no", "Norwegian");
        LANGUAGE_MAP.put("pl", "Polish");
        LANGUAGE_MAP.put("pt", "Portuguese");
        LANGUAGE_MAP.put("ro", "Romanian");
        LANGUAGE_MAP.put("ru", "Russian");
        LANGUAGE_MAP.put("sr", "Serbian");
        LANGUAGE_MAP.put("si", "Sinhalese");
        LANGUAGE_MAP.put("es", "Spanish");
        LANGUAGE_MAP.put("su", "Sundanese");
        LANGUAGE_MAP.put("sw", "Swahili");
        LANGUAGE_MAP.put("sv", "Swedish");
        LANGUAGE_MAP.put("ta", "Tamil");
        LANGUAGE_MAP.put("te", "Telugu");
        LANGUAGE_MAP.put("th", "Thai");
        LANGUAGE_MAP.put("tr", "Turkish");
        LANGUAGE_MAP.put("uk", "Ukrainian");
        LANGUAGE_MAP.put("vi", "Vietnamese");
        LANGUAGE_MAP.put("cy", "Welsh");
        LANGUAGE_MAP.put("xh", "Xhosa");
        LANGUAGE_MAP.put("yi", "Yiddish");
        LANGUAGE_MAP.put("zu", "Zulu");
    }


}
