package librio.models;


import javafx.beans.property.*;

import java.time.Instant;

public class Feedback {
    private StringProperty id;
    private StringProperty bookId;
    private StringProperty memberId;
    private IntegerProperty rating;
    private StringProperty about;
    private String created_by;
    private Instant created_at;
    private StringProperty update_by;
    private ObjectProperty<Instant> update_at;

    public Feedback() {
        this.id = new SimpleStringProperty();
        this.bookId = new SimpleStringProperty();
        this.memberId = new SimpleStringProperty();
        this.rating = new SimpleIntegerProperty();
        this.about = new SimpleStringProperty();
        this.update_at = new SimpleObjectProperty<>();
        this.update_by = new SimpleStringProperty();
    }

    public Feedback(String id, String bookId, String memberId, Integer rating, String about, Instant created_at) {
        this.id = new SimpleStringProperty(id);
        this.bookId = new SimpleStringProperty(bookId);
        this.memberId = new SimpleStringProperty(memberId);
        this.rating = new SimpleIntegerProperty(rating);
        this.about = new SimpleStringProperty(about);
        this.update_at = new SimpleObjectProperty<>(created_at);
    }

    // Getters và Setters
    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public StringProperty idProperty() {
        return id;
    }

    public String getBookId() {
        return bookId.get();
    }

    public void setBookId(String bookId) {
        this.bookId.set(bookId);
    }

    public StringProperty bookIdProperty() {
        return bookId;
    }

    public String getMemberId() {
        return memberId.get();
    }

    public void setMemberId(String memberId) {
        this.memberId.set(memberId);
    }

    public StringProperty memberIdProperty() {
        return memberId;
    }

    public int getRating() {
        return rating.get();
    }

    public void setRating(int rating) {
        this.rating.set(rating);
    }

    public IntegerProperty ratingProperty() {
        return rating;
    }

    public String getAbout() {
        return about.get();
    }

    public void setAbout(String about) {
        this.about.set(about);
    }

    public StringProperty aboutProperty() {
        return about;
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
}
