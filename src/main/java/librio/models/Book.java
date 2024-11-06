package librio.models;

import javafx.beans.property.*;

import java.time.Instant;

public class Book {
    private IntegerProperty id;
    private StringProperty title;
    private StringProperty author;
    private StringProperty category;
    private StringProperty language;
    private StringProperty publisher;
    private StringProperty yearPublished;
    private StringProperty numberOfPages;
    private StringProperty description;
    private StringProperty imagePath;
    private StringProperty isbn;
    private IntegerProperty quantityCopy;
    private DoubleProperty averageOfRating;
    private String created_by;
    private Instant created_at;
    private StringProperty update_by;
    private ObjectProperty<Instant> update_at;

    public Book(Integer id, String title, String isbn, String author, String category, Double averageOfRating) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.category = new SimpleStringProperty(category);
        this.isbn = new SimpleStringProperty(isbn);
        this.averageOfRating = new SimpleDoubleProperty(averageOfRating);
    }

    public Book(Integer id, String title, String isbn, String author, String imagePath) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.isbn = new SimpleStringProperty(isbn);
        this.imagePath = new SimpleStringProperty(imagePath);
    }

    public Book() {
        this.id = new SimpleIntegerProperty();
        this.title = new SimpleStringProperty();
        this.author = new SimpleStringProperty();
        this.isbn = new SimpleStringProperty();
        this.publisher = new SimpleStringProperty();
        this.category = new SimpleStringProperty();
        this.quantityCopy = new SimpleIntegerProperty();
        this.averageOfRating = new SimpleDoubleProperty();
        this.update_at = new SimpleObjectProperty<>();
        this.update_by = new SimpleStringProperty();
    }


    public Book(Integer id, String title, String author, String isbn, String category, String publisher, Integer quantityCopy, Double averageOfRating, String yearPublished, String language, String numberOfPages, String description, String imagePath) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.isbn = new SimpleStringProperty(isbn);
        this.category = new SimpleStringProperty(category);
        this.publisher = new SimpleStringProperty(publisher);
        this.quantityCopy = new SimpleIntegerProperty(quantityCopy);
        this.averageOfRating = new SimpleDoubleProperty(averageOfRating);
        this.yearPublished = new SimpleStringProperty(yearPublished);
        this.language = new SimpleStringProperty(language);
        this.numberOfPages = new SimpleStringProperty(numberOfPages);
        this.description = new SimpleStringProperty(description);
        this.imagePath = new SimpleStringProperty(imagePath);
    }


    // Getters và Setters cho các thuộc tính

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getAuthor() {
        return author.get();
    }

    public void setAuthor(String author) {
        this.author.set(author);
    }

    public StringProperty authorProperty() {
        return author;
    }

    public String getLanguage() {
        return language.get();
    }

    public void setLanguage(String language) {
        this.language.set(language);
    }

    public StringProperty languageProperty() {
        return language;
    }

    public String getIsbn() {
        return isbn.get();
    }

    public void setIsbn(String isbn) {
        this.isbn.set(isbn);
    }

    public StringProperty isbnProperty() {
        return isbn;
    }

    public String getPublisher() {
        return publisher.get();
    }

    public void setPublisher(String publisher) {
        this.publisher.set(publisher);
    }

    public StringProperty publisherProperty() {
        return publisher;
    }

    public String getCategory() {
        return category.get();
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public int getQuantityCopy() {
        return quantityCopy.get();
    }

    public void setQuantityCopy(int quantityCopy) {
        this.quantityCopy.set(quantityCopy);
    }

    public IntegerProperty quantityCopyProperty() {
        return quantityCopy;
    }

    public double getAverageOfRating() {
        return averageOfRating.get();
    }

    public void setAverageOfRating(double averageOfRating) {
        this.averageOfRating.set(averageOfRating);
    }

    public DoubleProperty averageOfRatingProperty() {
        return averageOfRating;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }

    public String getUpdate_by() {
        return update_by.get();
    }

    public void setUpdate_by(String update_by) {
        this.update_by.set(update_by);
    }

    public StringProperty update_byProperty() {
        return update_by;
    }

    public Instant getUpdate_at() {
        return update_at.get();
    }

    public void setUpdate_at(Instant update_at) {
        this.update_at.set(update_at);
    }

    public ObjectProperty<Instant> update_atProperty() {
        return update_at;
    }

    public String getYearPublished() {
        return yearPublished.get();
    }

    public void setYearPublished(String yearPublished) {
        this.yearPublished.set(yearPublished);
    }

    public StringProperty yearPublishedProperty() {
        return yearPublished;
    }

    public String getNumberOfPages() {
        return numberOfPages.get();
    }

    public void setNumberOfPages(String numberOfPages) {
        this.numberOfPages.set(numberOfPages);
    }

    public StringProperty numberOfPagesProperty() {
        return numberOfPages;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public String getImagePath() {
        return imagePath.get();
    }

    public void setImagePath(String imagePath) {
        this.imagePath.set(imagePath);
    }

    public StringProperty imagePathProperty() {
        return imagePath;
    }
}
