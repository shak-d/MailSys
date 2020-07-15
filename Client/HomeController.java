package Client;

import common.Mail;
import common.MailAction;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class HomeController implements ClientListner {


    @FXML
    private Button btnNewMail;
    @FXML
    private Button btnReply;
    @FXML
    private Button btnOpen;
    @FXML
    private Button btnReplyAll;
    @FXML
    private Button btnForward;
    @FXML
    private Button btnDelete;
    @FXML
    private Label labelStatus;
    @FXML
    private ListView<Mail> listMail;

    private DataModel dataModel;
    private ArrayList<NewMailController> newMailScreens;

    final Object lock = new Object();

    void initModel(DataModel model, Stage primaryStage){

        if(dataModel != null)
            throw new IllegalStateException("Model already initialized");
        dataModel = model;
        dataModel.setClientListener(this);
        newMailScreens = new ArrayList<>();

        listMail.setItems(model.getMails());
        SimpleDateFormat mdformat = new SimpleDateFormat("dd/MM/YY HH:mm:ss");
        listMail.setCellFactory(param -> new ListCell<Mail>() {
            @Override
            protected void updateItem(Mail item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getMailObject() == null) {
                    setText(null);
                } else {
                    setText( mdformat.format(item.getDate()) + "    " + item.getSender() + ":  " + item.getMailObject());
                }
            }
        });

        listMail.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) ->{
                model.setCurrentMail(newSelection);
                setEnabledActionButtons(true);
        });

        model.currentMailProperty().addListener((obs, oldMail, newMail) -> {
            if (newMail == null) {
                listMail.getSelectionModel().clearSelection();
                setEnabledActionButtons(false);
            } else {
                listMail.getSelectionModel().select(newMail);
                setEnabledActionButtons(true);
            }
        });

        model.getMails().addListener((ListChangeListener<Mail>) c -> {
            listMail.setItems((ObservableList<Mail>) c.getList());

        });

        model.establishConnection();

        primaryStage.setOnCloseRequest(event -> {
            model.closeConnection();
            Platform.exit();
        });

    }

    @FXML
    private void openAction(ActionEvent actionEvent) throws IOException {
        Mail item = listMail.getSelectionModel().getSelectedItem();
        if(item != null) {

            Stage readMailStage;
            readMailStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("readmail.fxml"));
            Parent root = loader.load();
            readMailStage.setTitle(item.getSender() + ": " + item.getMailObject());
            readMailStage.setScene(new Scene(root, 600, 400));
            readMailStage.resizableProperty().setValue(false);
            ReadMailController controller = loader.getController();
            controller.initModel(dataModel, readMailStage, this);
            controller.initData(item);
            readMailStage.show();

        }
    }

    @FXML
    private void replyAction(ActionEvent actionEvent) throws IOException {
        Mail item = listMail.getSelectionModel().getSelectedItem();
        if(item == null)
            return;
        Stage newMailStage;
        newMailStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("newmail.fxml"));
        Parent root = loader.load();
        newMailStage.setTitle("Reply to " + item.getSender());
        newMailStage.setScene(new Scene(root, 640, 400));
        newMailStage.resizableProperty().setValue(false);
        NewMailController controller = loader.getController();
        controller.initModel(dataModel,newMailStage);
        controller.initData("", item.getMailObject(), item.getSender());
        synchronized (lock){
            newMailScreens.add(controller);
        }
        newMailStage.show();

    }

    @FXML
    private void newMailAction(ActionEvent actionEvent) throws IOException {

        Stage newMailStage;
        newMailStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("newmail.fxml"));
        Parent root = loader.load();
        newMailStage.setTitle("New mail");
        newMailStage.setScene(new Scene(root, 640, 400));
        newMailStage.resizableProperty().setValue(false);
        NewMailController controller = loader.getController();
        controller.initModel(dataModel,newMailStage);
        synchronized (lock){
            newMailScreens.add(controller);
        }
        newMailStage.show();
    }

    @FXML
    private void replyAllAction(ActionEvent actionEvent) throws IOException {

        Mail item = listMail.getSelectionModel().getSelectedItem();
        if(item == null)
            return;

        StringBuilder sender = new StringBuilder();
        sender.append(item.getSender());
        for(String s : item.getRecipients()){
            if(!s.equals(item.getSender()) && !s.equals(dataModel.getMyMail()))
                sender.append(", ").append(s);
        }

        Stage newMailStage;
        newMailStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("newmail.fxml"));
        Parent root = loader.load();
        newMailStage.setTitle("Reply to " + sender.toString());
        newMailStage.setScene(new Scene(root, 640, 400));
        newMailStage.resizableProperty().setValue(false);
        NewMailController controller = loader.getController();
        controller.initModel(dataModel,newMailStage);
        controller.initData("", item.getMailObject(), sender.toString());
        synchronized (lock){
            newMailScreens.add(controller);
        }
        newMailStage.show();
    }

    @FXML
    private void forwardAction(ActionEvent actionEvent) throws IOException {

        Mail item = listMail.getSelectionModel().getSelectedItem();
        if(item == null)
            return;

        Stage newMailStage;
        newMailStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("newmail.fxml"));
        Parent root = loader.load();
        newMailStage.setTitle("Forward " + item.getMailObject());
        newMailStage.setScene(new Scene(root, 640, 400));
        newMailStage.resizableProperty().setValue(false);
        NewMailController controller = loader.getController();
        controller.initModel(dataModel,newMailStage);
        controller.initData(item.getMessage(), item.getMailObject(), "");
        synchronized (lock){
            newMailScreens.add(controller);
        }
        newMailStage.show();
    }

    @FXML
    private void deleteAction(ActionEvent actionEvent){
        Mail item = listMail.getSelectionModel().getSelectedItem();
        if(item != null) {
            if(!dataModel.deleteMail(item).equals(ClientLog.REQUEST_SUCCESSFUL)){

                Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to delete the email", ButtonType.OK);
                alert.show();
            }
            else
                dataModel.setCurrentMail(null);
        }
    }

    @Override
    public void onClientError(ClientLog error, Object... param) {

        String message;
        switch (error){
            case CONNECTION_FAILED:
                message = "Failed to connect to the server!";
                disableClient();
                break;
            case CONFIG_MISSING:
                message = "Failed to read mail.cfg";
                disableClient();
                break;
            case SERVER_DISCONNECTED:
                message = "Connection to the server has been lost";
                break;
            default:
                message = "An unknown error occurred";
                break;
        }
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.show();
        /*labelStatus.setTextFill(Color.web("#FF0000"));
        labelStatus.setText(message);*/
    }

    @Override
    public void onServerResponse(ClientLog event, Object... param) {
        String message;
        Alert.AlertType alertType;
        switch (event){
            case REQUEST_SUCCESSFUL:
                message = "Email sent!";
                alertType = Alert.AlertType.CONFIRMATION;
                break;
            case UNKNOWN_MAIL:
                message = "One or more emails do not exist";
                alertType = Alert.AlertType.ERROR;
                break;
            default:
                message="Unknown alert";
                alertType = Alert.AlertType.ERROR;
                break;
        }
        if(alertType.equals(Alert.AlertType.CONFIRMATION)) {
            MailAction action = (MailAction)param[0];
            synchronized (lock){
                for(NewMailController controller : newMailScreens){
                     if(controller.getReference() == action.getReference()){
                        controller.getStage().close();
                        newMailScreens.remove(controller);
                        break;
                    }

                }
            }
        }
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.show();

    }

    private void disableClient(){
        btnDelete.setDisable(true);
        btnForward.setDisable(true);
        btnNewMail.setDisable(true);
        btnOpen.setDisable(true);
        btnReply.setDisable(true);
        btnReplyAll.setDisable(true);
        listMail.setDisable(true);
    }

    private void setEnabledActionButtons(boolean value){
        btnDelete.setDisable(!value);
        btnForward.setDisable(!value);
        btnOpen.setDisable(!value);
        btnReply.setDisable(!value);
        btnReplyAll.setDisable(!value);
    }

    void addNewMailScreen(NewMailController controller){
        synchronized (lock){
            newMailScreens.add(controller);
        }
    }
}
