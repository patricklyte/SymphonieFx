package ch.meng.symphoniefx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;

public class Main extends Application {
    PropertyManager firstProperties = new PropertyManager("Environment", "FirstController");

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxml = getClass().getResource("/Symphonie.fxml");
        FXMLLoader loader = new FXMLLoader(fxml);
        Parent parentPane = loader.load();
        MainController controller = loader.getController();
        controller.setHostServices(getHostServices());
        primaryStage.initStyle(StageStyle.DECORATED);
        Group group = new Group(parentPane);
        Scene scene = new Scene(group, 1000, 600, true);
        primaryStage.setScene(scene);
        controller.setStage(this, primaryStage, group, scene);
        primaryStage.show();
        System.out.println(("primaryStage.show()"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
