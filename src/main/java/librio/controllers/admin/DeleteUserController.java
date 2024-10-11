package librio.controllers.admin;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import librio.models.User;

public class DeleteUserController implements Initializable {
    private User user;

    @FXML
    private Button deleteButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Text confirmationText;

    private ManageUserController manageUserController;

    public void setManageUserController(ManageUserController manageUserController) {
        this.manageUserController = manageUserController;
    }

    @FXML
    private void deleteUser(ActionEvent event) {

    }

    @FXML
    private void cancel(ActionEvent event) {
        closeWindow();
    }

    public void setUser(User user) {
        this.user = user;
    }

    private void closeWindow() {
        // Đóng cửa sổ hiện tại
        Stage stage = (Stage) deleteButton.getScene().getWindow(); // Hoặc có thể sử dụng cancelButton
        stage.close();
    }

    public void setUserName(String userName){
        confirmationText.setText("Are you sure you want to delete user \"" + userName + "\"?");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
