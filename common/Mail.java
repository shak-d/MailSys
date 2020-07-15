package common;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Mail implements Serializable {

    private String mailObject;
    private String message;
    private List<String> recipients;
    private Date date;
    private int id;
    private String sender;

    public Mail(int id, String msg, String obj, List<String> rcp, String from, Date date){

        message = msg;
        mailObject = obj;
        recipients = rcp;
        this.date = date;
        this.id = id;
        sender = from;
    }

    public String getSender() {
        return sender;
    }

    public String getMailObject() {
        return mailObject;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getRecipients() { return recipients; }

    public Date getDate() {
        return date;
    }

    public int getId() {
        return id;
    }

}
