package Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class NewMailController {
    @FXML
    public Button btnSend;
    @FXML
    public TextField areaTo;
    @FXML
    public Button btnHelp;
    @FXML
    public TextField areaSubject;
    @FXML
    public TextArea areaMessage;

    private DataModel model;
    private int reference;


    private Stage stage;

    void initModel(DataModel model, Stage stage){
        if(this.model != null)
            throw new IllegalStateException("Model already initialized");
        this.model = model;
        reference = model.getNewReference();
        this.stage = stage;
    }

    void initData(String message, String subject, String to){
        areaMessage.setText(message);
        areaSubject.setText(subject);
        areaTo.setText(to);
    }

    int getReference(){
        return reference;
    }

    Stage getStage() {
        return stage;
    }

    @FXML
    public void onSend(ActionEvent actionEvent) {

        ClientLog status = model.sendmail(areaSubject.getText(), areaMessage.getText(), areaTo.getText(), reference);
        if(status.equals(ClientLog.REQUEST_SUCCESSFUL))
            return;

        String message;
        Alert.AlertType alertType;
        switch (status){
            case SEND_ERROR:
                message = "An occurred while sending the email";
                alertType = Alert.AlertType.ERROR;
                break;
            case INVALID_MAIL:
                message = "One or more emails is not valid";
                alertType = Alert.AlertType.ERROR;
                break;
            case SERVER_DISCONNECTED:
                message = "Server disconnected";
                alertType = Alert.AlertType.ERROR;
                break;
            case NO_RECIPIENTS:
                message = "Specify at least one recipient";
                alertType = Alert.AlertType.ERROR;
                break;
            default:
                message = "An unknown error occurred";
                alertType = Alert.AlertType.ERROR;
                break;
            }
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.show();


    }

    @FXML
    public void onHelp(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "To add multiple recipients, separate them with a comma", ButtonType.OK);
        alert.show();
    }
}
