package librio.models;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.LongProperty;

import java.time.LocalDate;

public class Member extends User {
    private LongProperty fineAmount;
    private LongProperty totalBookBorrowed;

    public Member() {
        super();
        this.fineAmount = new SimpleLongProperty();
        this.totalBookBorrowed = new SimpleLongProperty();
    }

    public Member(String id, String name, String email, String phoneNumber, String address, Gender gender, Role role,
                  String avatar, LocalDate birthOfDate, long fineAmount, long totalBookBorrowed) {
        super(id, name, email, phoneNumber, address, gender, role, avatar, birthOfDate);
        this.fineAmount = new SimpleLongProperty(fineAmount);
        this.totalBookBorrowed = new SimpleLongProperty(totalBookBorrowed);
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
