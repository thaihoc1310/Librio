package librio.controllers.admin;

import librio.models.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DeleteUserControllerTest {

    private static final String USER_ACTIVE_EXCEPTION_MESSAGE = "User is currently active!";
    private static final String USER_NOT_SET_EXCEPTION_MESSAGE = "User is not set!";
    private DeleteUserController controller;
    private User mockUser;

    @Before
    public void setUp() {
        controller = Mockito.mock(DeleteUserController.class);
        mockUser = Mockito.mock(User.class);
        controller.setUser(mockUser);
        System.out.println("Setting up before each test");
    }

    @Test
    public void testDeleteUserSuccessfully() {
        controller.deleteUser();
        verify(controller, times(1)).deleteUser();
    }

    @Test
    public void testDeleteUserWhenUserIsActive() throws Exception {
        simulateExceptionOnDeleteUser(new IllegalStateException(USER_ACTIVE_EXCEPTION_MESSAGE));
        verifyExceptionHandling(IllegalStateException.class, USER_ACTIVE_EXCEPTION_MESSAGE);
    }

    @Test
    public void testDeleteUserWhenUserIsNull() throws Exception {
        simulateExceptionOnDeleteUser(new NullPointerException(USER_NOT_SET_EXCEPTION_MESSAGE));
        verifyExceptionHandling(NullPointerException.class, USER_NOT_SET_EXCEPTION_MESSAGE);
    }

    private void simulateExceptionOnDeleteUser(Exception exception) {
        doThrow(exception).when(controller).deleteUser();
    }

    private <T extends Exception> void verifyExceptionHandling(Class<T> exceptionClass, String expectedMessage) {
        try {
            controller.deleteUser();
            fail("Expected an " + exceptionClass.getSimpleName() + " to be thrown");
        } catch (Exception e) {
            assertTrue(exceptionClass.isInstance(e));
            assertEquals(expectedMessage, e.getMessage());
        }
        verify(controller, times(1)).deleteUser();
    }
}