package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import librio.session.Session;
import librio.database.DatabaseConnection;
import librio.models.Borrow;
import librio.enums.Status;

import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

import static librio.util.DesignUtil.setDatePickerFormat;

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
        LocalDate dueDate = null;
        LocalDate returnDate = null;
        Status status = Status.valueOf(statusLabel.getText());
        double fine = Double.parseDouble(fineLabel.getText().replace(" VNĐ", ""));

        String returnDateString = returnDatePicker.getEditor().getText();
        String dueDateString = dueDatePicker.getEditor().getText();
        String dateRegex = "^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/\\d{4}$";
        boolean validation = false;

        if (!dueDateString.matches(dateRegex)) {
            dueDateErrorLabel.setText("Invalid date format!");
            validation = true;
        } else {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                dueDate = LocalDate.parse(dueDateString, formatter);
                if (dueDate.isBefore(borrowDate)) {
                    dueDateErrorLabel.setText("Due date cannot be before borrow date!");
                    validation = true;
                }
            } catch (Exception e) {
                dueDateErrorLabel.setText("Invalid date format!");
                validation = true;
            }
        }

        if (!returnDateString.isEmpty()) {
            if (!returnDateString.matches(dateRegex)) {
                returnDateErrorLabel.setText("Invalid date format!");
                validation = true;
            } else {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                    returnDate = LocalDate.parse(returnDateString, formatter);

                    if (borrowDate.isAfter(returnDate)) {
                        returnDateErrorLabel.setText("Return Date must be after Borrow Date!");
                        validation = true;
                    } else if (returnDate.isAfter(LocalDate.now())) {
                        returnDateErrorLabel.setText("Return Date must not be after current Date!");
                        validation = true;
                    } else if (ChronoUnit.DAYS.between(borrowDate, dueDate) > 90) {
                        dueDateErrorLabel.setText("The borrowing period cannot exceed 90 days!");
                        validation = true;
                    }
                } catch (DateTimeParseException e) {
                    returnDateErrorLabel.setText("Invalid date!");
                    validation = true;
                }
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
            statement.setDate(3, returnDate != null ? Date.valueOf(returnDate) : null);  // Nếu returnDate null thì không set ngày trả
            statement.setString(4, status.name());
            statement.setString(5, String.valueOf(fine));
            statement.setString(6, Session.getInstance().getLoggedInUser().getEmail());
            statement.setInt(7, borrow.getId());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
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
        dueDatePicker.setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        dueDatePicker.getEditor().setOnMouseClicked(event -> {
            hideErrorLabels();
        });
        returnDatePicker.getEditor().setOnMouseClicked(event -> {
            hideErrorLabels();
        });

        setDatePickerFormat(borrowDatePicker);
        setDatePickerFormat(dueDatePicker);
        setDatePickerFormat(returnDatePicker);

        borrowDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateStatus());
        dueDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateStatus());
        returnDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateStatus());

    }


    private void updateStatus() {
        LocalDate borrowDate = borrowDatePicker.getValue() == null ? borrow.getBorrowDate() : borrowDatePicker.getValue();
        LocalDate dueDate = dueDatePicker.getValue() == null ? borrow.getDueDate() : dueDatePicker.getValue();
        LocalDate returnDate = returnDatePicker.getValue();
        boolean validation = false;

        if (dueDate == null || dueDate.isBefore(borrowDate)) {
            validation = true;
        }

        if (validation) {
            return;
        }

        if (returnDate != null) {
            if (borrowDate.isAfter(returnDate)) {
                validation = true;
            } else if (returnDate.isAfter(LocalDate.now())) {
                validation = true;
            }
        }

        if (validation) {
            return;
        }


        if (ChronoUnit.DAYS.between(borrowDate, dueDate) > 90) {
            validation = true;
        }

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
                fine = overdueDays * 5000 < 100000 ? overdueDays * 5000 : 100000;
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
