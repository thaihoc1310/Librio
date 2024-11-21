package librio.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;

public class BorrowedBook extends Book {
    private ObjectProperty<LocalDate> borrowDate;
    private ObjectProperty<LocalDate> dueDate;
    private ObjectProperty<LocalDate> returnDate;
    private Status status;
    private Double fine;
    private Integer borrowId;


    public BorrowedBook(Book book, LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate, Status status, Double fine, Integer borrowerId) {
        super(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(), book.getImagePath());
        this.dueDate = new SimpleObjectProperty<>(dueDate);
        this.borrowDate = new SimpleObjectProperty<>(borrowDate);
        this.returnDate = new SimpleObjectProperty<>(returnDate);
        this.status = status;
        this.fine = fine;
        this.borrowId = borrowerId;
    }

    public LocalDate getDueDate() {
        return dueDate.get();
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate.set(dueDate);
    }

    public LocalDate getBorrowDate() {
        return borrowDate.get();
    }

    public LocalDate getReturnDate() {
        return returnDate.get();
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate.set(returnDate);
    }

    public void setborrowDate(LocalDate borrowDate) {
        this.borrowDate.set(borrowDate);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Double getFine() {
        return fine;
    }

    public Integer getBorrowId() {
        return borrowId;
    }

    public void setBorrowId(Integer borrowerId) {
        this.borrowId = borrowerId;
    }


}

