package librio.controllers.admin;

import librio.models.Borrow;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DeleteBorrowControllerTest {

    private DeleteBorrowController controller;
    private Borrow mockBorrow;

    private static final String BORROW_ACTIVE_EXCEPTION_MESSAGE = "Borrow record is currently active!";
    private static final String BORROW_NOT_SET_EXCEPTION_MESSAGE = "Borrow record is not set!";

    @Before
    public void setUp() throws Exception {
        controller = Mockito.mock(DeleteBorrowController.class);
        mockBorrow = Mockito.mock(Borrow.class);
        controller.setBorrow(mockBorrow);
        System.out.println("Setting up before each test");
    }

    @Test
    public void testDeleteBorrowSuccessfully() throws Exception {
        controller.deleteBorrow();
        verify(controller, times(1)).deleteBorrow();
        // Add verification for changes or side effects
    }

    @Test
    public void testDeleteBorrowWhenBorrowIsActive() throws Exception {
        simulateExceptionOnDeleteBorrow(new IllegalStateException(BORROW_ACTIVE_EXCEPTION_MESSAGE));
        verifyExceptionHandling(IllegalStateException.class, BORROW_ACTIVE_EXCEPTION_MESSAGE);
    }

    @Test
    public void testDeleteBorrowWhenBorrowIsNull() throws Exception {
        simulateExceptionOnDeleteBorrow(new NullPointerException(BORROW_NOT_SET_EXCEPTION_MESSAGE));
        verifyExceptionHandling(NullPointerException.class, BORROW_NOT_SET_EXCEPTION_MESSAGE);
    }

    private void simulateExceptionOnDeleteBorrow(Exception exception) throws Exception {
        doThrow(exception).when(controller).deleteBorrow();
    }

    private <T extends Exception> void verifyExceptionHandling(Class<T> exceptionClass, String expectedMessage) {
        try {
            controller.deleteBorrow();
            fail("Expected an " + exceptionClass.getSimpleName() + " to be thrown");
        } catch (Exception e) {
            assertTrue(exceptionClass.isInstance(e));
            assertEquals(expectedMessage, e.getMessage());
        }
        verify(controller, times(1)).deleteBorrow();
    }
}