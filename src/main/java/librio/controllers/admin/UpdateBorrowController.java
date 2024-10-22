package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
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
            fineLabel.setText(String.valueOf(borrow.getFine()) + " VNĐ");
        }
    }

    @FXML
    private void updateBorrow() {
        LocalDate borrowDate = borrowDatePicker.getValue();
        LocalDate dueDate = dueDatePicker.getValue();
        LocalDate returnDate = returnDatePicker.getValue();
        boolean validation = false;

        if (borrowDate.isAfter(LocalDate.now())){
            borrowDateErrorLabel.setText("Borrow Date must not be after current Date!");
            validation = true;
        }else{
            borrowDateErrorLabel.setText("");
        }

        if (borrowDate.isAfter(dueDate)) {
            dueDateErrorLabel.setText("Due Date must be after Borrow Date!");
            validation = true;
        }else {
            dueDateErrorLabel.setText("");
        }

        if (returnDate != null) {
            if(borrowDate.isAfter(returnDate)){
                returnDateErrorLabel.setText("Return Date must be after Borrow Date!");
                validation = true;
            } else if (returnDate.isAfter(LocalDate.now())) {
                returnDateErrorLabel.setText("Return Date must not be after current Date!");
                validation = true;
            }else {
                returnDateErrorLabel.setText("");
            }
        }

        if (validation) {
            return;
        }
        String query = "UPDATE borrows SET borrow_date = ?, due_date = ?, return_date = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDate(1, Date.valueOf(borrowDate));
            statement.setDate(2, Date.valueOf(dueDate));
            statement.setDate(3, returnDate != null ? Date.valueOf(returnDate) : null);
            statement.setString(4, borrow.getId());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                Status newStatus = borrow.getStatus();
                if (returnDate == null) {
                    //Overdue but not returned
                    if (LocalDate.now().isAfter(dueDate)) {
                        newStatus = Status.OVERDUE;
                        long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
                        double fine = overdueDays * 5000.0;
                        updateFine(connection, borrow.getId(), fine);
                    } else {
                        newStatus = Status.BORROWING;
                        updateFine(connection, borrow.getId(), 0);
                    }
                } else {
                    // Trường hợp đã trả sách
                    if (returnDate.isAfter(dueDate)) {
                        newStatus = Status.RETURNED_LATE;
                        long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(dueDate, returnDate);
                        double fine = overdueDays * 5000.0;
                        updateFine(connection, borrow.getId(), fine);
                    } else {
                        newStatus = Status.RETURNED;
                        updateFine(connection, borrow.getId(), 0);
                    }
                }
                //Update new Status:
                String updateStatusQuery = "UPDATE borrows SET status = ? WHERE id = ?";
                try (PreparedStatement updateStatusStatement = connection.prepareStatement(updateStatusQuery)) {
                    updateStatusStatement.setString(1, newStatus.toString());
                    updateStatusStatement.setString(2, borrow.getId());
                    updateStatusStatement.executeUpdate();
                }
            }
            closeStage();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Update fine
    private void updateFine(Connection connection, String borrowId, double fine) throws SQLException {
        String updateFineQuery = "UPDATE borrows SET fine = ? WHERE id = ?";
        try (PreparedStatement updateFineStatement = connection.prepareStatement(updateFineQuery)) {
            updateFineStatement.setDouble(1, fine);
            updateFineStatement.setString(2, borrowId);
            updateFineStatement.executeUpdate();
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
        LocalDate borrowDate = borrowDatePicker.getValue();
        LocalDate dueDate = dueDatePicker.getValue();
        LocalDate returnDate = returnDatePicker.getValue();
        boolean validation = false;

        if (borrowDate.isAfter(LocalDate.now())){
            borrowDateErrorLabel.setText("Borrow Date must not be after current Date!");
            validation = true;
        } else {
            borrowDateErrorLabel.setText("");
        }

        if (borrowDate.isAfter(dueDate)) {
            dueDateErrorLabel.setText("Due Date must be after Borrow Date!");
            validation = true;
        } else {
            dueDateErrorLabel.setText("");
        }

        if (returnDate != null) {
            if (borrowDate.isAfter(returnDate)) {
                returnDateErrorLabel.setText("Return Date must be after Borrow Date!");
                validation = true;
            } else if (returnDate.isAfter(LocalDate.now())) {
                returnDateErrorLabel.setText("Return Date must not be beyond current Date!");
                validation = true;
            } else {
                returnDateErrorLabel.setText("");
            }
        }

        if (validation) {
            return;
        }

        double fine = 0;
        Status newStatus = Status.BORROWING;

        if (returnDate == null) {
            if (LocalDate.now().isAfter(dueDate)) {
                newStatus = Status.OVERDUE;
                long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
                fine = overdueDays * 5000;
            } else {
                newStatus = Status.BORROWING;
            }
        } else {
            if (returnDate.isAfter(dueDate)) {
                newStatus = Status.RETURNED_LATE;
                long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(dueDate, returnDate);
                fine = overdueDays * 5000;
            } else {
                newStatus = Status.RETURNED;
                fine = 0;
            }
        }

        // Cập nhật status và fine lên giao diện
        statusLabel.setText(newStatus.toString());
        fineLabel.setText(String.valueOf(fine));
    }


    private void hideErrorLabels() {
        borrowDateErrorLabel.setText("");
        dueDateErrorLabel.setText("");
        returnDateErrorLabel.setText("");
    }
}
