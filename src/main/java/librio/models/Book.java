package librio.models;

import javafx.beans.property.*;

import java.time.Instant;

public class Book {
    private StringProperty id;
    private StringProperty title;
    private StringProperty author;
    private StringProperty isbn;
    private StringProperty publisher;
    private StringProperty category;
    private IntegerProperty quantityCopy;
    private DoubleProperty averageOfRating;
    private String created_by;
    private Instant created_at;
    private StringProperty update_by;
    private ObjectProperty<Instant> update_at;

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
}
