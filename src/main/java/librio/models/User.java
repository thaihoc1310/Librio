package librio.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import librio.enums.Gender;
import librio.enums.Role;

import java.time.Instant;
import java.time.LocalDate;

public class User {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty email;
    private StringProperty password;
    private final StringProperty phoneNumber;
    private StringProperty address;
    private final ObjectProperty<Gender> gender;
    private ObjectProperty<Role> role;
    private StringProperty avatar;
    private ObjectProperty<LocalDate> birthOfDate;
    private String created_by;
    private Instant created_at;
    private StringProperty update_by;
    private ObjectProperty<Instant> update_at;

    public User() {
        this.id = new SimpleStringProperty();
        this.name = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.password = new SimpleStringProperty();
        this.phoneNumber = new SimpleStringProperty();
        this.gender = new SimpleObjectProperty<>();
        this.address = new SimpleStringProperty();
        this.avatar = new SimpleStringProperty();
        this.birthOfDate = new SimpleObjectProperty<>();
        this.update_at = new SimpleObjectProperty<>();
        this.update_by = new SimpleStringProperty();
    }

    public User(String id, String name, String email, String phoneNumber, Gender gender, Role role) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.phoneNumber = new SimpleStringProperty(phoneNumber);
        this.role = new SimpleObjectProperty<>(role);
        this.gender = new SimpleObjectProperty<>(gender);
    }

    public User(String id, String name, String email, String password, String phoneNumber, String address, Gender gender, Role role, String avatar, LocalDate birthOfDate) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.password = new SimpleStringProperty(password);
        this.phoneNumber = new SimpleStringProperty(phoneNumber);
        this.address = new SimpleStringProperty(address);
        this.role = new SimpleObjectProperty<>(role);
        this.gender = new SimpleObjectProperty<>(gender);
        this.avatar = new SimpleStringProperty(avatar);
        this.birthOfDate = new SimpleObjectProperty<>(birthOfDate);
    }

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public StringProperty idProperty() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public StringProperty emailProperty() {
        return email;
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.set(phoneNumber);
    }

    public StringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    public String getAddress() {
        return address.get();
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public StringProperty addressProperty() {
        return address;
    }

    public Gender getGender() {
        return gender.get();
    }

    public void setGender(Gender gender) {
        this.gender.set(gender);
    }

    public ObjectProperty<Gender> genderProperty() {
        return gender;
    }

    public Role getRole() {
        return role.get();
    }

    public void setRole(Role role) {
        this.role.set(role);
    }

    public ObjectProperty<Role> roleProperty() {
        return role;
    }

    public String getAvatar() {
        return avatar.get();
    }

    public void setAvatar(String avatar) {
        this.avatar.set(avatar);
    }

    public StringProperty avatarProperty() {
        return avatar;
    }

    public LocalDate getBirthOfDate() {
        return birthOfDate.get();
    }

    public void setBirthOfDate(LocalDate birthOfDate) {
        this.birthOfDate.set(birthOfDate);
    }

    public ObjectProperty<LocalDate> birthOfDateProperty() {
        return birthOfDate;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }

    public String getUpdate_by() {
        return update_by.get();
    }

    public void setUpdate_by(String update_by) {
        this.update_by.set(update_by);
    }

    public StringProperty update_byProperty() {
        return update_by;
    }

    public Instant getUpdate_at() {
        return update_at.get();
    }

    public void setUpdate_at(Instant update_at) {
        this.update_at.set(update_at);
    }

    public ObjectProperty<Instant> update_atProperty() {
        return update_at;
    }
}
