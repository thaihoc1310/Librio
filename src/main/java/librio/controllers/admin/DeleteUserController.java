package librio.controllers.admin;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import librio.database.DatabaseConnection;
import librio.models.BorrowedBook;
import librio.models.User;
import librio.util.DatabaseUtil;
public class DeleteUserController implements Initializable {
    @FXML
    private Button deleteButton;

    @FXML
    private Label errorLabel;

    private User user;



    @FXML
    private void deleteUser() {
        if(DatabaseUtil.checkIfUserBorrowingBook(user)){
            errorLabel.setVisible(true);
            return;
        }
        DatabaseUtil.deleteUser(user);
        String projectDir = System.getProperty("user.dir");
        String avatarsDir = projectDir + "/src/main/resources/images/user/";
        if (user.getAvatar() != null && !user.getAvatar().isEmpty() ) {
            File oldFile = new File(avatarsDir + user.getAvatar());
            if (oldFile.exists()) {
                boolean deleted = oldFile.delete();
                if (!deleted) {
                    System.out.println("Không thể xóa tệp ảnh cũ: " + oldFile.getAbsolutePath());
                }
            }
        }
        closeWindow();
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    public void setUser(User user) {
        this.user = user;
    }

    private void closeWindow() {
        Stage stage = (Stage) deleteButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        errorLabel.setVisible(false);
    }
}
