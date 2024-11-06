package librio.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;

public class BorrowedBook extends Book {
    private ObjectProperty<LocalDate> dueDate;
    private Status status;


    public BorrowedBook(Book book, LocalDate dueDate, Status status) {
        super(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(), book.getImagePath());
        this.dueDate = new SimpleObjectProperty<>(dueDate);
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate.get();
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate.set(dueDate);
    }
}

