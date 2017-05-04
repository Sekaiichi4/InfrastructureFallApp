package com.example.atakan.infrastructurefallapp.model;

import java.io.Serializable;
import java.util.Map;

public class EmailModel implements Serializable {
    private String to;
    private String subject;
    private String message;

    private Map<String, String> headers;
    private Map<String, String> parameters;

    public EmailModel() {
    }

    public EmailModel(String to, String subject, String message, Map<String, String> headers, Map<String, String> parameters) {
        this.to = to;
        this.subject = subject;
        this.message = message;
        this.headers = headers;
        this.parameters = parameters;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
