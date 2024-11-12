package librio.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;

public class BorrowedBook extends Book {
    private ObjectProperty<LocalDate> borrowDate;
    private ObjectProperty<LocalDate> dueDate;
    private Status status;
    private Double fine;


    public BorrowedBook(Book book, LocalDate borrowDate, LocalDate dueDate, Status status, Double fine) {
        super(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(), book.getImagePath());
        this.dueDate = new SimpleObjectProperty<>(dueDate);
        this.borrowDate = new SimpleObjectProperty<>(borrowDate);
        this.status = status;
        this.fine = fine;
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
}

