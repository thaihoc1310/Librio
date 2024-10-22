package librio.models;

import java.time.LocalDate;

public class Librarian extends User {
    public Librarian() {
        super();
    }

    public Librarian(String id, String name, String email, String phoneNumber, String address,
                     Gender gender, Role role, String avatar, LocalDate birthOfDate) {
        super(id, name, email, phoneNumber, address, gender, role, avatar, birthOfDate);
    }
}
