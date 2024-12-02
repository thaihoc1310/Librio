package librio.controllers.auth;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import librio.models.User;
import librio.session.Session;
import librio.util.DatabaseUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LoginControllerTest {

    @BeforeAll
    public static void initToolkit() {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("JavaFX startup took too long.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("JavaFX initialization interrupted", e);
        }
    }

    @Test
    public void testInvalidLogin() {
        Platform.runLater(() -> {
            LoginController contr = new LoginController();
            User mockUser = null;
            Session mockSession = mock(Session.class);

            contr.usernameField = new TextField("InvalidEmail");
            contr.passwordField = new PasswordField();
            contr.passwordField.setText("InvalidPassword");
            contr.incorrectLoginInformation = new Label("");

            when(mockSession.getLoggedInUser()).thenReturn(mockUser);
            try {
                contr.handleLogin();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            assertEquals("Incorrect login information!", contr.incorrectLoginInformation.getText());
        });
    }

    @Test
    public void testValidLogin() {
        Platform.runLater(() -> {
            LoginController contr = new LoginController();
            User mockUser = new User();
            Session mockSession = mock(Session.class);

            contr.usernameField = new TextField("ValidEmail");
            contr.passwordField = new PasswordField();
            contr.passwordField.setText("ValidPassword");
            contr.incorrectLoginInformation = new Label("");
            try (MockedStatic<DatabaseUtil> mockedDatabaseUtil = Mockito.mockStatic(DatabaseUtil.class)) {
                mockedDatabaseUtil.when(() -> DatabaseUtil.authenticate("ValidEmail", "ValidPassword")).thenReturn(mockUser);
                when(mockSession.getLoggedInUser()).thenReturn(mockUser);
                contr.handleLogin();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            assertEquals("", contr.incorrectLoginInformation.getText());
        });
    }

    @Test
    public void testSwitchToSignInFromSignUp() {
        Platform.runLater(() -> {
            LoginController contr = new LoginController();
            contr.leftPane = new AnchorPane();
            contr.rightPane = new AnchorPane();
            contr.centerPane = new AnchorPane();
            contr.leftPane.setTranslateX(-300);
            contr.centerPane.setVisible(true);
            contr.rightPane.setOpacity(0);

            contr.switchToSignIn(contr.centerPane);

            assertEquals(0, contr.leftPane.getTranslateX());
            assertFalse(contr.centerPane.isVisible());
            assertEquals(1, contr.rightPane.getOpacity());
        });
    }

    @Test
    public void testSwitchToSignInFromForgotPassword() {
        Platform.runLater(() -> {
            LoginController contr = new LoginController();
            contr.leftPane = new AnchorPane();
            contr.rightPane = new AnchorPane();
            contr.changePassWordPane = new AnchorPane();
            contr.leftPane.setTranslateX(-300);
            contr.changePassWordPane.setVisible(true);
            contr.rightPane.setOpacity(0);

            contr.switchToSignIn(contr.changePassWordPane);

            assertEquals(0, contr.leftPane.getTranslateX());
            assertFalse(contr.changePassWordPane.isVisible());
            assertEquals(1, contr.rightPane.getOpacity());
        });
    }

    @Test
    public void testSwitchToSignUpFromSignIn() {
        Platform.runLater(() -> {
            LoginController contr = new LoginController();
            contr.leftPane = new AnchorPane();
            contr.rightPane = new AnchorPane();
            contr.centerPane = new AnchorPane();
            contr.rightPane.setOpacity(1);
            contr.centerPane.setVisible(true);

            contr.switchToSignUpAndForgotPassword(contr.centerPane);

            assertEquals(-300, contr.leftPane.getTranslateX());
            assertFalse(contr.centerPane.isVisible());
            assertEquals(0, contr.rightPane.getOpacity());
        });
    }

    @Test
    public void testSwitchToForgotPasswordFromSignIn() {
        Platform.runLater(() -> {
            LoginController contr = new LoginController();
            contr.leftPane = new AnchorPane();
            contr.rightPane = new AnchorPane();
            contr.centerPane = new AnchorPane();
            contr.sendCodePane = new AnchorPane();
            contr.rightPane.setOpacity(1);
            contr.centerPane.setVisible(true);

            contr.switchToSignUpAndForgotPassword(contr.centerPane);

            assertEquals(-300, contr.leftPane.getTranslateX());
            assertFalse(contr.centerPane.isVisible());
            assertEquals(0, contr.rightPane.getOpacity());
        });
    }

    @Test
    public void testSignUpWithEmptyFields() {
        Platform.runLater(() -> {
            LoginController controller = new LoginController();
            User mockUser = Mockito.mock(User.class);
            Session mockSession = Mockito.mock(Session.class);
            controller.usernameField = new TextField();
            controller.passwordField = new PasswordField();
            controller.incorrectLoginInformation = new Label();

            try {
                controller.handleSignUp();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            assertEquals("Email cannot be empty!", controller.incorrectLoginInformation.getText());
        });
    }


    @Test
    public void testSignUpWithValidFields() {
        Platform.runLater(() -> {
            LoginController controller = new LoginController();
            User mockUser = new User();
            Session mockSession = Mockito.mock(Session.class);

            controller.usernameField = new TextField("ValidEmail@site.com");
            controller.passwordField = new PasswordField();
            controller.passwordField.setText("ValidPassword");
            controller.incorrectLoginInformation = new Label("");

            when(mockSession.getLoggedInUser()).thenReturn(mockUser);
            try {
                controller.handleSignUp();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            assertEquals("", controller.incorrectLoginInformation.getText());
        });
    }
}
