package com.ahenry.msmsimporter.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by axel on 09/06/15.
 */
public class MMS {


    public String transactionid;
    public Long timestamp;
    public String[] to;
    public String pathToAttachment;
    public String attachmentName;
    public String subject;
    public String mimeType;
    public boolean sent;

    public String getTransactionid() {
        return transactionid;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String[] getTo() {
        return to;
    }

    public String getPathToAttachment() {
        return pathToAttachment;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public String getSubject() {
        return subject;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isSent() {
        return sent;
    }

    public MMS(){
        /*default constructor to use with jackson*/
    }

    public MMS(String transactionid, Long timestamp, String[] to, String pathToAttachment, String attachmentName, String subject, String mimeType, boolean sent) {
        this.transactionid = transactionid;
        this.timestamp = timestamp;
        this.to = to;
        this.pathToAttachment = pathToAttachment;
        this.attachmentName = attachmentName;
        this.subject = subject;
        this.mimeType = mimeType;
        this.sent = sent;
    }

    public MMS(String trid, String to, String subject){
        this.pathToAttachment = null;
        this.attachmentName = null;
        this.mimeType = null;
        this.sent = false;
        this.transactionid = trid;
        this.subject = subject;
        String[] t = new String[1];
        t[0] = to;
        this.to = t;
        this.timestamp = new java.util.Date().getTime();
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append("---------------------------------\n");
        sb.append("id : ").append(transactionid).append("\n");
        sb.append("timestamp : ").append(timestamp).append("\n");
        sb.append("to: ").append(to.toString()).append("\n");
        sb.append("subject : ").append(subject).append("\n");
        sb.append("pathToAttachment : ").append(pathToAttachment).append("\n");
        sb.append("attachmentName : ").append(attachmentName).append("\n");
        sb.append("mimeType").append(mimeType).append("\n");
        sb.append("sent : ").append(sent).append("\n");
        sb.append("---------------------------------\n");

        return sb.toString();
    }


    @JsonIgnore
    public boolean isValid(){
        return to!=null && to.length>=1 && isValid(transactionid) && (isValid(subject) || (isValid(pathToAttachment) && isValid(attachmentName) && isValid(mimeType)));
    }


    private boolean isValid(String s){
        return s != null && s.compareTo("")!=0;
    }

}
