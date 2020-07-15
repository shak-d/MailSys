package common;

import java.io.Serializable;

public class MailAction implements Serializable {

    private ActionType actionType;
    private Mail mail;
    private int reference;

    public MailAction(ActionType actionType, Mail mail, int reference){

        this.actionType = actionType;
        this.mail = mail;
        this.reference = reference;
    }

    public int getReference() {
        return reference;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public Mail getMail() {
        return mail;
    }
}
