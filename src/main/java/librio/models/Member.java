package librio.models;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.LongProperty;

public class Member extends User {
    private LongProperty fineAmount;
    private LongProperty totalBookBorrowed;

    public Member() {
        super();
        this.fineAmount = new SimpleLongProperty();
        this.totalBookBorrowed = new SimpleLongProperty();
    }

    public long getFineAmount() {
        return fineAmount.get();
    }

    public void setFineAmount(long fineAmount) {
        this.fineAmount.set(fineAmount);
    }

    public LongProperty fineAmountProperty() {
        return fineAmount;
    }

    public long getTotalBookBorrowed() {
        return totalBookBorrowed.get();
    }

    public void setTotalBookBorrowed(long totalBookBorrowed) {
        this.totalBookBorrowed.set(totalBookBorrowed);
    }

    public LongProperty totalBookBorrowedProperty() {
        return totalBookBorrowed;
    }
}
