package librio.models;

import javafx.beans.property.*;
import librio.enums.NotificationType;

import java.time.Instant;

public class Notification {
    private StringProperty id;
    private StringProperty memberId;
    private StringProperty borrowId;
    private ObjectProperty<NotificationType> notificationType;
    private StringProperty message;
    private BooleanProperty isRead;
    private ObjectProperty<Instant> createdAt;

    public Notification() {
        this.id = new SimpleStringProperty();
        this.memberId = new SimpleStringProperty();
        this.borrowId = new SimpleStringProperty();
        this.notificationType = new SimpleObjectProperty<>();
        this.message = new SimpleStringProperty();
        this.isRead = new SimpleBooleanProperty(false);
        this.createdAt = new SimpleObjectProperty<>();
    }

    public Notification(String id, String memberId, String borrowId, NotificationType notificationType, String message, boolean isRead, Instant createdAt) {
        this.id = new SimpleStringProperty(id);
        this.memberId = new SimpleStringProperty(memberId);
        this.borrowId = new SimpleStringProperty(borrowId);
        this.notificationType = new SimpleObjectProperty<>(notificationType);
        this.message = new SimpleStringProperty(message);
        this.isRead = new SimpleBooleanProperty(isRead);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getMemberId() {
        return memberId.get();
    }

    public StringProperty memberIdProperty() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId.set(memberId);
    }

    public String getBorrowId() {
        return borrowId.get();
    }

    public StringProperty borrowIdProperty() {
        return borrowId;
    }

    public void setBorrowId(String borrowId) {
        this.borrowId.set(borrowId);
    }

    public NotificationType getNotificationType() {
        return notificationType.get();
    }

    public ObjectProperty<NotificationType> notificationTypeProperty() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType.set(notificationType);
    }

    public String getMessage() {
        return message.get();
    }

    public StringProperty messageProperty() {
        return message;
    }

    public void setMessage(String message) {
        this.message.set(message);
    }

    public boolean isIsRead() {
        return isRead.get();
    }

    public BooleanProperty isReadProperty() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead.set(isRead);
    }

    public Instant getCreatedAt() {
        return createdAt.get();
    }

    public ObjectProperty<Instant> createdAtProperty() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt.set(createdAt);
    }
}
