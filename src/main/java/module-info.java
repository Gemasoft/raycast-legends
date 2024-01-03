module com.gemasoft.gema_engine {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;
    requires java.desktop;

    opens com.gemasoft.gema_engine to javafx.fxml;
    exports com.gemasoft.gema_engine;
}