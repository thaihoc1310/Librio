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

    exports librio;
    exports librio.controllers;
    exports librio.models;
    opens librio.controllers to javafx.fxml;
    exports librio.controllers.admin;
    opens librio.controllers.admin to javafx.fxml;
}