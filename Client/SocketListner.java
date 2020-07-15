package Client;

import common.MailAction;
import javafx.application.Platform;

import java.io.IOException;

public class SocketListner implements Runnable {

     private DataModel model;
     private boolean shutdown;

    SocketListner(DataModel model){

        this.model = model;
        shutdown = false;
    }

    void shutdown(){
        shutdown = true;
    }

    @Override
    public void run() {
        boolean error = false;
        while(!error && !shutdown){
            try {
                MailAction action = (MailAction) model.getIn().readObject();
                switch (action.getActionType()){
                    case SEND:
                        Platform.runLater(()-> model.getMails().add(action.getMail()));
                        break;
                    case SUCCESSFUL_OP:
                        if(model.getClientListener() != null)
                            Platform.runLater(()-> model.getClientListener().onServerResponse(ClientLog.REQUEST_SUCCESSFUL, action));
                        break;
                    case UNKNOWN_RECIPIENT:
                        if(model.getClientListener() != null)
                            Platform.runLater(()-> model.getClientListener().onServerResponse(ClientLog.UNKNOWN_MAIL));
                        break;
                }
            } catch (IOException e) {
                error = true;
                if(model.getClientListener() != null)
                    Platform.runLater(()-> model.getClientListener().onClientError(ClientLog.SERVER_DISCONNECTED));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
}
