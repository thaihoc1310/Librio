package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import librio.auth.Session;
import librio.database.DatabaseConnection;
import librio.models.Borrow;
import librio.models.Status;

import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class UpdateBorrowController implements Initializable {
    @FXML
    private Button backButton;

    @FXML
    private DatePicker borrowDatePicker;

    @FXML
    private DatePicker dueDatePicker;

    @FXML
    private Label fineLabel;

    @FXML
    private DatePicker returnDatePicker;

    @FXML
    private Label statusLabel;

    @FXML
    private Label borrowDateErrorLabel;

    @FXML
    private Label dueDateErrorLabel;

    @FXML
    private Label returnDateErrorLabel;

    private Borrow borrow;

    @FXML
    void back() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    public void setBorrow(Borrow borrow) {
        this.borrow = borrow;
        populateFields();
    }

    private void populateFields() {
        if (borrow != null) {
            borrowDatePicker.setValue(borrow.getBorrowDate());
            dueDatePicker.setValue(borrow.getDueDate());
            if (borrow.getReturnDate() != null) {
                returnDatePicker.setValue(borrow.getReturnDate());
            } else {
                returnDatePicker.setValue(null);
            }
            statusLabel.setText(borrow.getStatus().toString());
            fineLabel.setText(borrow.getFine() + " VNĐ");
        }
    }

    @FXML
    private void updateBorrow() {
        LocalDate borrowDate = borrowDatePicker.getValue();
        LocalDate dueDate = dueDatePicker.getValue();
        LocalDate returnDate = returnDatePicker.getValue();
        Status status = Status.valueOf(statusLabel.getText());
        double fine = Double.parseDouble(fineLabel.getText().replace(" VNĐ", ""));
        boolean validation = false;

        if (borrowDate.isAfter(LocalDate.now())){
            borrowDateErrorLabel.setText("Borrow Date must not be after current Date!");
            validation = true;
        }

        if (borrowDate.isAfter(dueDate)) {
            dueDateErrorLabel.setText("Due Date must be after Borrow Date!");
            validation = true;
        }

        if (returnDate != null) {
            if(borrowDate.isAfter(returnDate)){
                returnDateErrorLabel.setText("Return Date must be after Borrow Date!");
                validation = true;
            } else if (returnDate.isAfter(LocalDate.now())) {
                returnDateErrorLabel.setText("Return Date must not be after current Date!");
                validation = true;
            } else if (ChronoUnit.DAYS.between(borrowDate, dueDate) > 60) {
                dueDateErrorLabel.setText("The borrowing period cannot exceed 60 days!");
                validation = true;
            }
        }

        if (validation) {
            return;
        }
        String query = "UPDATE borrows SET borrow_date = ?, due_date = ?, return_date = ?, status = ?, fine = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDate(1, Date.valueOf(borrowDate));
            statement.setDate(2, Date.valueOf(dueDate));
            statement.setDate(3, returnDate != null ? Date.valueOf(returnDate) : null);
            statement.setString(4,status.name());
            statement.setString(5,String.valueOf(fine));
            statement.setString(6, Session.getInstance().getLoggedInUser().getEmail());
            statement.setInt(7, borrow.getId());


            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
//                clearInputFields();
                closeStage();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hideErrorLabels();
        borrowDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateStatus());
        dueDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateStatus());
        returnDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateStatus());
    }


    private void updateStatus() {
        LocalDate borrowDate = borrowDatePicker.getValue() == null ? borrow.getBorrowDate() : borrowDatePicker.getValue();
        LocalDate dueDate = dueDatePicker.getValue() == null ? borrow.getDueDate() : dueDatePicker.getValue();
        LocalDate returnDate = returnDatePicker.getValue();
        boolean validation = false;

        dueDatePicker.setOnMouseClicked(event -> {hideErrorLabels();});
        returnDatePicker.setOnMouseClicked(event -> {hideErrorLabels();});

        if (validation) {
            return;
        }

        double fine = 0;
        Status newStatus;

        if (returnDate == null) {
            if (LocalDate.now().isAfter(dueDate)) {
                newStatus = Status.OVERDUE;
                long overdueDays = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
                fine = overdueDays * 5000 < 100000 ? overdueDays * 5000 : 100000;
            } else {
                newStatus = Status.BORROWING;
            }
        } else {
            if (returnDate.isAfter(dueDate)) {
                newStatus = Status.RETURNED_LATE;
                long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
                fine = overdueDays * 5000 < 100000 ? overdueDays * 5000 : 100000;;
            } else {
                newStatus = Status.RETURNED;
                fine = 0;
            }
        }

        statusLabel.setText(newStatus.toString());
        fineLabel.setText(String.valueOf(fine));
    }


    private void hideErrorLabels() {
        borrowDateErrorLabel.setText("");
        dueDateErrorLabel.setText("");
        returnDateErrorLabel.setText("");
    }

}
