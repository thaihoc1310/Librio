package librio.models;

import java.time.LocalDate;

public class Librarian extends User {
    public Librarian() {
        super();
    }

    public Librarian(String id, String name, String email, String password, String phoneNumber, String address,
                     Gender gender, Role role, String avatar, LocalDate birthOfDate) {
        super(id, name, email, password, phoneNumber, address, gender, role, avatar, birthOfDate);
    }
}
