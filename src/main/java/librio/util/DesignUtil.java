package librio.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import librio.cache.ImageCache;
import librio.session.Session;
import librio.models.Book;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

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

        Image image = ImageCache.getInstance().getImage(defaultImage,defaultImage);
        bookImageView.setImage(image);
    }

    public static void setConfirmButton(Button confirmButton, Book book) {
        int availableCopy = book.getAvailableCopy();
        boolean isAlreadyBorrowed = checkIfUserBorrowedBook(Session.getInstance().getLoggedInUser(), book);
        confirmButton.setUserData(book.getId());
        if (availableCopy == 0) {
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
        button.setCursor(isEnabled ? Cursor.HAND : Cursor.DEFAULT);

        if (!isEnabled) {
            button.setOnAction(null);
            button.setOpacity(0.5);
            button.setOnMouseEntered(null);
            button.setOnMouseExited(null);
        } else {
            button.setOpacity(1.0); // Đảm bảo nút rõ ràng khi khả dụng
            button.setOnMouseEntered(e -> button.setStyle("-fx-text-fill: #943f20;"));
            button.setOnMouseExited(e -> button.setStyle("-fx-text-fill: " + color + ";"));
        }
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

    public static void setDatePickerFormat(DatePicker datePicker) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date == null) ? "" : date.format(formatter);
            }

            @Override
            public LocalDate fromString(String string) {
                String dateRegex = "^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/\\d{4}$";
                if (string.isEmpty()) {
                    return null;
                }

                if (string.matches(dateRegex)) {
                    try {
                        return LocalDate.parse(string, formatter);
                    } catch (DateTimeParseException e) {
                        return null;
                    }
                } else {
                    return datePicker.getValue();
                }
            }
        });
    }

    public static HBox getStarBox(Book book) {
        HBox starBox = new HBox(5);
        double rating = book.getAverageOfRating();
        int fullStars = (int) rating;
        double decimalPart = rating - fullStars;
        Image fullStarImage = new Image(DesignUtil.class.getResource("/icons/MemberIcon/Star.png").toExternalForm());
        Image emptyStarImage = new Image(DesignUtil.class.getResource("/icons/MemberIcon/Star_notfill.png").toExternalForm());
        for (int i = 1; i <= 5; i++) {
            StackPane starPane = new StackPane();

            ImageView emptyStar = new ImageView(emptyStarImage);
            emptyStar.setFitHeight(15);
            emptyStar.setFitWidth(15);

            starPane.getChildren().add(emptyStar);

            if (i <= fullStars) {
                ImageView fullStar = new ImageView(fullStarImage);
                fullStar.setFitHeight(15);
                fullStar.setFitWidth(15);
                starPane.getChildren().add(fullStar);
            } else if (i == fullStars + 1 && decimalPart > 0) {
                ImageView fullStar = new ImageView(fullStarImage);
                fullStar.setFitHeight(15);
                fullStar.setFitWidth(15);

                Rectangle clip = new Rectangle(15 * decimalPart, 15);
                fullStar.setClip(clip);
                starPane.getChildren().add(fullStar);
            }

            starBox.getChildren().add(starPane);
        }
        return starBox;
    }

    public static void generateAndDisplayQRCode(ImageView qrCodeImageView, Book book) {
        String bookUrl = "https://books.google.com.vn/books?vid=ISBN" + book.getIsbn() + "&redir_esc=y";
        int size = 350;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix bitMatrix = qrCodeWriter.encode(bookUrl, BarcodeFormat.QR_CODE, size, size, hints);

            WritableImage qrCodeImage = new WritableImage(size, size);
            PixelWriter pixelWriter = qrCodeImage.getPixelWriter();

            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    Color color = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                    pixelWriter.setColor(x, y, color);
                }
            }

            qrCodeImageView.setImage(qrCodeImage);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    public static void setAvatarAndUserName(ImageView avatarUser, Label userNameUser) {
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        String path = avatarsDir + Session.getInstance().getLoggedInUser().getAvatar();
        Image image = ImageCache.getInstance().getImage(path,avatarsDir + "Male User.png");
        cropAndClipToCircle(image, avatarUser, 38.5);
        userNameUser.setText(Session.getInstance().getLoggedInUser().getName());
    }

    public static void switchScene(Button button, String path) {
        try {
            FXMLLoader loader = new FXMLLoader(DesignUtil.class.getResource(path));
            Parent manageBookRoot = loader.load();

            Stage currentStage = (Stage)button.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(manageBookRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
