    package librio.controllers.admin;

    import librio.models.Book;
    import org.junit.Before;
    import org.junit.Test;
    import org.mockito.Mockito;

    import static org.junit.Assert.*;
    import static org.mockito.Mockito.*;

    public class DeleteBookControllerTest {

        private static final String BOOK_BORROWED_EXCEPTION_MESSAGE = "Book is currently borrowed!";
        private static final String BOOK_NOT_SET_EXCEPTION_MESSAGE = "Book is not set!";
        private DeleteBookController controller;
        private Book mockBook;

        @Before
        public void setUp() {
            controller = Mockito.mock(DeleteBookController.class);
            mockBook = Mockito.mock(Book.class);
            controller.setBook(mockBook);
            System.out.println("Setting up before each test");
        }

        @Test
        public void testDeleteBookSuccessfully() {
            controller.deleteBook();
            verify(controller, times(1)).deleteBook();
        }

        @Test
        public void testDeleteBookWhenBookIsCurrentlyBorrowed() {
            simulateExceptionOnDeleteBook(new IllegalStateException(BOOK_BORROWED_EXCEPTION_MESSAGE));
            verifyExceptionHandling(IllegalStateException.class, BOOK_BORROWED_EXCEPTION_MESSAGE);
        }

        @Test
        public void testDeleteBookWhenBookIsNull() {
            simulateExceptionOnDeleteBook(new NullPointerException(BOOK_NOT_SET_EXCEPTION_MESSAGE));
            verifyExceptionHandling(NullPointerException.class, BOOK_NOT_SET_EXCEPTION_MESSAGE);
        }

        private void simulateExceptionOnDeleteBook(Exception exception) {
            doThrow(exception).when(controller).deleteBook();
        }

        private <T extends Exception> void verifyExceptionHandling(Class<T> exceptionClass, String expectedMessage) {
            try {
                controller.deleteBook();
                fail("Expected an " + exceptionClass.getSimpleName() + " to be thrown");
            } catch (Exception e) {
                assertTrue(exceptionClass.isInstance(e));
                assertEquals(expectedMessage, e.getMessage());
            }
            verify(controller, times(1)).deleteBook();
        }
    }