package librio.models;


import javafx.beans.property.*;

import java.time.Instant;

public class Borrow {
    private StringProperty id;
    private StringProperty bookId;
    private StringProperty memberId;
    private ObjectProperty<Instant> borrowDate;
    private ObjectProperty<Instant> dueDate;
    private ObjectProperty<Instant> returnDate;
    private StringProperty status;
    private DoubleProperty fine;
    private String created_by;
    private Instant created_at;
    private StringProperty update_by;
    private ObjectProperty<Instant> update_at;

    public Borrow() {
        this.id = new SimpleStringProperty();
        this.bookId = new SimpleStringProperty();
        this.memberId = new SimpleStringProperty();
        this.borrowDate = new SimpleObjectProperty<>();
        this.dueDate = new SimpleObjectProperty<>();
        this.returnDate = new SimpleObjectProperty<>();
        this.status = new SimpleStringProperty();
        this.fine = new SimpleDoubleProperty();
        this.update_at = new SimpleObjectProperty<>();
        this.update_by = new SimpleStringProperty();
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

    public Instant getBorrowDate() {
        return borrowDate.get();
    }

    public void setBorrowDate(Instant borrowDate) {
        this.borrowDate.set(borrowDate);
    }

    public ObjectProperty<Instant> borrowDateProperty() {
        return borrowDate;
    }

    public Instant getDueDate() {
        return dueDate.get();
    }

    public void setDueDate(Instant dueDate) {
        this.dueDate.set(dueDate);
    }

    public ObjectProperty<Instant> dueDateProperty() {
        return dueDate;
    }

    public Instant getReturnDate() {
        return returnDate.get();
    }

    public void setReturnDate(Instant returnDate) {
        this.returnDate.set(returnDate);
    }

    public ObjectProperty<Instant> returnDateProperty() {
        return returnDate;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public StringProperty statusProperty() {
        return status;
    }

    public double getFine() {
        return fine.get();
    }

    public void setFine(double fine) {
        this.fine.set(fine);
    }

    public DoubleProperty fineProperty() {
        return fine;
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
