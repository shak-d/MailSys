package Server;

import common.ActionType;
import common.Mail;
import common.MailAction;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {

    private Socket socket;
    private DataModel model;
    private Scanner incoming;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private String myMail;
    private ClientData clientData;
    private File mailFile;
    private ObjectOutputStream fileOutputStream;

    ClientHandler(Socket socket, DataModel model, int index){
        this.socket = socket;
        this.model = model;
        myMail = "unknown";
        try {
            incoming = new Scanner(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());


        } catch (IOException e) {
            model.getServerListner().onServerError(ServerError.CLIENT_INIT);
        }
    }

    @Override
    public void run() {

        myMail = incoming.nextLine();
        mailFile = new File(myMail+".dat");
        synchronized (model.getFileWriting()) {
            ObjectInputStream fileInputStream;
            try {
               // fileOutputStream = new ObjectOutputStream(new FileOutputStream(mailFile));
                fileInputStream = new ObjectInputStream(new FileInputStream(mailFile));
                fileOutputStream = null;
                clientData = (ClientData) fileInputStream.readObject();
                fileInputStream.close();
                if(clientData == null)
                    clientData = new ClientData(null, 0);

            } catch (IOException | ClassNotFoundException e) {
                try {
                    mailFile.createNewFile();
                    fileOutputStream = new ObjectOutputStream(new FileOutputStream(mailFile));
                    fileInputStream = new ObjectInputStream(new FileInputStream(mailFile));
                    ArrayList<Mail> mailList = new ArrayList<>();
                    clientData = new ClientData(mailList, 0);
                    fileOutputStream.writeObject(clientData);
                    fileInputStream.close();
                    fileOutputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        try {
            objectOutputStream.writeObject(clientData.getMailData());
        } catch (IOException e) {
            model.getServerListner().onServerError(ServerError.CLIENT_INIT);
        }
        model.getServerListner().onServerEvent(ServerLog.CLIENT_AUTH, this);

        while(model.isServerRunning()){
            try {
                MailAction action = (MailAction)objectInputStream.readObject();
                if(model.getServerListner() != null)
                    model.getServerListner().onServerEvent(ServerLog.ACTION_RECEIVED, this, action.getActionType());
                switch (action.getActionType()){
                    case SEND:
                        sendMail(action.getMail(), action.getReference());
                        break;
                    case DELETE:
                        deleteMail(action.getMail());
                        break;
                }
            } catch (IOException | ClassNotFoundException e) {
                if(model.isServerRunning() && model.getServerListner() != null)
                    model.getServerListner().onServerEvent(ServerLog.CLIENT_DISCONNECTED, this);
                    synchronized (model.getFileWriting()){
                        model.removeClient(this);
                    }
                return;
            }
        }
    }

    private void deleteMail(Mail mail){

        synchronized (model.getFileWriting()){

            ArrayList<Mail> myMails = clientData.getMailData();
            for(Mail m : myMails){
                if(m.getId() == mail.getId()){
                    myMails.remove(m);
                    break;
                }
            }
            clientData.setMailData(myMails);
            ClientData cd = new ClientData(myMails, clientData.getIdIndex());
            try {
                fileOutputStream = new ObjectOutputStream(new FileOutputStream(mailFile));
                fileOutputStream.writeObject(cd);
                fileOutputStream.close();
                fileOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    String getMyMail() {
        return myMail;
    }

    private void sendMail(Mail actionMail, int reference) throws IOException {

        for(String recipient : actionMail.getRecipients()){
            File recFile;
            synchronized (model.getFileWriting()) {
                recFile = new File(recipient + ".dat");
            }
            if(!recFile.exists()){
                try {
                    objectOutputStream.writeObject(new MailAction(ActionType.UNKNOWN_RECIPIENT, null, reference));
                } catch (IOException e) {
                    if(model.getServerListner() != null)
                        model.getServerListner().onServerError(ServerError.CLIENT_DISCONNECTED, this);
                }
                if(model.getServerListner() != null)
                    model.getServerListner().onServerError(ServerError.WRONG_RECIPIENT, this);
                return;
            }

        }

        for(String recipient : actionMail.getRecipients()){

            ClientHandler user = null;
            for(ClientHandler usr : model.getClients()){
                if(recipient.equals(usr.getMyMail()))
                    user = usr;
            }

            if(user == null){
                File userFile = new File(recipient +".dat");
                synchronized (model.getFileWriting()) {
                    try {
                        ObjectInputStream fInput = new ObjectInputStream(new FileInputStream(userFile));
                        ClientData userData = (ClientData) fInput.readObject();
                        fInput.close();
                        Mail responseMail = new Mail(userData.getIdIndex(),
                                actionMail.getMessage(),
                                actionMail.getMailObject(),
                                actionMail.getRecipients(),
                                this.myMail,
                                actionMail.getDate());
                        userData.setIdIndex(userData.getIdIndex() + 1);
                        ArrayList<Mail> userMails = userData.getMailData();
                        userMails.add(responseMail);
                        userData.setMailData(userMails);
                        ObjectOutputStream fOutput = new ObjectOutputStream(new FileOutputStream(userFile));
                        fOutput.writeObject(clientData);
                        fOutput.close();
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            else{

                try {
                    user.updateMail(actionMail, myMail);
                } catch (IOException e) {
                    if(model.getServerListner() != null)
                        model.getServerListner().onServerError(ServerError.CLIENT_DISCONNECTED, user);
                }
            }

        }
        objectOutputStream.writeObject(new MailAction(ActionType.SUCCESSFUL_OP, actionMail, reference));

    }

    private synchronized void updateMail(Mail actionMail, String from) throws IOException {

        Mail responseMail;
        synchronized (model.getFileWriting()) {
            responseMail = new Mail(clientData.getIdIndex(),
                actionMail.getMessage(),
                actionMail.getMailObject(),
                actionMail.getRecipients(),
                from,
                actionMail.getDate());
            ArrayList<Mail> myMails = clientData.getMailData();
            myMails.add(responseMail);
            clientData.setIdIndex(clientData.getIdIndex() + 1);
            clientData.setMailData(myMails);
            ClientData cd = new ClientData(myMails, clientData.getIdIndex()+1);
            fileOutputStream = new ObjectOutputStream(new FileOutputStream(mailFile));
            fileOutputStream.writeObject(cd);
            fileOutputStream.close();
        }
        MailAction responseAction = new MailAction(ActionType.SEND, responseMail, -1);
        objectOutputStream.writeObject(responseAction);
    }

    void close() throws IOException {
        socket.close();
    }
}
