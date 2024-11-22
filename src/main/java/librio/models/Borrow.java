package librio.models;


import javafx.beans.property.*;
import librio.enums.Status;

import java.time.Instant;
import java.time.LocalDate;

public class Borrow {
    private IntegerProperty id;
    private StringProperty bookIsbn;
    private StringProperty email;
    private ObjectProperty<LocalDate> borrowDate;
    private ObjectProperty<LocalDate> dueDate;
    private ObjectProperty<LocalDate> returnDate;
    private ObjectProperty<Status> status;
    private DoubleProperty fine;
    private String created_by;
    private Instant created_at;
    private StringProperty update_by;
    private ObjectProperty<Instant> update_at;

    public Borrow(Integer id, String bookIsbn, String email, LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate, Status status, double fine) {
        this.id = new SimpleIntegerProperty(id);
        this.bookIsbn = new SimpleStringProperty(bookIsbn);
        this.email = new SimpleStringProperty(email);
        this.borrowDate = new SimpleObjectProperty<>(borrowDate);
        this.dueDate = new SimpleObjectProperty<>(dueDate);
        this.returnDate = new SimpleObjectProperty<>(returnDate);
        this.status = new SimpleObjectProperty<>(status);
        this.fine = new SimpleDoubleProperty(fine);
    }


    // Getters và Setters


    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getBookIsbn() {
        return bookIsbn.get();
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn.set(bookIsbn);
    }

    public StringProperty bookIdProperty() {
        return bookIsbn;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public StringProperty emailProperty() {
        return email;
    }

    public LocalDate getBorrowDate() {
        return borrowDate.get();
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate.set(borrowDate);
    }

    public ObjectProperty<LocalDate> borrowDateProperty() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate.get();
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate.set(dueDate);
    }

    public ObjectProperty<LocalDate> dueDateProperty() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate.get();
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate.set(returnDate);
    }

    public ObjectProperty<LocalDate> returnDateProperty() {
        return returnDate;
    }

    public Status getStatus() {
        return status.get();
    }

    public void setStatus(Status status) {
        this.status.set(status);
    }

    public ObjectProperty<Status> statusProperty() {
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

    public StringProperty bookIsbnProperty() {
        return bookIsbn;
    }

}
