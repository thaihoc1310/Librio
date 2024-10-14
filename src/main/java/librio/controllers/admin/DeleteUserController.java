package librio.controllers.admin;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import librio.models.User;
import librio.util.DatabaseUtil;
public class DeleteUserController implements Initializable {
    @FXML
    private Button deleteButton;

    private User user;
    private ManageUserController manageUserController;
    private int currentPage = 0;

    public void setManageUserController(ManageUserController manageUserController) {
        this.manageUserController = manageUserController;
    }

    public void setCurrentPage(int currentPage){
        this.currentPage = currentPage;
    }

    @FXML
    private void deleteUser() {
        DatabaseUtil.deleteUser(user);
        if (manageUserController != null) {
            manageUserController.loadUsers(null,currentPage);
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

    }
}
