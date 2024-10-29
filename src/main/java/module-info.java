module librio {
    requires javafx.controls;
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

    exports librio;
    exports librio.controllers;
    exports librio.models;
    opens librio.controllers to javafx.fxml;
    exports librio.controllers.admin;
    exports librio.controllers.member;
    opens librio.controllers.admin to javafx.fxml;
    opens librio.controllers.member to javafx.fxml;
}