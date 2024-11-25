package librio.session;

import librio.enums.Role;
import librio.models.User;

/**
 * The Session class is a singleton that manages the current user session.
 * It maintains information about the logged-in user and provides methods
 * to manage the user's session state.
 */
public class Session {
    private static Session instance;
    private User loggedInUser;
    private int totalBooks;
    private Session() {}

    /**
     * Returns the singleton instance of the Session class.
     * If the instance does not exist, it will be created.
     *
     * @return the singleton instance of the Session class.
     */
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

    /**
     * Retrieves the role of the currently logged-in user.
     *
     * @return the role of the logged-in user, or null if no user is logged in.
     */
    public Role getUserRole() {
        if (loggedInUser != null) {
            return loggedInUser.getRole();
        }
        return null;
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return true if a user is logged in, false otherwise.
     */
    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    /**
     * Logs out the currently logged-in user by setting the loggedInUser field to null.
     * This will effectively terminate the user's session.
     */
    public void logout() {
        loggedInUser = null;
    }
    public void setTotalBooks(int totalBooks) {
        this.totalBooks = totalBooks;
    }

    public int getTotalBooks() {
        return totalBooks;
    }
}

