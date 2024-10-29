package librio.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import librio.controllers.auth.Session;

import java.io.IOException;

public class LogoutController {
    @FXML
    private Button cancelButton;

    private Stage ownerStage;
private StackPane stackPaneRoot;
    public void setOwnerStage(Stage ownerStage) {
        this.ownerStage = ownerStage;
    }
    public void setStackPaneRoot(StackPane stackPaneRoot) {
        this.stackPaneRoot = stackPaneRoot;
    }
    @FXML
    void cancel() throws IOException {
        stackPaneRoot.setOpacity(1.0);
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void logOut() throws IOException {
        Stage currenStage = (Stage) cancelButton.getScene().getWindow();
        Stage stage = new Stage();
        stage.setTitle("Librio");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent loginRoot  = loader.load();
        stage.setScene(new Scene(loginRoot));
        stage.show();
        Session.getInstance().logout();

        if (ownerStage != null) {
            ownerStage.close();
        }

        currenStage.close();
    }

}
