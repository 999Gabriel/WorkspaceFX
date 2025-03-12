module swp.com.workspace_fx {
    requires javafx.controls;
    requires javafx.fxml;


    opens swp.com.workspace_fx to javafx.fxml;
    exports swp.com.workspace_fx;
}