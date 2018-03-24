package org.shvdy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.shvdy.controllers.MainController;
import org.shvdy.model.DaoFactory;

import java.io.IOException;

public class AppMain extends Application {
    private static final String MAINVIEW_FILE = "MainView.fxml";
    private static final DaoFactory.Implementations DEFAULT_DATA_SOURCE = DaoFactory.Implementations.ORACLE;

    @Override
    public void start(Stage appWindow) {
        appWindow.setTitle("lab02");
        appWindow.setMinHeight(420);
        appWindow.setMinWidth(400);

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource(MAINVIEW_FILE));
            appWindow.setScene(new Scene(fxmlLoader.load()));
            appWindow.show();
            fxmlLoader.<MainController>getController().initAfterSceneLoaded(DEFAULT_DATA_SOURCE);

        } catch (IOException e) {
            System.out.println("Failed to load "+ MAINVIEW_FILE + "\n" + e);
            Alert alert = new Alert(Alert.AlertType.ERROR, "Critical error:\n" + e.getMessage());
            alert.showAndWait();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
