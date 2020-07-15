package Server;

import common.Mail;
import java.io.Serializable;
import java.util.ArrayList;

class ClientData implements Serializable {

    private ArrayList<Mail> mailData;

    private int idIndex;

    ClientData(ArrayList<Mail> mailData, int index){
        this.mailData = mailData;
        idIndex = index;
    }

    ArrayList<Mail> getMailData() {
        return mailData;
    }

    int getIdIndex() {
        return idIndex;
    }

    void setMailData(ArrayList<Mail> mailData) {
        this.mailData = mailData;
    }

    void setIdIndex(int idIndex) {
        this.idIndex = idIndex;
    }

}
