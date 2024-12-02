package librio.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import librio.database.DatabaseConnection;
import librio.enums.Status;
import librio.models.Borrow;
import librio.session.Session;

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

import static librio.util.DatabaseUtil.*;
import static librio.util.DesignUtil.setDatePickerFormat;

public class UpdateBorrowController implements Initializable {
    @FXML
    protected Button backButton;
    @FXML
    protected DatePicker borrowDatePicker;
    @FXML
    protected DatePicker dueDatePicker;
    @FXML
    protected Label fineLabel;
    @FXML
    protected Label statusLabel;
    @FXML
    protected Label borrowDateErrorLabel;
    @FXML
    protected Label dueDateErrorLabel;
    @FXML
    protected Label returnDateErrorLabel;
    @FXML
    protected DatePicker returnDatePicker;

    private Borrow borrow;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets up the date pickers for the borrow, due, and return date fields and listeners
     * for changes to these fields to update the borrow status.
     *
     * @param url the location used to resolve relative paths for the root object, or null if unknown
     * @param resourceBundle the resources used to localize the root object, or null if not localized
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initDatePicker();
    }

    /**
     * Handles the action of navigating back from the current view.
     * This method will close the current stage window, effectively
     * taking the user back to the previous view or main application window.
     */
    @FXML
    void back() {
        closeStage();
    }

    /**
     * Closes the current stage associated with the backButton.
     * This method retrieves the current window from the scene of the backButton
     * and closes it, effectively hiding or terminating the associated stage.
     */
    private void closeStage() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Sets the borrow instance for the controller and updates the relevant fields
     * within the UI based on the provided borrow details.
     *
     * @param borrow the borrow object containing information about a book borrow transaction
     */
    public void setBorrow(Borrow borrow) {
        this.borrow = borrow;
        populateFields();
    }

    /**
     * Populates various fields in the user interface with data from the current borrow instance.
     *
     * This method is used to set the values of date pickers and labels based on the data retrieved
     * from the current {@code borrow} object. It sets the borrow date, due date, and return date
     * (if available) in their respective date pickers. It also updates the status label with the
     * string representation of the borrow status and the fine label with the fine amount.
     *
     * The borrow date and due date pickers are always set with the respective dates from the
     * borrow object. If a return date is available, it is set in the return date picker; otherwise,
     * the return date picker is cleared. The status label is updated to reflect the borrow status,
     * and the fine label is updated to show the current fine amount followed by " VNĐ".
     *
     * This method assumes that the {@code borrow} field is not null; it performs no action if
     * {@code borrow} is null. Therefore, it should be ensured that this method is called only
     * when the {@code borrow} instance has been properly initialized.
     */
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

    /**
     * Handles the update process for a borrowing record. This method validates the input
     * for borrow date, due date, and return date, ensuring correct date format and logical
     * date relationships. It adjusts book quantities based on the transition between
     * borrowing statuses and updates the borrowing record in the database.
     *
     * Performs the following actions:
     * - Validates the due date and return date inputs for correct format and logical correctness.
     * - Adjusts book quantities if there is a transition between BORROWING or OVERDUE and RETURNED or RETURNED_LATE statuses.
     * - Updates the borrowing record in the database with the provided information and changes the status.
     *
     * Does not proceed with database updates if input validation fails, displaying appropriate error messages
     * for invalid inputs.
     *
     * Utilizes the current session's user information to log who performed the update.
     *
     * Catches SQLException for any database-related issues and prints stack trace for any unexpected exceptions.
     */
    @FXML
    protected void updateBorrow() {
        if (borrow == null) {
            System.err.println("Cannot update borrow: borrow object is not initialized.");
            return;
        }
        LocalDate borrowDate = borrowDatePicker.getValue();
        LocalDate dueDate = null;
        LocalDate returnDate = null;
        Status oldStatus = borrow.getStatus();
        Status newStatus = Status.valueOf(statusLabel.getText());
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
                } else if (ChronoUnit.DAYS.between(borrowDate, dueDate) > 90) {
                    dueDateErrorLabel.setText("The borrowing period cannot exceed 90 days!");
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

        if (oldStatus == Status.OVERDUE || oldStatus == Status.BORROWING) {
            if (newStatus == Status.RETURNED || newStatus == Status.RETURNED_LATE) {
                updateQuantityBook(getBookByIsbn(borrow.getBookIsbn()).getId());
            }
        } else if (oldStatus == Status.RETURNED || oldStatus == Status.RETURNED_LATE) {
            if (newStatus == Status.BORROWING || newStatus == Status.OVERDUE) {
                decreaseQuantityBook(getBookByIsbn(borrow.getBookIsbn()).getId());
            }
        }

        String query = "UPDATE borrows SET borrow_date = ?, due_date = ?, return_date = ?, status = ?, fine = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDate(1, Date.valueOf(borrowDate));
            statement.setDate(2, Date.valueOf(dueDate));
            statement.setDate(3, returnDate != null ? Date.valueOf(returnDate) : null);  // Nếu returnDate null thì không set ngày trả
            statement.setString(4, newStatus.name());
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


    /**
     * Updates the status and fine labels of a borrow transaction based on the
     * selected borrow, due, and return dates.
     *
     * The method checks for various validation conditions:
     * 1. Whether the due date is before the borrow date.
     * 2. Whether the return date is before the borrow date or in the future.
     * 3. Whether the loan period exceeds 90 days.
     *
     * If any of these conditions are met, the method exits without updating
     * the status or fine.
     *
     * If the borrow is still ongoing or overdue without a return:
     * - If today's date is past the due date, sets status to OVERDUE and
     *   calculates a fine based on the overdue days, capped at 100,000.
     * - Otherwise, sets status to BORROWING with no fine.
     *
     * If a return date is present:
     * - If the return date is past the due date, sets status to RETURNED_LATE and
     *   calculates a fine based on the overdue days, capped at 100,000.
     * - Otherwise, sets status to RETURNED with no fine.
     *
     * The method updates the statusLabel with the new status and the
     * fineLabel with the calculated fine.
     */
    private void updateStatus() {
        LocalDate borrowDate = borrowDatePicker.getValue() == null ? borrow.getBorrowDate() : borrowDatePicker.getValue();
        LocalDate dueDate = dueDatePicker.getValue() == null ? borrow.getDueDate() : dueDatePicker.getValue();
        LocalDate returnDate = returnDatePicker.getValue();
        boolean validation = dueDate == null || dueDate.isBefore(borrowDate);

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


    /**
     * Clears the text of the error labels associated with borrow date, due date, and return date.
     * This method is used to hide any existing error messages that may have been displayed
     * due to invalid input or any other issues related to the date fields.
     */
    private void hideErrorLabels() {
        borrowDateErrorLabel.setText("");
        dueDateErrorLabel.setText("");
        returnDateErrorLabel.setText("");
    }

    /**
     * Initializes and configures the behavior and format of date picker components.
     * Sets mouse click events on date pickers to hide error labels and applies a specific date format.
     * Adds listeners to the value properties of the date pickers to update the borrow status.
     */
    private void initDatePicker() {
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
}
