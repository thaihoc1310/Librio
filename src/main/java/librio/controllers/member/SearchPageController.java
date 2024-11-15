package librio.controllers.member;

import javafx.fxml.Initializable;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class SearchPageController implements Initializable {
    @FXML
    private ImageView avatar;

    @FXML
    private TitledPane categoryPane;

    @FXML
    private ComboBox<?> filterBox;

    @FXML
    private FlowPane flowPane;

    @FXML
    private ComboBox<?> limitBox;

    @FXML
    private ScrollPane mainScroll;

    @FXML
    private TitledPane ratePane;

    @FXML
    private ImageView searchButton;

    @FXML
    private TextField searchTextField;

    @FXML
    private ComboBox<?> sortBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupAnimatedPane(ratePane, 255);
        setupAnimatedPane(categoryPane, 454);
    }

    private void setupAnimatedPane(TitledPane pane, double targetHeight) {
        pane.setExpanded(false);
        pane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Tạo animation mở rộng chiều cao
                Timeline expandTimeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(pane.prefHeightProperty(), 0)),
                        new KeyFrame(Duration.seconds(0.3), new KeyValue(pane.prefHeightProperty(), targetHeight))
                );
                expandTimeline.play();
            } else {
                // Tạo animation thu nhỏ chiều cao
                Timeline collapseTimeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(pane.prefHeightProperty(), targetHeight)),
                        new KeyFrame(Duration.seconds(0.3), new KeyValue(pane.prefHeightProperty(), 0))
                );
                collapseTimeline.play();
            }
        });
    }




}
