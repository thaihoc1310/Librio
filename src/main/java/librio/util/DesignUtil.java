package librio.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Circle;

public class DesignUtil {
    public static void cropAndClipToCircle(Image avatarImage, ImageView avatarImageView, double radius) {
        // Lấy chiều rộng và chiều cao của ảnh
        double width = avatarImage.getWidth();
        double height = avatarImage.getHeight();

        // Tính toán kích thước để cắt ảnh thành hình vuông
        double cropSize = Math.min(width, height);  // Chọn kích thước nhỏ hơn giữa width và height

        // Tính toán tọa độ bắt đầu để cắt hình vuông từ trung tâm của ảnh
        double x = (width - cropSize) / 2;
        double y = (height - cropSize) / 2;

        // Cắt ảnh thành hình vuông
        PixelReader reader = avatarImage.getPixelReader();
        WritableImage squareImage = new WritableImage(reader, (int) x, (int) y, (int) cropSize, (int) cropSize);

        // Hiển thị ảnh đã cắt trong ImageView
        avatarImageView.setImage(squareImage);
        avatarImageView.setPreserveRatio(true);

        // Tạo clip hình tròn với bán kính được cung cấp và tâm tại (radius, radius)
        Circle clip = new Circle(radius, radius, radius);
        avatarImageView.setClip(clip);  // Thiết lập clip hình tròn cho ImageView
    }
}
