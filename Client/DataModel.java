package Client;

import common.ActionType;
import common.Mail;
import common.MailAction;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DataModel {

    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private Socket socket;
    private ObservableList<Mail> mails;
    private final ObjectProperty<Mail> currentMail = new SimpleObjectProperty<>();
    private ClientListner clientListner;

    private String myMail;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int reference;
    private SocketListner socketListner;

    void setClientListener(ClientListner clientListner) {
        this.clientListner = clientListner;
    }

    ObservableList<Mail> getMails() {
        return mails;
    }

    void setCurrentMail(Mail currentMail) {
        this.currentMail.set(currentMail);
    }

    ObjectProperty<Mail> currentMailProperty() {
        return currentMail;
    }

    DataModel(){
        mails = FXCollections.observableArrayList();
        reference = 0;
    }

    void establishConnection(){
        try {
            socket = new Socket("127.0.0.1", 5522);
            File cfg = new File("mail.cfg");
            if(!cfg.exists()){
                if(clientListner != null)
                    clientListner.onClientError(ClientLog.CONFIG_MISSING);
                return;
            }
            Scanner scanner = new Scanner(cfg, StandardCharsets.UTF_8.name());
            if(scanner.hasNextLine())
                myMail = scanner.nextLine();
            else{
                if(clientListner != null)
                    clientListner.onClientError(ClientLog.CONNECTION_FAILED);
                return;
            }

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            PrintWriter outgoing = new PrintWriter(socket.getOutputStream(), true);
            outgoing.println(myMail);
            ArrayList<Mail> mailsList =  (ArrayList<Mail>)in.readObject();
            mails.addAll(mailsList);
        }catch (IOException e){
            if(clientListner != null)
                clientListner.onClientError(ClientLog.CONNECTION_FAILED);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        socketListner = new SocketListner(this);
        Thread tr = new Thread(socketListner);
        tr.start();
    }

    ClientLog sendmail(String object, String message, String recipients, int myReference){
        recipients = recipients.trim();
        if(recipients.isEmpty()){
            return ClientLog.NO_RECIPIENTS;
        }
        List<String> mailRecipients = new ArrayList<>();
        String[] receip = recipients.split(",");
        for (String recipient : receip) {
            String m = recipient.trim();
            if(!validateMail(m)){
                return ClientLog.INVALID_MAIL;
            }
            mailRecipients.add(m);
        }
        Mail mail = new Mail(-1,message,object, mailRecipients, myMail, new Date());
        MailAction mailAction = new MailAction(ActionType.SEND, mail, myReference);
        try {
            out.writeObject(mailAction);
            return ClientLog.REQUEST_SUCCESSFUL;
        } catch (IOException e) {
            if(!socket.isConnected()){
                return ClientLog.SERVER_DISCONNECTED;
            }
            else{
                return ClientLog.SEND_ERROR;
            }
        }
    }

    ClientLog deleteMail(Mail mail){

        MailAction mailAction = new MailAction(ActionType.DELETE, mail, -1);
        try {
            out.writeObject(mailAction);
        } catch (IOException e) {
            return ClientLog.DELETE_ERROR;
        }
        mails.remove(mail);
        return ClientLog.REQUEST_SUCCESSFUL;
    }

    ObjectInputStream getIn() {
        return in;
    }

    ClientListner getClientListener() {
        return clientListner;
    }

    private boolean validateMail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }

    String getMyMail() {
        return myMail;
    }

    void closeConnection(){
        try {
            if(socketListner != null)
                socketListner.shutdown();
            if(socket!=null)
                socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    synchronized int getNewReference(){
        return reference++;
    }
}
