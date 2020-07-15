package Client;

import common.Mail;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class ReadMailController {

    @FXML
    public TextArea messageArea;
    @FXML
    public TextField fromArea;
    @FXML
    public TextField toArea;
    @FXML
    public Button btnReply;
    @FXML
    public Button btnForward;
    @FXML
    public Button btnDelete;
    @FXML
    public TextField subjectArea;

    private DataModel model;
    private HomeController homeController;
    private Mail mail;
    private Stage stage;

    void initModel(DataModel model, Stage stage, HomeController homeController){
        if(this.model != null)
            throw new IllegalStateException("Model already initialized");
        this.model = model;
        this.homeController = homeController;
        this.stage = stage;
    }

    void initData(Mail mail){

        StringBuilder toBuilder = new StringBuilder();
        int i = mail.getRecipients().size()-1;
        for(String to : mail.getRecipients()){
            toBuilder.append(to);
            if(i != 0){
                toBuilder.append(", ");
                i--;
            }
        }

        messageArea.setText(mail.getMessage());
        fromArea.setText(mail.getSender());
        toArea.setText(toBuilder.toString());
        subjectArea.setText(mail.getMailObject());
        this.mail = mail;
    }

    @FXML
    public void onReply(ActionEvent actionEvent) throws IOException {

        Stage newMailStage;
        newMailStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("newmail.fxml"));
        Parent root = loader.load();
        newMailStage.setTitle("Reply to " + mail.getSender());
        newMailStage.setScene(new Scene(root, 640, 400));
        newMailStage.resizableProperty().setValue(false);
        NewMailController controller = loader.getController();
        controller.initModel(model,newMailStage);
        controller.initData("", mail.getMailObject(), mail.getSender());
        synchronized (homeController.lock){
            homeController.addNewMailScreen(controller);
        }
        newMailStage.show();
        stage.close();
    }

    @FXML
    public void onForward(ActionEvent actionEvent) throws IOException {
        Stage newMailStage;
        newMailStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("newmail.fxml"));
        Parent root = loader.load();
        newMailStage.setTitle("Forward " + mail.getMailObject());
        newMailStage.setScene(new Scene(root, 640, 400));
        newMailStage.resizableProperty().setValue(false);
        NewMailController controller = loader.getController();
        controller.initModel(model,newMailStage);
        controller.initData(mail.getMessage(), mail.getMailObject(), "");
        synchronized (homeController.lock){
            homeController.addNewMailScreen(controller);
        }
        newMailStage.show();
        stage.close();
    }

    @FXML
    public void onDelete(ActionEvent actionEvent) {
        if(!model.deleteMail(mail).equals(ClientLog.REQUEST_SUCCESSFUL)){

            Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to delete the email", ButtonType.OK);
            alert.show();
        }
        else {
            model.setCurrentMail(null);
            stage.close();
        }
    }
}
