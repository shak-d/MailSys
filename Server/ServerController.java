package Server;

import common.ActionType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;

public class ServerController implements ServerListner {

    @FXML
    private TextArea logArea;
    @FXML
    private Button btnKill;
    private DataModel dataModel;
    private Stage stage;

    void initModel(DataModel model, Stage stage){

        if(dataModel != null)
            throw new IllegalStateException("Model already initialized");
        dataModel = model;
        this.stage = stage;
        dataModel.setServerListner(this);
        dataModel.startServer();

        stage.setOnCloseRequest(event -> {
            try {
                dataModel.closeServer();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            Platform.exit();
        });
    }

    @FXML
    public void actionKill(ActionEvent actionEvent) throws IOException, InterruptedException {
        dataModel.closeServer();
        stage.close();
    }



    @Override
    public synchronized void onServerEvent(ServerLog log, Object ...args) {
        String message;
        switch (log){
            case SERVER_STARTED:
                message = "Server started on port: " + dataModel.getMailPort();
                break;
            case ACCEPTED_CONNECTION:
                message = "New connection accepted";
                break;
            case CLIENT_AUTH:
                message = ((ClientHandler)args[0]).getMyMail() + ": logged in";
                break;
            case ACTION_RECEIVED:
                message = ((ClientHandler)args[0]).getMyMail() + ": "+ ((ActionType)args[1]).toString()  + " action received";
                break;
            case CLIENT_DISCONNECTED:
                message = ((ClientHandler)args[0]).getMyMail() + ": user disconnected";
                break;
            default:
                message = "Unknown event";
                break;
        }
        logArea.appendText( message + "\n");
    }

    @Override
    public synchronized void onServerError(ServerError error, Object ...args) {
        String errorMessage;
        switch (error){
            case ACCEPT_CONNECTION:
                errorMessage = "Couldn't accept new user";
                break;
            case CLIENT_INIT:
                errorMessage = "Unable to initialize new client";
                break;
            case AUTH_FAILED:
                errorMessage = "Client authentication failed";
                break;
            case FAILED_RECEIVING:
                errorMessage = ((ClientHandler)args[0]).getMyMail() + ": Couldn't receive message";
                break;
            case WRONG_RECIPIENT:
                errorMessage = ((ClientHandler)args[0]).getMyMail() + ": Send request failed, wrong recipient";
                break;
            default:
                errorMessage = "Unknown";
                break;
        }
        logArea.appendText("ERROR: " + errorMessage + "\n");
    }
}
