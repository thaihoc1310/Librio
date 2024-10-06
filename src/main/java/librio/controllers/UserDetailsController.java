package librio.controllers;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

/**
 *
 * @author WINDOWS 10
 */
public class UserDetailsController implements Initializable {

    @FXML
    private AnchorPane main_form;

    @FXML
    private TextField edit_patientID;

    @FXML
    private TextField edit_name;

    @FXML
    private ComboBox<String> edit_gender;

    @FXML
    private TextField edit_contactNumber;

    @FXML
    private TextArea edit_address;

    @FXML
    private ComboBox<String> edit_status;

    @FXML
    private Button edit_updateBtn;


    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    public void updateBtn() {


    }

    // CLOSE THE EDITPATIENTFORM FXML FILE AND OPEN IT AGAIN
    public void setField() {
//        edit_patientID.setText(String.valueOf(User.temp_PatientID));
//        edit_name.setText(User.temp_name);
//        edit_gender.getSelectionModel().select(User.temp_gender);
//        edit_contactNumber.setText(String.valueOf(User.temp_number));
//        edit_address.setText(User.temp_address);
//        edit_status.getSelectionModel().select(User.temp_status);
    }

    public void genderList() {
//        List<String> genderL = new ArrayList<>();
//
//        for (String data : Data.gender) {
//            genderL.add(data);
//        }
//
//        ObservableList listData = FXCollections.observableList(genderL);
//        edit_gender.setItems(listData);
    }

    public void statusList() {
//        List<String> statusL = new ArrayList<>();
//
//        for (String data : Data.status) {
//            statusL.add(data);
//        }
//
//        ObservableList listData = FXCollections.observableList(statusL);
//        edit_status.setItems(listData);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        setField();
//        genderList();
//        statusList();
    }

}
