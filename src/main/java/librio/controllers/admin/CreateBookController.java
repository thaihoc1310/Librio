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
import librio.database.DatabaseConnection;
import librio.models.Book;
import librio.session.Session;

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

import static librio.util.DatabaseUtil.isIsbnExists;

/**
 * The CreateBookController class is responsible for managing the user interface
 * for creating and editing book records. It handles the initialization of UI components,
 * validation of input fields, and interaction with image files. The controller is
 * integrated with JavaFX and is initialized automatically when the associated fxml file
 * is loaded.
 *
 * Fields in this class represent various UI components such as text fields, error labels,
 * and buttons, as well as data storage elements like images and book objects. The fields
 * manage different aspects of a book, including title, author, ISBN, publisher, and more.
 *
 * The class provides methods for setting book details, populating fields with data from
 * an API, creating new book entries, handling file uploads, and more. Interaction with the
 * form is enhanced by providing feedback through error labels and managing the state of
 * the form fields when data is sourced from an API.
 *
 * The controller ensures all key functionalities required for managing book data in a UI
 * context, supporting both manual data entry and API-driven processes.
 */
public class CreateBookController implements Initializable {

    private static final Map<String, String> LANGUAGE_MAP = new HashMap<>();
    private static final Map<String, String> CATEGORY_MAP = new HashMap<>();

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

    static {
        CATEGORY_MAP.put("Nature", "Science");
        CATEGORY_MAP.put("Mathematics", "Science");
        CATEGORY_MAP.put("Science", "Science");
        CATEGORY_MAP.put("Fiction", "Fiction");
        CATEGORY_MAP.put("Juvenile Fiction", "Fiction");
        CATEGORY_MAP.put("Education", "Education");
        CATEGORY_MAP.put("Games & Activities", "Education");
        CATEGORY_MAP.put("Family & Relationships", "Education");
        CATEGORY_MAP.put("Computers", "Computers");
        CATEGORY_MAP.put("Artificial intelligence", "Computers");
        CATEGORY_MAP.put("Bodybuilding", "Health");
        CATEGORY_MAP.put("Medical", "Health");
        CATEGORY_MAP.put("Health", "Health");
        CATEGORY_MAP.put("Health & Fitness", "Health");
        CATEGORY_MAP.put("Cooking", "Health");
        CATEGORY_MAP.put("Psychology", "Health");
        CATEGORY_MAP.put("Enzymes", "Health");
        CATEGORY_MAP.put("Social Science", "Social Science");
        CATEGORY_MAP.put("Political Science", "Social Science");
        CATEGORY_MAP.put("Philosophy", "Social Science");
        CATEGORY_MAP.put("Religion", "Social Science");
        CATEGORY_MAP.put("Business & Economics", "Economics");
        CATEGORY_MAP.put("History", "History");
        CATEGORY_MAP.put("Biography & Autobiography", "History");
        CATEGORY_MAP.put("Art", "Art");
        CATEGORY_MAP.put("Crafts & Hobbies", "Art");
        CATEGORY_MAP.put("Architecture", "Art");
        CATEGORY_MAP.put("Handicraft", "Art");
        CATEGORY_MAP.put("Antiques & Collectibles", "Art");
        CATEGORY_MAP.put("Design", "Art");
        CATEGORY_MAP.put("Travel", "Travel");
        CATEGORY_MAP.put("Technology & Engineering", "Technology");
        CATEGORY_MAP.put("Engineering", "Technology");
        CATEGORY_MAP.put("Technology", "Technology");
        CATEGORY_MAP.put("Music", "Music");
        CATEGORY_MAP.put("Rock musicians", "Music");
        CATEGORY_MAP.put("Sports medicine", "Sports");
        CATEGORY_MAP.put("Sports", "Sports");
        CATEGORY_MAP.put("Exercise", "Sports");
        CATEGORY_MAP.put("Law", "Law");
        CATEGORY_MAP.put("Administrative courts", "Law");
        CATEGORY_MAP.put("Administrative Law", "Law");
    }

    private boolean openedFromApi = false;
    @FXML
    protected Label authorErrorLabel;
    @FXML
    protected Label bookTitleErrorLabel;
    @FXML
    private Label categoryErrorLabel;
    @FXML
    protected Label isbnErrorLabel;
    @FXML
    private Label languageErrorLabel;
    @FXML
    private Label numberOfPagesErrorLabel;
    @FXML
    protected Label publisherErrorLabel;
    @FXML
    private Label yearPublishedErrorLabel;
    @FXML
    private Label quantityOfCopyErrorLabel;
    @FXML
    protected TextField authorTextField;
    @FXML
    private TextField categoryTextField;
    @FXML
    protected TextField isbnTextField;
    @FXML
    private TextField languageTextField;
    @FXML
    private TextField numberOfPagesTextField;
    @FXML
    protected TextField publisherTextField;
    @FXML
    private TextField quantityOfCopyTextField;
    @FXML
    private TextField yearPublishedTextField;
    @FXML
    protected TextArea bookTitleTextField;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private Button cancelButton;
    @FXML
    private Button uploadImageButton;
    @FXML
    protected Button createBookButton;
    @FXML
    protected ImageView bookImageView;
    private Book apiBook;
    private String bookImageFilePath;
    private String previousBookFilePath;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded. It ensures that all error labels
     * are hidden and sets up listeners for user interaction.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hideErrorLabels();
        addListeners();
    }

    /**
     * Sets the current Book object and populates the fields accordingly.
     *
     * @param book the Book object to be set as the current book.
     */
    public void setBook(Book book) {
        this.apiBook = book;
        populateFields();
    }

    /**
     * Downloads an image from the specified URL and saves it to a local directory.
     * If the download fails, a default image path is returned.
     *
     * @param imageUrl the URL of the image to download
     * @return the name of the saved image file if download is successful,
     *         or "defaultBook.jpg" if an error occurs
     */
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

    /**
     * Populates the UI fields with data from the current API book.
     *
     * This method sets the text fields and text areas with the corresponding
     * information from the `apiBook` instance, such as title, ISBN, author, publisher,
     * category, language, year published, description, and number of pages. It also
     * sets the image for the book if available. If the information is marked as
     * unknown or uses specific default values, it adjusts accordingly to display a
     * user-friendly message.
     *
     * Additionally, this method makes the populated fields non-editable to prevent
     * changes to the details brought from the API.
     *
     * Preconditions:
     * - The `apiBook` instance must not be null.
     *
     * Side effects:
     * - Updates the state of `openedFromApi` to true.
     * - Sets the contents of various UI components.
     * - Handles the image view update and its associated file path.
     */
    public void populateFields() {
        if (apiBook != null) {
            bookTitleTextField.setText(apiBook.getTitle());
            isbnTextField.setText(apiBook.getIsbn().equals("Unknown ISBN") ? "Unknown ISBN" : apiBook.getIsbn().substring(7));
            authorTextField.setText(apiBook.getAuthor());
            publisherTextField.setText(apiBook.getPublisher());

            String broadCategory = mapCategoryToBroadCategory(apiBook.getCategory());
            categoryTextField.setText(broadCategory);

            languageTextField.setText(getFullLanguageName(apiBook.getLanguage()));
            yearPublishedTextField.setText(apiBook.getYearPublished().contains("Unknown") ? "Unknown" : apiBook.getYearPublished().substring(0, 4));
            descriptionTextArea.setText(apiBook.getDescription());
            numberOfPagesTextField.setText(apiBook.getNumberOfPages());
            openedFromApi = true;

            if (apiBook.getImagePath() == null) {
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

    /**
     * Handles the creation of a new book entry in the database.
     * This method is triggered by a UI event and performs validation
     * on input fields before attempting to insert a new book record into the database.
     *
     * Input fields are fetched directly from the associated UI components such as TextFields and TextArea.
     * If the image of the book is provided through an API and is not the default image,
     * this image is downloaded and saved locally.
     * Performs a series of validation checks on mandatory fields (title, isbn, author, etc.),
     * and sets relevant error messages if any validation fails.
     *
     * If validation passes, constructs an SQL INSERT query to save the book details, including:
     * - Title, Author, ISBN, Publisher, Category
     * - Quantity of Copies (total and available), Average Rating (default is 0.0)
     * - Year Published, Language, Number of Pages, Description, Book Image
     *
     * The image is saved locally and relevant paths are updated if needed. Finally, it clears the input fields
     * and closes the current stage upon successful insertion of a new book record.
     *
     * In case of any SQL or general exception, stack trace is printed for debugging purposes.
     */
    @FXML
    protected void createBook() {
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

        if (apiBook != null && apiBook.getImagePath() != null && !apiBook.getImagePath().isEmpty()) {
            bookImageFilePath = downloadImage(apiBook.getImagePath());
        }

        boolean validation = false;

        if (bookTitle.isEmpty()) {
            bookTitleErrorLabel.setText("Title cannot be empty");
            validation = true;
        }

        if (isbn.isEmpty()) {
            isbnErrorLabel.setText("isbn cannot be empty");
            validation = true;
        } else if (!isbn.matches("\\d{10}|\\d{13}")) {
            isbnErrorLabel.setText("isbn must be 10 or 13 digits");
            validation = true;
        } else if (isIsbnExists(isbn)) {
            isbnErrorLabel.setText("isbn already exists");
            validation = true;
        }

        if (author.isEmpty()) {
            authorErrorLabel.setText("Author must not be empty");
            validation = true;
        }

        if (publisher.isEmpty()) {
            publisherErrorLabel.setText("Publisher must not be empty");
            validation = true;
        }

        if (category.isEmpty()) {
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

        if (quantityOfCopy.isEmpty()) {
            quantityOfCopyErrorLabel.setText("Quantity of copy cannot be empty");
            validation = true;
        } else if (!quantityOfCopy.matches("\\d+")) {
            quantityOfCopyErrorLabel.setText("Quantity of copy must be a number");
            validation = true;
        }

        if (language.isEmpty()) {
            languageErrorLabel.setText("Language cannot be empty");
            validation = true;
        }

        if (!yearPublished.isEmpty()) {
            if (!yearPublished.matches("\\d++") && !yearPublished.equals("Unknown")) {
                yearPublishedErrorLabel.setText("Year published must be a number or Unknown");
                validation = true;
            }
        } else {
            yearPublished = null;
        }

        if (validation) {
            return;
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO books (title, author, isbn, publisher, category, quantity_copy, available_copy, average_of_rating, year_published, language, number_of_pages, description, book_image, created_by, created_at)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, bookTitle);
            statement.setString(2, author);
            statement.setString(3, isbn);
            statement.setString(4, publisher);
            statement.setString(5, category);
            statement.setString(6, quantityOfCopy);
            statement.setString(7, quantityOfCopy);
            statement.setString(8, averageOfRating);
            if (yearPublished == null || yearPublished.equals("Unknown")) {
                statement.setNull(9, java.sql.Types.INTEGER);
            } else {
                statement.setInt(9, Integer.parseInt(yearPublished));
            }
            statement.setString(10, language);
            statement.setString(11, numberOfPages);
            if (descriptionTextArea.getText().isEmpty()) {
                statement.setString(12, "No description provided!");
            } else {
                statement.setString(12, description);
            }

            statement.setString(13, (bookImageFilePath == null || bookImageFilePath.equals("defaultBook.jpg")) ? null : bookImageFilePath);
            statement.setString(14, Session.getInstance().getLoggedInUser().getEmail());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                String projectDir = System.getProperty("user.dir");
                String booksDir = projectDir + "/src/main/resources/images/book/";
                if (previousBookFilePath != null) {
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
     * Resets the text of all form error labels to empty strings, effectively hiding them.
     * This method is used to clear any previous error messages displayed to the user
     * as they interact with the input fields in the form.
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
        yearPublishedErrorLabel.setText("");
    }

    /**
     * Adds event listeners to various text fields and text area within the form.
     * These listeners are triggered when a mouse click event occurs on the
     * respective fields, leading to the invocation of the hideErrorLabels()
     * method. This effectively clears any error messages associated with the
     * input fields, improving user experience by ensuring error indicators are
     * removed when the user interacts with the form elements.
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
     * Handles the action of uploading an image for the book. This method is triggered when the
     * user initiates the process to select an image file to associate with the book being created
     * or edited. The allowed file types for selection are PNG, JPG, and JPEG.
     *
     * The method uses a FileChooser dialog to allow the user to select an image. Upon selecting
     * a valid file, it updates the image view with the selected image and sets the file path
     * properties accordingly.
     *
     * Note: This method also invokes {@code hideErrorLabels()} to clear any visible error labels
     * before opening the file chooser dialog.
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
     * Closes the current stage window associated with the cancel button.
     * This method is typically used to terminate or exit the current user interface window.
     */
    private void closeStage() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }


    /**
     * Cancels the current book creation or editing process.
     * This method clears all input fields and closes the stage associated with the cancel button.
     * It is typically invoked when the user decides not to proceed with the creation or modification of a book record.
     */
    @FXML
    private void cancel() {
        clearInputFields();
        closeStage();
    }

    /**
     * Clears all text input fields and text areas in the form.
     *
     * This method resets the input fields for book title, ISBN, author,
     * publisher, category, number of pages, quantity of copy, language,
     * year published, and the description text area to their default state.
     * It is typically used to reset the form after a book is successfully
     * created or when the cancel action is invoked.
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

    /**
     * Retrieves the full name of a language based on its ISO code.
     *
     * @param isoCode the ISO code of the language.
     * @return the full name of the language corresponding to the given ISO code,
     * or "Others" if the ISO code is not found in the LANGUAGE_MAP.
     */
    private String getFullLanguageName(String isoCode) {
        return LANGUAGE_MAP.getOrDefault(isoCode, "Others");
    }

    /**
     * Maps a specific category to a broader category based on predefined mappings.
     * If the given category is not found in the mapping, returns the default value "Others".
     *
     * @param category the specific category to be mapped
     * @return the corresponding broad category or "Others" if no mapping is found
     */
    private String mapCategoryToBroadCategory(String category) {
        return CATEGORY_MAP.getOrDefault(category, "Others");
    }

}
