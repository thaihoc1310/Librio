package librio.models;

import javafx.beans.property.*;

import java.time.Instant;

public class Book {
    //Copy lại phần này rồi paste lại nếu bị conflict
    private StringProperty id;
    private StringProperty title;
    private StringProperty author;
    private StringProperty category;
    private StringProperty language;
    private StringProperty publisher;
    private StringProperty yearPublished;
    private StringProperty numberOfPages;
    private StringProperty description;
    private String imagePath;
    private StringProperty isbn;
    private IntegerProperty quantityCopy;
    private DoubleProperty averageOfRating;
    private String created_by;
    private Instant created_at;
    private StringProperty update_by;
    private ObjectProperty<Instant> update_at;

    public Book(String id, String title, String author, String category, String language, String publisher) {
        this.id = new SimpleStringProperty(id);
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.category = new SimpleStringProperty(category);
        this.language = new SimpleStringProperty(language);
        this.publisher = new SimpleStringProperty(publisher);
    }

    public Book() {
        this.id = new SimpleStringProperty();
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



    public Book(String id, String title, String author, String category, String publisher, String yearPublished, String language, String numberOfPages, String description) {
        this.id = new SimpleStringProperty(id);
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.category = new SimpleStringProperty(category);
        this.publisher = new SimpleStringProperty(publisher);
        this.yearPublished = new SimpleStringProperty(yearPublished);
        this.language = new SimpleStringProperty(language);
        this.numberOfPages = new SimpleStringProperty(numberOfPages);
        this.description = new SimpleStringProperty(description);
    }

    // Getters và Setters cho các thuộc tính
    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public StringProperty idProperty() {
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

    public StringProperty update_byProperty() {
        return update_by;
    }

    public void setUpdate_by(String update_by) {
        this.update_by.set(update_by);
    }

    public Instant getUpdate_at() {
        return update_at.get();
    }

    public ObjectProperty<Instant> update_atProperty() {
        return update_at;
    }

    public void setUpdate_at(Instant update_at) {
        this.update_at.set(update_at);
    }

    public String getYearPublished() {
        return yearPublished.get();
    }

    public StringProperty yearPublishedProperty() {
        return yearPublished;
    }

    public void setYearPublished(String yearPublished) {
        this.yearPublished.set(yearPublished);
    }

    public String getNumberOfPages() {
        return numberOfPages.get();
    }

    public StringProperty numberOfPagesProperty() {
        return numberOfPages;
    }

    public void setNumberOfPages(String numberOfPages) {
        this.numberOfPages.set(numberOfPages);
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
