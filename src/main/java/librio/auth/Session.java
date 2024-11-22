package librio.auth;

import librio.enums.Role;
import librio.models.User;

public class Session {
    private static Session instance;
    private User loggedInUser;

    private Session() {}

    // Singleton pattern to ensure only one session is active at a time
    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public Role getUserRole() {
        if (loggedInUser != null) {
            return loggedInUser.getRole();
        }
        return null;
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public void logout() {
        loggedInUser = null;
    }
}
