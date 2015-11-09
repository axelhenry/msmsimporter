package com.ahenry.msmsimporter.models;

import com.ahenry.msmsimporter.utilities.Utilities;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by axel on 09/06/15.
 */
public class SMS {

    private Long timestamp;
    private String to;
    private String body;
    private boolean sent;

    public SMS(){
        /*default constructor to use with jackson*/
    }

    public SMS(Long timestamp, String to, String body, boolean sent) {
        this.timestamp = timestamp;
        this.to = to;
        this.body = body;
        this.sent = sent;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public boolean isSent() {
        return sent;
    }

    public String getBody() {
        return body;
    }

    public String getTo() {
        return to;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append("---------------------------------\n");
        sb.append("timestamp : ").append(timestamp).append("\n");
        sb.append("to : ").append(to).append("\n");
        sb.append("body : ").append(body).append("\n");
        sb.append("sent : ").append(sent).append("\n");
        sb.append("---------------------------------\n");

        return sb.toString();
    }

    @JsonIgnore
    public boolean isValid(){
        return isValid(to) && Utilities.isPhoneNumberValid(to) && isValid(body) && timestamp != null;
    }

    private boolean isValid(String s){
        return s != null && s.compareTo("")!=0;
    }
}
