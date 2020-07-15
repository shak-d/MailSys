package Server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("server.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Mail server");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.resizableProperty().setValue(false);
        ServerController controller = loader.getController();
        DataModel model = new DataModel();
        controller.initModel(model, primaryStage);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
