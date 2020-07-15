package Client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.xml.crypto.Data;

public class ClientMain extends Application {



    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("home.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Mail");
        primaryStage.setScene(new Scene(root, 640, 400));
        primaryStage.resizableProperty().setValue(false);
        HomeController controller = loader.getController();
        DataModel model = new DataModel();
        controller.initModel(model, primaryStage);
        primaryStage.show();

    }


    public static void main(String[] args) {

        launch(args);
    }
}
