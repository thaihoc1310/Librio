package librio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import librio.database.DatabaseInitializer;
import librio.util.DatabaseUtil;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Khởi tạo cơ sở dữ liệu khi bắt đầu ứng dụng
        DatabaseInitializer.initializeDatabase();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/fxml/Login.fxml"));
        DatabaseUtil.startAutoUpdate();
        ///fxml/member/Book.fxml
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Librio");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "true");
        System.setProperty("prism.text", "true");
        launch();
    }
}
