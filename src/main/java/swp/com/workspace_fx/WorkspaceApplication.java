package swp.com.workspace_fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import swp.com.workspace_fx.Logger;

import java.io.IOException;

public class WorkspaceApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(WorkspaceApplication.class.getResource("workspace-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 550);
        scene.getStylesheets().add(WorkspaceApplication.class.getResource("styles.css").toExternalForm());
        stage.setTitle("File Workspace");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(event -> Logger.getInstance().close());
    }

    public static void main(String[] args) {
        launch();
    }
}