module librio {
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires java.desktop;
    requires java.net.http;
    requires org.json;
    requires mysql.connector.j;
    requires java.mail;
    requires com.google.zxing;
    requires com.google.zxing.javase;

    exports librio;
    exports librio.models;
    exports librio.enums;
    exports librio.controllers.admin;
    exports librio.controllers.member;
    opens librio.controllers.admin to javafx.fxml;
    opens librio.controllers.member to javafx.fxml;
    exports librio.controllers.auth;
    opens librio.controllers.auth to javafx.fxml;
}