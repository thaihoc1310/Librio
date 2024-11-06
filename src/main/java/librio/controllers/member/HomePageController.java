package librio.controllers.member;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class HomePageController implements Initializable {

    @FXML
    private ImageView avatar;

    @FXML
    private ComboBox<?> filterBox;

    @FXML
    private Button leftMainBannerButton;

    @FXML
    private Button leftScrollButton1;

    @FXML
    private Button leftScrollButton2;

    @FXML
    private ImageView mainBanner0;

    @FXML
    private ImageView mainBanner1;

    @FXML
    private ImageView mainBanner2;

    @FXML
    private ScrollPane mainBannerScroll;

    @FXML
    private HBox mainBannerContainer;

    @FXML
    private Button rightMainBannerButton;

    @FXML
    private Button rightScrollButton1;

    @FXML
    private Button rightScrollButton2;

    @FXML
    private ImageView searchButton;

    @FXML
    private TextField searchTextField;

    private Timeline autoScrollTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        leftMainBannerButton.setOnMouseClicked(event -> scrollMainBanner(-1));
        rightMainBannerButton.setOnMouseClicked(event -> scrollMainBanner(1));
        startAutoScroll();
    }

    private void scrollMainBanner(int direction) {
        double currentHValue = mainBannerScroll.getHvalue();
        final double targetHValue = getTargetHValue(direction, currentHValue);

        Node currentBanner = getCurrentBanner(currentHValue);
        Node nextBanner = getCurrentBanner(targetHValue);

        if (currentBanner != null && nextBanner != null) {
            // Fade out current banner
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), currentBanner);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.7);

            // Fade in next banner
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), nextBanner);
            fadeIn.setFromValue(0.7);
            fadeIn.setToValue(1.0);

            // Set scroll position after fade out
            fadeOut.setOnFinished(event -> {
                mainBannerScroll.setHvalue(targetHValue);
                fadeIn.play();
            });

            fadeOut.play();
        }
    }

    private double getTargetHValue(int direction, double currentHValue) {
        double scrollAmount = 1.0 / (mainBannerContainer.getChildren().size() - 1); // Calculate scroll increment based on the number of banners
        final double targetHValue;

        if (direction == -1 && currentHValue == 0) {
            targetHValue = 1; // Wrap around to the last banner if at the beginning
        } else if (direction == 1 && currentHValue == 1) {
            targetHValue = 0; // Wrap around to the first banner if at the end
        } else {
            targetHValue = currentHValue + (scrollAmount * direction);
        }
        return targetHValue;
    }

    private Node getCurrentBanner(double hValue) {
        int index = (int) Math.round(hValue * (mainBannerContainer.getChildren().size() - 1));
        if (index >= 0 && index < mainBannerContainer.getChildren().size()) {
            return mainBannerContainer.getChildren().get(index);
        }
        return null;
    }

    private void startAutoScroll() {
        autoScrollTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> scrollMainBanner(1)));
        autoScrollTimeline.setCycleCount(Timeline.INDEFINITE);
        autoScrollTimeline.play();
    }

}
