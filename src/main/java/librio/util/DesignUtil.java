package librio.util;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import librio.auth.Session;
import librio.models.Book;

import java.io.File;

import static librio.util.DatabaseUtil.checkIfUserBorrowedBook;

public class DesignUtil {
    public static void cropAndClipToCircle(Image avatarImage, ImageView avatarImageView, double radius) {

        double width = avatarImage.getWidth();
        double height = avatarImage.getHeight();

        double cropSize = Math.min(width, height);

        double x = (width - cropSize) / 2;
        double y = (height - cropSize) / 2;

        PixelReader reader = avatarImage.getPixelReader();
        WritableImage squareImage = new WritableImage(reader, (int) x, (int) y, (int) cropSize, (int) cropSize);

        avatarImageView.setImage(squareImage);
        avatarImageView.setPreserveRatio(true);

        Circle clip = new Circle(radius, radius, radius);
        avatarImageView.setClip(clip);
    }

    public static void cropToAspectRatio(Image avatarImage, ImageView avatarImageView, double targetWidth, double targetHeight) {
        double width = avatarImage.getWidth();
        double height = avatarImage.getHeight();

        double targetRatio = targetWidth / targetHeight;
        double imageRatio = width / height;

        double cropWidth, cropHeight;
        if (imageRatio > targetRatio) {
            cropHeight = height;
            cropWidth = height * targetRatio;
        } else {
            cropWidth = width;
            cropHeight = width / targetRatio;
        }

        double x = (width - cropWidth) / 2;
        double y = (height - cropHeight) / 2;

        PixelReader reader = avatarImage.getPixelReader();
        WritableImage croppedImage = new WritableImage(reader, (int) x, (int) y, (int) cropWidth, (int) cropHeight);

        avatarImageView.setImage(croppedImage);
        avatarImageView.setFitWidth(targetWidth);
        avatarImageView.setFitHeight(targetHeight);
        avatarImageView.setSmooth(true);
        avatarImageView.setPreserveRatio(false);
    }

    public static void loadDefaultBookImage(ImageView bookImageView) {
        String projectDir = System.getProperty("user.dir");
        String booksDir = projectDir + "/src/main/resources/images/book/";
        String defaultImage = booksDir + "defaultBook.jpg";
        File defaultImageFile = new File(defaultImage);
        if (defaultImageFile.exists()) {
            Image image = new Image(defaultImageFile.toURI().toString());
            bookImageView.setImage(image);
            bookImageView.setSmooth(true);
        }
    }

    public static void truncateTextToFit(Text textNode, double maxWidth, int maxLines) {
        String originalText = textNode.getText();
        String ellipsis = "...";
        textNode.setText(originalText);

        double lineHeight = textNode.getFont().getSize();
        double maxHeight = lineHeight * maxLines;

        while ((textNode.getLayoutBounds().getWidth() > maxWidth || textNode.getLayoutBounds().getHeight() > maxHeight) && originalText.length() > 0) {
            originalText = originalText.substring(0, originalText.length() - 1);
            textNode.setText(originalText + ellipsis);
        }
    }

    public static void setConfirmButton(Button confirmButton, Book book) {
        int quantityOfCopy = book.getQuantityCopy();
        boolean isAlreadyBorrowed = checkIfUserBorrowedBook(Session.getInstance().getLoggedInUser(), book);
        confirmButton.setUserData(book.getId());
        if (quantityOfCopy == 0) {
            updateBorrowButton(confirmButton, "OUT OF STOCK", "#9e4b3e", false);
        } else if (isAlreadyBorrowed) {
            updateBorrowButton(confirmButton, "BORROWING", "#b57a3e", false);
        } else {
            updateBorrowButton(confirmButton, "QUICK BORROW", "#6e2f18", true);
        }
    }

    public static void updateBorrowButton(Button button, String text, String color, boolean isEnabled) {
        button.setText(text);
        button.setStyle("-fx-border-color: " + color + "; -fx-text-fill: " + color);
        button.setDisable(!isEnabled);
        button.setCursor(isEnabled ? Cursor.HAND : Cursor.DEFAULT);
        button.setOnMouseEntered(e -> button.setStyle("-fx-text-fill: #943f20;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-text-fill: #6e2f18;"));
    }

    public static void updateButtonInContainer(HBox container, Book book) {
        for (Node node : container.getChildren()) {
            if (node instanceof AnchorPane) {
                AnchorPane bookPane = (AnchorPane) node;
                Button returnButton = (Button) bookPane.lookup(".quick-borrow-button");
                if (returnButton != null) {
                    if (returnButton.getUserData() != null && book.getId() == (int) returnButton.getUserData()) {
                        setConfirmButton(returnButton, book);
                    }
                }
            }
        }
    }

}
